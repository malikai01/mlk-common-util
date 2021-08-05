package com.mlk.util.resubmit;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author malikai
 * @date 2021年06月18日 13:33
 */
public class ResubmitLock {

    private static final ConcurrentHashMap<String, Object> LOCK_CACHE = new ConcurrentHashMap<>(200);
    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5, new ThreadPoolExecutor.DiscardPolicy());


    private ResubmitLock() {
    }

    /**
     * 静态内部类 单例模式
     *
     * @return
     */
    private static class SingletonInstance {
        private static final ResubmitLock INSTANCE = new ResubmitLock();
    }

    public static ResubmitLock getInstance() {
        return SingletonInstance.INSTANCE;
    }


    public static String handleKey(String param) {
        return DigestUtils.md5Hex(param == null ? "" : param);
    }

    /**
     * 加锁 putIfAbsent 是原子操作保证线程安全
     *
     * @param key   对应的key
     * @param value
     * @return
     */
    public boolean lock(final String key, Object value) {
        return Objects.isNull(LOCK_CACHE.putIfAbsent(key, value));
    }

    /**
     * 延时释放锁 用以控制短时间内的重复提交
     *
     * @param lock         是否需要解锁
     * @param key          对应的key
     * @param delaySeconds 延时时间
     */
    public void unLock(final boolean lock, final String key, final int delaySeconds) {
        if (lock) {
            EXECUTOR.schedule(() -> {
                LOCK_CACHE.remove(key);
            }, delaySeconds, TimeUnit.SECONDS);
        }
    }
}
