package com.gjk.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
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

    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}