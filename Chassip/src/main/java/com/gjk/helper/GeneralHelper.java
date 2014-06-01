package com.gjk.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gjk.Application;
import com.gjk.Constants;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author gpl
 */
public final class GeneralHelper {

    public static void reportMessage(Context ctx, String tag, String message) {
        if (message != null) {
            Log.e(tag, String.format(Locale.getDefault(), "%s: %s", getMethodName(2), message));
            if (ctx != null && Application.get().getPreferences().getBoolean(Constants.PROPERTY_SETTING_SHOW_DEBUG_TOASTS,
                    Constants.PROPERTY_SETTING_SHOW_DEBUG_TOASTS_DEFAULT)) {
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

    public static void setInterleavingPref(boolean set) {
        Application.get().getPreferences().edit().putBoolean(Constants.PROPERTY_SETTING_INTERLEAVING, set).commit();
    }

    public static boolean getInterleavingPref() {
        return Application.get().getPreferences().getBoolean(Constants.PROPERTY_SETTING_INTERLEAVING,
                Constants.PROPERTY_SETTING_INTERLEAVING_DEFAULT);
    }

    public static void setKachisCachePref(boolean set) {
        Application.get().getPreferences().edit().putBoolean(Constants.PROPERTY_SETTING_USE_KACHIS_CACHE, set).commit();
    }

    public static boolean getKachisCachePref() {
        return Application.get().getPreferences().getBoolean(Constants.PROPERTY_SETTING_USE_KACHIS_CACHE,
                Constants.PROPERTY_SETTING_USE_KACHIS_CACHE_DEFAULT);
    }

    public static void setShowDebugToastsPref(boolean set) {
        Application.get().getPreferences().edit().putBoolean(Constants.PROPERTY_SETTING_SHOW_DEBUG_TOASTS, set).commit();
    }

    public static boolean getShowDebugToastsPref() {
        return Application.get().getPreferences().getBoolean(Constants.PROPERTY_SETTING_SHOW_DEBUG_TOASTS,
                Constants.PROPERTY_SETTING_SHOW_DEBUG_TOASTS_DEFAULT);
    }

    public static void setCirclizeMemberAvisPref(boolean set) {
        Application.get().getPreferences().edit().putBoolean(Constants.PROPERTY_SETTING_CIRCLIZE_MEMBER_AVIS, set).commit();
    }

    public static boolean getCirclizeMemberAvisPref() {
        return Application.get().getPreferences().getBoolean(Constants.PROPERTY_SETTING_CIRCLIZE_MEMBER_AVIS,
                Constants.PROPERTY_SETTING_CIRCLIZE_MEMBER_AVIS_DEFAULT);
    }
}