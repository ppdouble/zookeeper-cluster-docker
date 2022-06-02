package apiexample;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ZKManagerImpl implements ZKManager{
    private ZooKeeper zooKeeper;

    public ZKManagerImpl(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void create(String zNodePath, byte[] data) throws InterruptedException, KeeperException {
        zooKeeper.create(zNodePath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Override
    public Stat getZNodeStats(String zNodePath) throws InterruptedException, KeeperException {

        Stat stat = ZNodeExists(zNodePath);
        if (null != stat) {
            System.out.println("Node exists and the node version is " + stat.getVersion());
        } else {
            System.out.println("Node does not exists.");
        }
        return stat;
    }

    @Override
    public String getZNodeData(String zNodePath, boolean watchFlag) throws InterruptedException, KeeperException, UnsupportedEncodingException {
        Stat stat = ZNodeExists(zNodePath);
        byte[] bData = null;
        String data = null;
        if (null != stat) {
            if (watchFlag) {
                ZKWatcher zkWatcher = new ZKWatcher();
                bData = zooKeeper.getData(zNodePath, zkWatcher, stat);
                zkWatcher.await();
            } else {
                bData = zooKeeper.getData(zNodePath, null, null);
            }

        } else {
            System.out.println("Node does not exists.");
        }
        data = new String(bData,"UTF-8");
        return data;
    }

    @Override
    public void setZNodeData(String zNodePath, byte[] data) throws InterruptedException, KeeperException {
        zooKeeper.setData(zNodePath, data, ZNodeExists(zNodePath).getVersion());
    }

    @Override
    public Stat ZNodeExists(String zNodePath) throws InterruptedException, KeeperException {
        return zooKeeper.exists(zNodePath, true);
    }

    @Override
    public List<String> ZKGetChildren(String zNodePath, boolean watchFlag) throws InterruptedException, KeeperException {
        Stat stat = ZNodeExists(zNodePath);
        List<String> children = null;
        if (null != stat) {
            children = zooKeeper.getChildren(zNodePath, watchFlag);
            for (String child : children) {
                System.out.println(child);
            }
        } else {
            System.out.println("Node does not exists");
        }
        return children;
    }

    @Override
    public void ZKDeleteZNode(String zNodepath) throws InterruptedException, KeeperException {
        if (null == ZNodeExists(zNodepath)) {
            System.out.println("No Exists");
        } else {
            zooKeeper.delete(zNodepath, ZNodeExists(zNodepath).getVersion());
        }

    }

}
