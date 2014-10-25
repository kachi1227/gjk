package com.gjk.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Session;
import com.gjk.Application;
import com.gjk.Constants;
import com.gjk.database.objects.GroupMember;
import com.google.common.collect.Sets;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author gpl
 */
public final class GeneralHelper {

    public static void reportMessage(Context ctx, String tag, String message) {
        reportMessage(ctx, tag, message,
                Application.get().getPreferences().getBoolean(Constants.PROPERTY_SETTING_SHOW_DEBUG_TOASTS,
                        Constants.PROPERTY_SETTING_SHOW_DEBUG_TOASTS_DEFAULT));
    }

    public static void reportMessage(Context ctx, String tag, String message, boolean showToast) {
        if (message != null) {
            Application.get().log(String.format("%s: %s", tag, message));
            Log.e(tag, String.format("%s: %s", getMethodName(1), message));
            if (ctx != null && showToast) {
                showLongToast(ctx, message);
            }
        }
    }

    public static String getMethodName(final int depth) {
        if (depth >= 0) {
            final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            return ste[0].getMethodName();
        }
        return "";
    }


    public static String getClassName(final int depth) {
        if (depth >= 0) {
            final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            return ste[ste.length - 1 - depth].getClassName();
        }
        return "";
    }

    public static void showLongToast(Context ctx, String message) {
        if (ctx != null & message != null) {
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static <T> T[] concat(T[] first, T[]... rest) {
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

    public static long[] concatLong(long[] first, long[]... rest) {
        return concatLong(convertLong(first), convertLong(rest[0]));
    }

    public static long[] concatLong(Long[] first, Long[]... rest) {
        Long[] array = concat(first, rest);
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                result[i] = array[i];
            }
        }
        return result;
    }

    public static Long[] convertLong(long[] array) {
        Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static long[] convertLong(Long[] array) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
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

    public static void showHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.gjk", PackageManager.GET_SIGNATURES); //Your package name here
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.v("KeyHash:", keyHash);
                showLongToast(context, keyHash);
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public static void logoutOfFacebook(Context context) {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (!session.isClosed()) {
                session.closeAndClearTokenInformation();
            }
        } else {
            session = new Session(context);
            Session.setActiveSession(session);
            session.closeAndClearTokenInformation();
        }
    }

    public static long[] diff(long[] array1, long[] array2) {
        final Set<Long> set1 = new HashSet<Long>();
        set1.addAll(Arrays.asList(convertLong(array1)));
        final Set<Long> set2 = new HashSet<Long>();
        set2.addAll(Arrays.asList(convertLong(array2)));
        final Set<Long> diffSet = Sets.difference(set1, set2);
        final Long[] diffArray = diffSet.toArray(new Long[diffSet.size()]);
        return convertLong(diffArray);
    }

    public static long[] getIdsFromGroupMembers(Set<GroupMember> members) {
        return getIdsFromGroupMembers(members.toArray(new GroupMember[members.size()]));
    }

    public static long[] getIdsFromGroupMembers(GroupMember[] members) {
        final long[] ids = new long[members.length];
        int i = 0;
        for (GroupMember gm : members) {
            ids[i] = gm.getGlobalId();
            i++;
        }
        return ids;
    }

    public static long[] getConvoIds(String convoString) {
        final JSONObject convos = splitConvos(convoString);
        final JSONArray convoNames = convos.names();
        final long[] ids = new long[convoNames == null ? 0 : convoNames.length()];
        for (int i = 0; i < ids.length; i++) {
            try {
                ids[i] = convos.getLong(convoNames.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ids;
    }

    public static JSONObject splitConvos(String convoString) {
        final String[] convosStrSplit = convoString.split("\\|");
        final JSONObject ret = new JSONObject();
        try {
            for (String convo : convosStrSplit) {
                final String[] convoStrSplit = convo.split(":");
                if (convoStrSplit.length > 1) {
                    final long id = Long.valueOf(convoStrSplit[0]);
                    final String name = convoStrSplit[1];
                    ret.put(name, id);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String[] getPhoneNumbersFromContacts(Context ctx) {
        final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        final Cursor cursor = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        final Set<String> numbers = Sets.newHashSet();
        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (number != null) {
                try {
                    Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number, "US");
                    numbers.add(String.valueOf(numberProto.getNationalNumber()));
                } catch (NumberParseException e) {
                    e.printStackTrace();
                }

            }
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }
}