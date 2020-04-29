package org.poem.publishSubscribe;

import lombok.Data;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.poem.ClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 只能监听子节点的变化
 * 不能监听自身的变化
 */
@Data
public class ZkTreeCacheChangeWatcherDemo {

    private static final Logger logger = LoggerFactory.getLogger(ZkTreeCacheChangeWatcherDemo.class);


    private String workerPath = "/test/treeCacheNode/remoteNode";

    private String subWorkerPath = "/test/treeCacheNode/remoteNode/id-";


    public void testWatch() {
        CuratorFramework client = ClientFactory.createSimple();

        try {
            client.start();
            Stat stat = client.checkExists().forPath(workerPath);
            if (stat == null) {
                client.create()
                        .creatingParentContainersIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(workerPath);
            }


            TreeCache treeCache = new TreeCache(client, workerPath);
            TreeCacheListener treeCacheListener = new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                    ChildData data = treeCacheEvent.getData();
                    if (data == null) {
                        logger.info("数据为空");
                    }
                    TreeCacheEvent.Type type = treeCacheEvent.getType();
                    switch (type) {
                        case NODE_ADDED:
                            logger.info("子节点增加,path={}, data = {}", data.getPath(), new String(data.getData() == null ? "".getBytes() : data.getData()));
                            break;
                        case NODE_UPDATED:
                            logger.info("子节点更新 path={}, data={}", data.getPath(), new String(data.getData() == null ? "".getBytes() : data.getData()));
                            break;
                        case NODE_REMOVED:
                            logger.info("子节点删除 path={}, data={}", data.getPath(), new String(data.getData() == null ? "".getBytes() : data.getData()));
                            break;
                        case CONNECTION_LOST:
                            logger.info("Connection is Lost");
                            break;
                        case INITIALIZED:
                            logger.info("init zookeeper  is Lost");
                            break;
                        case CONNECTION_RECONNECTED:
                            logger.info("reconnetion .... ");
                            break;
                    }
                }
            };
            treeCache.getListenable().addListener(treeCacheListener);
            treeCache.start();
            Thread.sleep(1000);

            //创建节点
            for (int i = 0; i < 10; i++) {
                val stat1 = client.checkExists().forPath(subWorkerPath + i);
                if (stat1 == null) {
                    client.create().withMode(CreateMode.EPHEMERAL).forPath(subWorkerPath + i, subWorkerPath.getBytes());
                }
            }


            //更新节点
            for (int i = 0; i < 10; i++) {
                val stat1 = client.checkExists().forPath(subWorkerPath + i);
                if (stat1 == null) {
                    client.setData().forPath(subWorkerPath + i, (subWorkerPath + i).getBytes());
                }
            }


            //删除节点
            Thread.sleep(1000);
            for (int i = 0; i < 10; i++) {
                val stat1 = client.checkExists().forPath(subWorkerPath + i);
                if (stat1 != null) {
                    client.delete().forPath(subWorkerPath + i);
                }
            }

            Thread.sleep(1000);
            client.delete().forPath(workerPath);


            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    public static void main(String[] args) {
        ZkTreeCacheChangeWatcherDemo zkWatcherDemo = new ZkTreeCacheChangeWatcherDemo();
        zkWatcherDemo.testWatch();
    }
}
