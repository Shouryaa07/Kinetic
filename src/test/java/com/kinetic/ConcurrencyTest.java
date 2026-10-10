package com.kinetic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.kinetic.concurrent.OnlineUsers;
import com.kinetic.util.LruCache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class ConcurrencyTest {
    @Test
    public void auditLoggerWritesEveryEntryFromManyThreads() throws Exception {
        AppContext app = TestSupport.newApp();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            futures.add(pool.submit(() -> {
                for (int i = 0; i < 50; i++) {
                    app.audit.log(null, "STRESS", "x");
                }
            }));
        }
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        pool.shutdown();
        app.audit.close(); // flushes the queue
        assertEquals(400, app.auditDao.count("STRESS"));
        app.db.close();
    }

    @Test
    public void connectionPoolSharedByManyThreads() throws Exception {
        AppContext app = TestSupport.newApp();
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch done = new CountDownLatch(64);
        for (int i = 0; i < 64; i++) {
            pool.submit(() -> {
                try {
                    app.users.findAll();
                } finally {
                    done.countDown();
                }
            });
        }
        assertTrue(done.await(15, TimeUnit.SECONDS));
        pool.shutdown();
        app.close();
    }

    @Test
    public void lruCacheEvictsLeastRecentlyUsed() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");
        cache.put("c", 3);
        assertTrue(cache.get("a").isPresent());
        assertTrue(cache.get("b").isEmpty());
        assertEquals(2, cache.size());
    }

    @Test
    public void onlineUsersIsThreadSafe() throws Exception {
        OnlineUsers online = new OnlineUsers();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 1000; i++) {
            long id = i % 100;
            pool.submit(() -> online.signedIn(id));
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(100, online.count());
    }
}
