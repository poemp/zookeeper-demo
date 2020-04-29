package org.poem.publishSubscribe;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
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
public class ZkPathChangeWatcherDemo {

    private static final Logger logger = LoggerFactory.getLogger(ZkPathChangeWatcherDemo.class);


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


            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, workerPath, false);
            PathChildrenCacheListener listener = (curatorFramework, event) -> {
                ChildData data = event.getData();
                PathChildrenCacheEvent.Type type = event.getType();
                switch (type) {
                    case CHILD_ADDED:
                        logger.info("子节点增加,path={}, data = {}", data.getPath(), new String(data.getData() == null ? "".getBytes() :data.getData()));
                        break;
                    case CHILD_UPDATED:
                        logger.info("子节点更新 path={}, data={}", data.getPath(), new String(data.getData() == null ? "".getBytes() :data.getData()));
                        break;
                    case CHILD_REMOVED:
                        logger.info("子节点删除 path={}, data={}", data.getPath(),new String(data.getData() == null ? "".getBytes() :data.getData()));
                        break;
                    default:
                        break;
                }
            };
            pathChildrenCache.getListenable().addListener(listener);
            pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

            for (int i = 0; i < 10; i++) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(subWorkerPath + i,subWorkerPath.getBytes());
            }
            Thread.sleep(1000);

            for (int i = 0; i < 10; i++) {
                client.delete().forPath(subWorkerPath + i) ;
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    public static void main(String[] args) {
        ZkPathChangeWatcherDemo zkWatcherDemo = new ZkPathChangeWatcherDemo();
        zkWatcherDemo.testWatch();
    }
}
