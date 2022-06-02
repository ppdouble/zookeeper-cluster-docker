package apiexample;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Test {
    public static void main (String[] args) {
        ZKConnection zkConnection = new ZKConnection();
        String zNodePath = "/firstdata";
        byte[] bData = "testdata1".getBytes(UTF_8);
        try {
            ZooKeeper zooKeeper = zkConnection.connect("localhost:12181");
            ZKManager zkManager = new ZKManagerImpl(zooKeeper);

            System.out.println("test create");
            zkManager.create(zNodePath, bData);
            Stat stat = zkManager.getZNodeStats(zNodePath);
            if (null != stat) {
                System.out.println("get node data: " + zkManager.getZNodeData(zNodePath, false));
                bData = "newtestdata1".getBytes(UTF_8);
                zkManager.setZNodeData(zNodePath, bData);
                System.out.println("get node new data: " + zkManager.getZNodeData(zNodePath, false));
                zkManager.create(zNodePath + "/mychildren", "childdata".getBytes());
                zkManager.create(zNodePath + "/mychildren" + "/mygrandchildren", "grandchilddata".getBytes());

                System.out.println(zkManager.getZNodeStats(zNodePath + "/mychildren"));
                System.out.println(zkManager.getZNodeData(zNodePath + "/mychildren", false));
                System.out.println(zkManager.getZNodeData(zNodePath +  "/mychildren" + "/mygrandchildren", false));

            }
            zkManager.ZKDeleteZNode(zNodePath + "/mychildren" + "/mygrandchildren");
            zkManager.ZKDeleteZNode(zNodePath + "/mychildren");
            zkManager.ZKDeleteZNode(zNodePath);
            zkConnection.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
