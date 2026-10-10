package com.kinetic.concurrent;

import com.kinetic.dao.AuditDao;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Producer/consumer audit trail. Request threads call {@link #log} and return immediately; a
 * single daemon worker thread drains the {@link BlockingQueue} and writes to the database, so
 * a slow insert never delays a page. {@link #close()} flushes what is left before shutdown.
 */
public final class AuditLogger implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(AuditLogger.class.getName());

    private record Entry(Long userId, String event, String detail) {
    }

    private static final Entry POISON = new Entry(null, "STOP", null);

    private final AuditDao dao;
    private final BlockingQueue<Entry> queue = new LinkedBlockingQueue<>(10_000);
    private final Thread worker;

    public AuditLogger(AuditDao dao) {
        this.dao = dao;
        this.worker = new Thread(this::drain, "kinetic-audit");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    /** Non-blocking. Drops the entry if the queue is full rather than slowing the caller. */
    public void log(Long userId, String event, String detail) {
        if (!queue.offer(new Entry(userId, event, detail))) {
            LOG.warning("Audit queue full; dropped " + event);
        }
    }

    private void drain() {
        try {
            while (true) {
                Entry e = queue.take();
                if (e == POISON) {
                    return;
                }
                try {
                    dao.insert(e.userId(), e.event(), e.detail());
                } catch (RuntimeException ex) {
                    LOG.log(Level.WARNING, "Could not write audit entry " + e.event(), ex);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        queue.offer(POISON);
        try {
            worker.join(TimeUnit.SECONDS.toMillis(5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
