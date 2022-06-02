package apiexample;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ZKManager {

    // public void init () throws IOException, InterruptedException;
    /*
     * create zNode
     */
    public void create (String zNodePath, byte[] data) throws InterruptedException, KeeperException;

    public Stat getZNodeStats (String zNodePath) throws InterruptedException, KeeperException;

    public String getZNodeData (String zNodePath, boolean watchFlag) throws InterruptedException, KeeperException, UnsupportedEncodingException;

    public void setZNodeData (String zNodePath, byte[] data) throws InterruptedException, KeeperException;

    public Stat ZNodeExists (String zNodePath) throws InterruptedException, KeeperException;

    public List<String> ZKGetChildren (String zNodePath, boolean watchFlag) throws InterruptedException, KeeperException;

    public void ZKDeleteZNode (String zNodepath) throws InterruptedException, KeeperException;


}
