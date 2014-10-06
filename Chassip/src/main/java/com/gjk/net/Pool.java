package com.gjk.net;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gjk.helper.GeneralHelper;

import java.util.concurrent.Semaphore;

import static com.gjk.Constants.CHASSIP_ACTION;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.MAX_SEMAPHORE_COUNT;
import static com.gjk.Constants.START_PROGRESS;
import static com.gjk.Constants.STOP_PROGRESS;

public class Pool {

    private final static String LOGTAG = "Pool";

    private final Semaphore available = new Semaphore(MAX_SEMAPHORE_COUNT, true);

    public Pool() {
        for (int i = 0; i < MAX_SEMAPHORE_COUNT; ++i) {
            items[i] = new Object();
            used[i] = false;
        }
    }

    public Object getItem(Context ctx, boolean showProgress) throws InterruptedException {
        if (showProgress) {
            final Intent i = new Intent(CHASSIP_ACTION);
            i.putExtra(INTENT_TYPE, START_PROGRESS);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(i);
        }
        available.acquire();
        Log.d(LOGTAG, String.format("%s is acquiring semaphore. Permits remaining=%d",
                GeneralHelper.getMethodName(1), available.availablePermits()));
        return getNextAvailableItem();
    }

    public void putItem(Context ctx, Object x) {
        if (markAsUnused(x)) {
            available.release();
            if (available.availablePermits() == MAX_SEMAPHORE_COUNT) {
                Intent i = new Intent(CHASSIP_ACTION);
                i.putExtra(INTENT_TYPE, STOP_PROGRESS);
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(i);
            }
            Log.d(LOGTAG, String.format("%s is releasing semaphore. Permits remaining=%d",
                    GeneralHelper.getMethodName(1), available.availablePermits()));
        }
    }

    // Not a particularly efficient data structure; just for demo

    protected Object[] items = new Object[MAX_SEMAPHORE_COUNT];
    protected boolean[] used = new boolean[MAX_SEMAPHORE_COUNT];

    protected synchronized Object getNextAvailableItem() {
        for (int i = 0; i < MAX_SEMAPHORE_COUNT; ++i) {
            if (!used[i]) {
                used[i] = true;
                return items[i];
            }
        }
        return null; // not reached
    }

    protected synchronized boolean markAsUnused(Object item) {
        for (int i = 0; i < MAX_SEMAPHORE_COUNT; ++i) {
            if (item == items[i]) {
                if (used[i]) {
                    used[i] = false;
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }

}