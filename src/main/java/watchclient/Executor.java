package watchclient;

import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.*;

public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {

    // default access privilege. same package. no sub-class
    String filename;
    String[] execStr;
    ZooKeeper zooKeeper;
    DataMonitor dataMonitor;
    Process child;

    public static void main (String[] args)  {
        /*
        if (args.length < 4) {
            System.err.println("USAGE: Executor hostPort znode filename program [args ...]");
            System.exit(2);
        }
*/
        //String hostPort = args[0];
        //String znode = args[1];
        //String filename = args[2];
        //String[] exec = new String[args.length - 3];
        String hostPort = "localhost:12181";
        String znode = "/firstznode";
        String filename = "abcfile";
        String[] execStr = new String[3];
        execStr[0] = hostPort;
        execStr[1] = znode;
        execStr[2] = filename;
        //System.arraycopy(args, 3, exec, 0, exec.length);

        try {
            new Executor(hostPort, znode, filename, execStr).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Executor (String hostPort, String znode, String filename, String execStr[]) throws KeeperException, IOException {
        System.out.println("Executor const ructor");
        this.filename = filename;
        this.execStr = execStr;
        zooKeeper = new ZooKeeper(hostPort, 3000, this);
        dataMonitor = new DataMonitor(zooKeeper, znode, null, this);
    }

    public void run () {
        System.out.println("Executor run %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        try {
            synchronized (this) {
                while (!dataMonitor.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        dataMonitor.process(watchedEvent);
    }

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
