package watchclient;

import java.util.Arrays;
import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.KeeperException.Code;

/*
 *  monitors the data and existence of a ZooKeeper node
 */
public class DataMonitor {

    boolean dead;
    ZooKeeper zooKeeper;
    String zNodePath;
    Watcher chainedWatcher;
    DataMonitorListener dataMonitorListener;
    byte[] prevData;

    public DataMonitor(ZooKeeper zooKeeper, String zNodePath,
                       Watcher chainedWatcher, DataMonitorListener dataMonitorListener) throws InterruptedException, KeeperException {
        this.zooKeeper = zooKeeper;
        this.zNodePath = zNodePath;
        this.chainedWatcher = chainedWatcher;
        this.dataMonitorListener = dataMonitorListener;
        // Get things started by checking if the node exists. We are going
        // to be completely event driven
        //zooKeeper.exists(zNodePath, true);
        zooKeeper.exists(zNodePath, true, (StatCallback)this, null);
        /*
        * java.lang.ClassCastException: class watchclient.DataMonitor cannot be cast to
        * class org.apache.zookeeper.AsyncCallback$StatCallback (watchclient.DataMonitor and
        * org.apache.zookeeper.AsyncCallback$StatCallback are in unnamed module of loader 'app')
        * at watchclient.DataMonitor.<init>(DataMonitor.java:28)
        * at watchclient.Executor.<init>(Executor.java:71)
        * at watchclient.Executor.main(Executor.java:42)
        * */
    }

    /*
     * implemented in Executor
     */
    public interface DataMonitorListener {
        void exists(byte data[]);

        void closing(Code reasonCode);
    }

    public void process (WatchedEvent event) {
        System.out.println("receive event type name: " + event.getType().name());
        System.out.println("receive event path: " + event.getPath());

        String eventZNodePath = event.getPath();
        if (event.getType() == Event.EventType.None) {
            switch (event.getState()) {
                case SyncConnected:

                    break;
                case Expired:
                    System.out.println("It's all over ----------- The ZooKeeper session is no longer valid.");
                    dead = true;
                    dataMonitorListener.closing(Code.SESSIONEXPIRED);
                    break;
            }
        } else {
            if (null != eventZNodePath && eventZNodePath.equals(zNodePath)) {
                zooKeeper.exists(zNodePath, true, (StatCallback) this, null);

            }
        }
        if (null != chainedWatcher) {
            chainedWatcher.process(event);
        }
    }

    public void processResult (Code reasonCode, String path, Object ctx, Stat stat) {
        boolean exists;

        // KeeperException.Code
        // The code first checks the error codes for znode existence
        switch (reasonCode) {
            case OK:
                exists = true;
                break;
            case NONODE:
                exists = false;
                break;
            case SESSIONEXPIRED:
            case NOAUTH:
                dead = true;
                dataMonitorListener.closing(reasonCode);
                return;
            default:
                zooKeeper.exists(zNodePath, true, (StatCallback) this, null);
                return;
        }
        byte[] b = null;
        if (exists) {
            try {
                b = zooKeeper.getData(zNodePath, false, null);

            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }


        if ((null == b && prevData != b) || (null != b && !Arrays.equals(prevData, b))) {
            dataMonitorListener.exists(b);
            prevData = b;
        }
    }
}
