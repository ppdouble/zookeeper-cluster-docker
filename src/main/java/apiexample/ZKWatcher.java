package apiexample;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class ZKWatcher implements Watcher {
    CountDownLatch countDownLatch;

    public ZKWatcher () {
        countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("Watcher fired on path: "
                + watchedEvent.getPath() + " stat: "
                + watchedEvent.getState() + " type: "
                + watchedEvent.getType()
        );
        countDownLatch.countDown();

    }

    public void await () throws InterruptedException {
        countDownLatch.await();
    }
}
