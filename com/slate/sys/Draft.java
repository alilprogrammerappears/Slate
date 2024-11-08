package com.slate.sys;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public abstract class Draft {
    public static final ScheduledExecutorService DRAFT_UPDATER = new ScheduledThreadPoolExecutor(1);
    private static final HashMap<Integer, Draft> DRAFTS = new HashMap<>();

    public static final int DEFAULT_DURATION_HOURS = 1;

    private static int idCounter = 0;
    private final int id;
    private LocalDateTime expiryDate;

    private Draft (LocalDateTime expiryDate) {
        this.id = idCounter++;
        this.expiryDate = expiryDate;

        DRAFTS.put(this.id, this);
    }
    protected Draft () {
        this(LocalDateTime.now().plusHours(DEFAULT_DURATION_HOURS));
    }

    public void refresh () {
        expiryDate = LocalDateTime.now().plusHours(DEFAULT_DURATION_HOURS);
    }
    public static void remove (int id) {
        DRAFTS.remove(id);
    }
    public abstract void expire ();

    public static <T extends Draft> Optional<T> get (Class<T> c, int id) {
        Draft draft = DRAFTS.get(id);
        if (c.isInstance(draft)) {
            return Optional.of(c.cast(draft));
        }
        else {
            return Optional.empty();
        }
    }
    public int getId () {
        return id;
    }
    public boolean isExpired () {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    // Thread code
    public static final Runnable REMOVE_EXPIRED_DRAFTS = () ->
        DRAFTS.keySet().removeIf(id -> {
            Draft draft = DRAFTS.get(id);
            draft.expire();
            return draft.isExpired();
        });
}
