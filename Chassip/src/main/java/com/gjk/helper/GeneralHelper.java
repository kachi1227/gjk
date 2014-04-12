package com.gjk.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 *
 * @author gpl
 */
public final class GeneralHelper {

    public static void reportMessage(Context ctx, String tag, String message) {
        if (message != null) {
            Log.e(tag, String.format(Locale.getDefault(), "%s: %s", getMethodName(2), message));
            if (ctx != null) {
                showLongToast(ctx, message);
            }
        }
    }

    public static String getMethodName(final int depth) {
        if (depth >= 0) {
            final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            return ste[ste.length - 1 - depth].getMethodName();
        }
        return "";
    }

    public static void showLongToast(Context ctx, String message) {
        if (ctx != null & message != null) {
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
        }
    }
}