package org.poem.lock.distributedLock;

import java.util.List;

public interface Lock {

    /**
     * 加锁
     * @return
     */
    boolean lock();


    /**
     * 解锁方式
     * @return
     */
    boolean unlock();

    /**
     *
     * @return
     */
    boolean tryLock();


    /**
     * 判断是否加锁成功
     * @param waiters
     * @return
     */
    boolean checkLocked(List<String> waiters);
}
