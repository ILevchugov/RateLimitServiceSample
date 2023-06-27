package io.levchugov.sample.ratelimit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LockByKey {

    private static final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();

    public void lock(String key) {
        locks.compute(key, (k, v) -> v == null ? new ReentrantLock() : v).lock();
    }

    public void unlock(String key) {
        locks.get(key).unlock();
    }

}