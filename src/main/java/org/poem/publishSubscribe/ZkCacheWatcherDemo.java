package org.poem.publishSubscribe;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.poem.ClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class ZkCacheWatcherDemo {

    private static final Logger logger = LoggerFactory.getLogger(ZkCacheWatcherDemo.class);

    
    private String workerPath = "/test/cacheListener/remoteNode";

    private String subWorkerPath = "/test/cacheListener/remoteNode/id-";


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


            final NodeCache nodeCache = new NodeCache(client, workerPath, false);

            NodeCacheListener cacheListener = new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    ChildData childData = nodeCache.getCurrentData();
                    if (childData != null) {
                        logger.info("ZNode 节点状态改变，path={}", childData.getPath());
                        logger.info("ZNode 节点状态改变, data={}", new java.lang.String(childData.getData()));
                        logger.info("ZNode 节点状态改变, stat={}", childData.getStat());
                    }
                }
            };

            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, workerPath, true);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
                    ChildData data = event.getData();
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            logger.info("子节点增加, path={}, data={}", data.getPath(), data.getData());
                            break;
                        case CHILD_UPDATED:
                            logger.info("子节点更新, path={}, data={}", data.getPath(), data.getData());
                            break;
                        case CHILD_REMOVED:
                            logger.info("子节点删除, path={}, data={}", data.getPath(), data.getData());
                            break;
                        default:
                            break;
                    }
                }
            };
            //启动路径监听
            pathChildrenCache.getListenable().addListener(childrenCacheListener);
            pathChildrenCache.start(PathChildrenCache.StartMode.NORMAL);
            //启动节点的事件监听
            nodeCache.getListenable().addListener(cacheListener);
            nodeCache.start(true);

            //第一次节点数据变化
            System.out.println("第一次更改内容");
            client.setData().forPath(workerPath, "第一次更改内容".getBytes());
            Thread.sleep(1000);

            //第二次节点数据变化
            System.out.println("第二次更改内容");
            client.setData().forPath(workerPath, "第二次更改内容".getBytes());
            Thread.sleep(1000);

            //第三次更改节点内容
            System.out.println("第三次更改内容");
            client.setData().forPath(workerPath, "第三次更改内容".getBytes());
            Thread.sleep(1000);

            //第四次节点更改内容
            System.out.println("第四次更改内容");
            client.setData().forPath(workerPath, "第四次更改内容".getBytes());
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
        ZkCacheWatcherDemo zkWatcherDemo = new ZkCacheWatcherDemo();
        zkWatcherDemo.testWatch();
    }
}
