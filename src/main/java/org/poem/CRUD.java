package org.poem;

import com.sun.security.ntlm.Client;
import lombok.extern.java.Log;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.AsyncCallback;

@Log
public class CRUD {

    public void updateNodeAsync(){
        CuratorFramework client = ClientFactory.createSimple(ClientFactory.connectionStr);
        try {
            //异步更新完成，回调次实例
            AsyncCallback.StringCallback callback =
                    new AsyncCallback.StringCallback() {
                        public void processResult(int i, String s, Object o, String s1) {
                            log.info(
                                    " i = " + i + "  |  "+
                                            "s = " + s + " | "+
                                            "o = " + o + " | "+
                                            "s1 = " + s1
                            );
                        }
                    };
            client.start();

            String date = "Hello , every body";
            byte[] payload = date.getBytes();
            client.setData()
                    .inBackground(callback)
                    .forPath(ClientFactory.zkPath, payload);

            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            CloseableUtils.closeQuietly(client);
        }
    }
}
