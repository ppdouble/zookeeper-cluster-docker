package watchclient;

import java.util.Arrays;
import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.KeeperException.Code;

public class DataMonitor {

    boolean dead;
    ZooKeeper zooKeeper;
    String znode;
    Watcher chainedWatcher;
    DataMonitorListener dataMonitorListener;
    byte[] prevData;

    public DataMonitor(ZooKeeper zooKeeper, String znode,
                       Watcher chainedWatcher, DataMonitorListener dataMonitorListener) {
        this.zooKeeper = zooKeeper;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.dataMonitorListener = dataMonitorListener;

    }

    public interface DataMonitorListener {
        void exists(byte data[]);

        void closing(Code reasonCode);
    }

    public void process (WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == Event.EventType.None) {
            switch (event.getState()) {
                case SyncConnected:

                    break;
                case Expired:

                    dead = true;
                    dataMonitorListener.closing(Code.SESSIONEXPIRED);
                    break;
            }
        } else {
            if (null != path && path.equals(znode)) {
                zooKeeper.exists(znode, true, (AsyncCallback.StatCallback) this, null);

            }
        }
        if (null != chainedWatcher) {
            chainedWatcher.process(event);
        }
    }

    public void processResult (Code reasonCode, String path, Object ctx, Stat stat) {
        boolean exists;

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
                zooKeeper.exists(znode, true, (AsyncCallback.StatCallback) this, null);
                return;
        }
        byte[] b = null;
        if (exists) {
            try {
                b = zooKeeper.getData(znode, false, null);

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
