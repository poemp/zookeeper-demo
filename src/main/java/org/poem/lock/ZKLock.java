//package org.poem.lock;
//
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.recipes.cache.ChildData;
//import org.apache.curator.framework.recipes.cache.TreeCache;
//import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
//import org.apache.curator.framework.recipes.cache.TreeCacheListener;
//import org.apache.zookeeper.CreateMode;
//import org.apache.zookeeper.WatchedEvent;
//import org.apache.zookeeper.Watcher;
//import org.apache.zookeeper.data.Stat;
//import org.checkerframework.checker.units.qual.C;
//import org.poem.ClientFactory;
//import org.poem.lock.distributedLock.Lock;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.locks.Condition;
//
//@Slf4j
//@Data
//public class ZKLock implements Lock {
//
//
//    private static final String ZK_PATH= "/test/lock";
//
//    private static final String LOCK_PREFIX = ZK_PATH + "/";
//
//    private static final long WAIT_TIME= 1000L;
//
//    CuratorFramework client = null;
//    private String lock_short_path = null;
//    private String lock_path = null;
//    private String prior_path = null;
//    final AtomicInteger lockCount = new AtomicInteger(0);
//
//    private Thread thread;
//
//    public ZKLock(){
//        CuratorFramework client = ClientFactory.createSimple();
//
//        try {
//            client.start();
//            Stat stat = client.checkExists().forPath(ZK_PATH);
//            if (stat == null) {
//                client.create()
//                        .creatingParentContainersIfNeeded()
//                        .withMode(CreateMode.PERSISTENT)
//                        .forPath(ZK_PATH);
//            }
//            this.client = client;
//        }catch (Exception e){
//            e.printStackTrace();
//            log.error(e.getMessage() ,e);
//        }
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public boolean lock() {
//        synchronized (this){
//            if (lockCount.get() == 0){
//                thread = Thread.currentThread();
//                lockCount.incrementAndGet();
//            }else {
//                if (!thread.equals(Thread.currentThread())){
//                    return false;
//                }
//                lockCount.incrementAndGet();
//                return true;
//            }
//        }
//        try {
//            boolean locked = false;
//
//            locked = tryLock();
//            if (locked){
//                return true;
//            }
//            while (!locked){
//                await();
//
//                List<String> waiters = getWaiters();
//                if (checkLocked(waiters)){
//                    locked = true;
//                }
//                return true;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            unlock();
//        }
//        return false;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public boolean unlock() {
//        return false;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public boolean tryLock() {
//       try {
//           lock_path = client.create().withMode(CreateMode.EPHEMERAL).forPath(LOCK_PREFIX);
//           if (lock_path ==null){
//               throw  new Exception("zk error");
//           }
//           lock_short_path = getShortPath(lock_path);
//           List<String> waiters = getWaiters();
//           if (checkLocked(waiters)){
//               return  true;
//           }
//
//           int index = Collections.binarySearch(waiters, lock_short_path);
//           if (index < 0){
//               throw new Exception("节点诶后找到:" + lock_short_path);
//           }
//
//           //如果自己么有读取锁
//           prior_path = ZK_PATH + "/" + waiters.get(index -1);
//           return false;
//       }catch (Exception e){
//           e.printStackTrace();
//       }
//        return false;
//    }
//
//    /**
//     *
//     * @param waiters
//     * @return
//     */
//    @Override
//    public boolean checkLocked(List<String> waiters) {
//        Collections.sort(waiters);
//        if (lock_short_path.equals(waiters.get(0))){
//            log.info("成功获取分布式锁，节点是：" + lock_short_path);
//            return true;
//        }
//        return false;
//    }
//
//
//    /**
//     * 等待
//     * 监听前一个节点的删除事件
//     * @throws Exception
//     */
//    private void await() throws Exception{
//        if (null == prior_path){
//            throw  new Exception("prior_path is null");
//        }
//        final CountDownLatch latch = new CountDownLatch(1);
//
//        Watcher watcher  = new Watcher() {
//            @Override
//            public void process(WatchedEvent watchedEvent) {
//                System.out.println("监听事件的变化 watherEvent=" + watchedEvent);
//                log.info("【watherEvent 节点删除】");
//                latch.countDown();
//            }
//        };
//
//        //开始监听
//        client.getData().usingWatcher(watcher).forPath(prior_path);
//
//        //监听当时二 ，使用treeNode
//        //订阅比自己小的节点的删除事件
//        TreeCache treeCache = new TreeCache(client, prior_path);
//        TreeCacheListener listener = new TreeCacheListener() {
//            @Override
//            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
//                ChildData childData = treeCacheEvent.getData();
//                if (childData != null){
//                    switch (treeCacheEvent.getType()){
//                        case  NODE_REMOVED:
//                            log.debug("[TreeCache] 节点删除,path={}, data={}", childData.getPath(), childData.getData());
//                            latch.countDown();
//                        default:
//                            break;
//                    }
//                }
//            }
//        };
//        //开始监听
//        treeCache.getListenable().addListener(listener);
//        treeCache.start();
//        //等待，最长加锁事件是 3s
//        latch.wait(3000);
//    }
//}
