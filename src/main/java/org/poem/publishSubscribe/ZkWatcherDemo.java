package org.poem.publishSubscribe;

import com.sun.security.ntlm.Client;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.client.ZKClientConfig;
import org.apache.zookeeper.data.Stat;
import org.poem.ClientFactory;

@Slf4j
@Data
public class ZkWatcherDemo {

    private String  workerPath = "/test/listener/remoteNode";

    private String subWorkerPath = "/test/listener/remoteNode/id-";


    public void testWatch(){
        CuratorFramework client = ClientFactory.createSimple();

       try{
           client.start();
           Stat stat = client.checkExists().forPath(workerPath);
           if (stat == null){
               client.create()
                       .creatingParentContainersIfNeeded()
                       .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                       .forPath(workerPath);
           }

           try {
               Watcher watcher = new Watcher() {
                   public void process(WatchedEvent watchedEvent) {
                       System.out.println("监听到的变化WatchEvent = " + watchedEvent );
                   }
               };
               byte[] content = client.getData()
                       .usingWatcher(watcher).forPath(workerPath);

               log.info("监听到节点内容:" + new String(content));

               client.setData().forPath(workerPath, "第一次更改内容".getBytes());
               client.setData().forPath(workerPath, "第二次更改内容".getBytes());

               Thread.sleep(Integer.MAX_VALUE);

           }catch (Exception e){
               e.printStackTrace();
           }
       }catch (Exception e){
           e.printStackTrace();
       }finally {
           CloseableUtils.closeQuietly(client);
       }
    }

    public static void main(String[] args) {
        ZkWatcherDemo zkWatcherDemo = new ZkWatcherDemo();
        zkWatcherDemo.testWatch();
    }
}
