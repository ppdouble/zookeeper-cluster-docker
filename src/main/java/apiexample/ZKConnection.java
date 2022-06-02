package apiexample;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/*
 * Create connection to ZooKeeper ensemble
 *
 * @author biu George
 * @since 2016
 * @version 1.0
 * http://www.java.globinch.com. All rights reserved
 *
 * @modify by nickdong 2022
 */
public class ZKConnection {

    private ZooKeeper zoo;
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    // NoArgs constructor
    public ZKConnection () {}

    /*
     * create connection to ZooKeeper ensemble
     * host  e.g localhost:12181
     */
    public ZooKeeper connect (String host) throws IOException, InterruptedException {
        // new Watcher() {} is an anonymous inner class, which
        // implements an interface. It is an expression, which looks like a constructor.
        // It includes a class definition body.
        zoo = new ZooKeeper(host, 2000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                    // Decrements the count of the latch, releasing all waiting
                    // threads if the count reaches zero.
                    countDownLatch.countDown();
                }
            }
        });

        // Causes the current thread to wait until the latch has counted down to zero,
        // unless the thread is interrupted.
        countDownLatch.await();
        return zoo;
    }

    // disconnect from ZooKeeper ensemble
    public void close () throws InterruptedException {
        zoo.close();
    }
}
