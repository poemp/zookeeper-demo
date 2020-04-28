package org.poem.id;

import lombok.extern.java.Log;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.poem.ClientFactory;

/**
 * 生成分布式id
 */
@Log
public class IDMaker {

    CuratorFramework client;

    /**
     * 初始化
     */
    public void init() {
        client = ClientFactory.createSimple(ClientFactory.connectionStr);
        client.start();
    }

    /**
     * 关闭
     */
    public void close() {
        if (client != null) {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * 排序
     * @param pathPrefix
     * @return
     */
    private String createSquNode(String pathPrefix) {
        try {
            return client.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(pathPrefix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 设置id
     * @param nodeName
     * @return
     */
    private String makeId(String nodeName) {
        String str = createSquNode(nodeName);
        if (str == null) {
            return null;
        }

        int index = str.lastIndexOf(nodeName);
        if (index >= 0) {
            index += nodeName.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }

    public static void main(String[] args) {
        IDMaker idMaker = new IDMaker();
        idMaker.init();

        String nodeName = "/test/IDMaker/ID-";

        for (int i = 0; i < 10; i++) {
            String id = idMaker.makeId(nodeName);
            log.info( "第 " + i + "个创建的id为： " + id);
        }

        idMaker.close();
    }
}
