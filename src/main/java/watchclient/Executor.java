package watchclient;

import io.netty.util.internal.SocketUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.Code;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {

    // default access privilege. same package. no sub-class
    String filename;
    String[] execStr;
    ZooKeeper zooKeeper;
    DataMonitor dataMonitor;
    Process child;

    // use
    public static void main (String[] args)  {
        /*
        if (args.length < 4) {
            System.err.println("USAGE: Executor hostPort zNode filename program [args ...]");
            System.exit(2);
        }
*/
        //String hostPort = args[0];
        //String zNodePath = args[1];
        //String filename = args[2];
        //String[] exec = new String[args.length - 3];
        String hostPort = "localhost:12181";
        String zNodePath = "/firstznode5";
        String filename = "/firstznode5/abcfile";
        String[] execStr = new String[3];
        execStr[0] = hostPort;
        execStr[1] = zNodePath;
        execStr[2] = filename;
        //System.arraycopy(args, 3, exec, 0, exec.length);

        try {
            new Executor(hostPort, zNodePath, filename, execStr).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * contains the main thread and contains the execution logic
     */
    public Executor (String hostPort, String zNodePath, String filename, String execStr[]) throws KeeperException, IOException, InterruptedException {
        System.out.println("Executor const ructor");
        this.filename = filename;
        this.execStr = execStr;
        /*
         * the address of the ZooKeeper service
         * the name of a znode - the one to be watched
         */
        // maintains the ZooKeeper connection
        // passes a reference to itself as the Watcher argument in the ZooKeeper constructor
        System.out.println("LL: passes a reference to itself as the Watcher argument in the ZooKeeper constructor");
        zooKeeper = new ZooKeeper(hostPort, 3000, this);

        /*
         * It fetches the data associated with the znode and starts the executable.
         * If the znode changes, the client re-fetches the contents and restarts the executable.
         * If the znode disappears, the client kills the executable.
         */
        // passes a reference to itself as DataMonitorListener argument to the DataMonitor constructor
        System.out.println("LL: passes a reference to itself as DataMonitorListener argument to the DataMonitor constructor");
        dataMonitor = new DataMonitor(zooKeeper, zNodePath, null, this);

    }

    public void run () {
        System.out.println("fbbbbbbbbbbbbbb");
        try {
            zooKeeper.create("/ftest3", "ftestdata".getBytes(UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            byte[] bData = zooKeeper.getData("/ftest3", this, null);
            System.out.println(new String(bData, UTF_8));
            System.out.println("ftttttttttttt");
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Executor run %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        try {
            synchronized (this) {
                while (!dataMonitor.dead) {
                    System.out.println("wwwwwwwwwwwwwwwwbbbbbbbbbbbb");
                    wait();
                    System.out.println("wwwwwwwwwwwwaaaaaaaaaaaaaaaaa");
                }
            }
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    /*
    * The Watcher interface is defined by the ZooKeeper Java API. ZooKeeper uses it to communicate back
    * to its container. It supports only one method, process(), and ZooKeeper uses it to communicates
    * generic events that the main thread would be interested in, such as the state of the ZooKeeper
    * connection or the ZooKeeper session.
    * */
    // The implementation  of the Watcher interface
    @Override
    public void process(WatchedEvent watchedEvent) {

        dataMonitor.process(watchedEvent);
        System.out.println("receive event >> : " + watchedEvent.getType().name());

    }

    //  Executor's implementation of DataMonitorListener.exists()
    @Override
    public void exists(byte[] data) {
        if (null != data) {
            if (null != child) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    // do nothing
                }
                child = null;
            }

        } else {
            if (null != child) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(filename);
                fileOutputStream.write(data);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(execStr);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Executor's implementation of DataMonitorListener.closing
    @Override
    public void closing(Code reasonCode) {
        synchronized (this) {
            notifyAll();
        }
    }

    static class StreamWriter extends Thread{
        OutputStream outputStream;
        InputStream inputStream;

        public StreamWriter(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            start();
        }

        public void run () {
            byte b[] = new byte[80];
            int numBytes;
            try {
                while ((numBytes = inputStream.read(b))>0) {
                    outputStream.write(b, 0, numBytes);
                }
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
