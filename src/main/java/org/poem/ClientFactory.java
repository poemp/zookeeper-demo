package org.poem;

import lombok.extern.java.Log;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.checkerframework.checker.units.qual.C;

import java.util.List;

@Log
public class ClientFactory {

    public static String date = "hello";

    public static String zkPath = "/test/CRUD/node-1";

    public static String connectionStr = "127.0.0.1:2181";

    public static CuratorFramework createSimple(String connectionStr) {

        //重试测量，第一次重试等待1s，第二次重试等待2s，第三次重试等待4s
        //第一个参数：等待时间的基础单位，单位是毫秒
        //第二个参数：最大的重试次数
        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(100, 3);

        //获取curatorframefactory实例的最简单方法
        //第一个参数是zk的链接地址
        //第二个参数是重试策略
        return CuratorFrameworkFactory.newClient(connectionStr, retry);
    }

    /**
     *
     * @return
     */
    public static CuratorFramework createSimple(){
        return createSimple(connectionStr);
    }


    /**
     * @param connectionStr       连接地址
     * @param retryPolicy         重试策略
     * @param connectionTimeoutMs 连接超时时间
     * @param sessionTimeoutMs    回话超时时间
     * @return
     */
    public static CuratorFramework createWithOptions(String connectionStr, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs) {
        return CuratorFrameworkFactory.builder()
                .connectString(connectionStr)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }


    /**
     * 创新节点
     */
    public void createNode() {
        CuratorFramework curatorFramework = ClientFactory.createSimple(connectionStr);

        try {
            curatorFramework.start();


            byte[] payload = date.getBytes();

            curatorFramework.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkPath, payload);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(curatorFramework);
        }
    }

    /**
     * 读取数据
     */
    public void readNode() {
        CuratorFramework client = ClientFactory.createSimple(connectionStr);
        try {
            client.start();
            Stat stat = client.checkExists().forPath(zkPath);
            if (null != stat) {
                byte[] payload = client.getData().forPath(zkPath);
                String data = new String(payload);
                log.info(data);


                String parentPath = "/test";
                List<String> clientChild = client.getChildren().forPath(parentPath);

                for (String child: clientChild){
                    log.info(child);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            CloseableUtils.closeQuietly(client);
        }
    }


    /**
     * 同步更新数据
     * 该操作是同步阻塞的
     */
    public void updateNode(){
        CuratorFramework client = ClientFactory.createSimple(connectionStr);
        try {
            client.start();

            String date = "Hello Zookeeper";
            byte[] payload = date.getBytes();

            client.setData()
                    .forPath(zkPath, payload);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            CloseableUtils.closeQuietly(client);
        }
    }
    public static void main(String[] args) {
        ClientFactory factory = new ClientFactory();
//        factory.createNode();
//        factory.readNode();
        factory.updateNode();

        CRUD crud = new CRUD();
        crud.updateNodeAsync();
    }
}
