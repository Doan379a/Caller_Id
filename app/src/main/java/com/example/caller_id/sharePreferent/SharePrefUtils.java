package com.example.caller_id.sharePreferent;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class SharePrefUtils {
    private static final String PREF_NAME = "data";
    private static SharedPreferences mPreferences;

    public static void init(Context context) {
        if (mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static boolean isRated(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("rated", false);
    }

    public static boolean isGoToMain(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("GoToMain", false);
    }

    public static void forceGoToMain(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("GoToMain", true);
        editor.commit();
    }

    public static void forceRated(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("rated", true);
        editor.commit();
    }

    public static boolean isHome(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("home", false);
    }

    public static void forceHome(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("home", true);
        editor.commit();
    }

    public static int getSplashOpenCount(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getInt("splash_open", 1);
    }

    public static void incrementSplashOpenCount(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        int currentCount = getSplashOpenCount(context);
        if (currentCount <= 10) {
            pre.edit().putInt("splash_open", currentCount + 1).apply();
        }
    }
    public static void saveSetting(Context context, String key,Boolean value) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        pre.edit().putBoolean(key, value).apply();
    }

    public static boolean getSetting(Context context, String key) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean(key, false);
    }

    public static void saveCountrySelection(Context context, String isoCode) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        pre.edit().putString("selected_country_iso", isoCode).apply();
    }

    public static String getSavedCountryIso(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getString("selected_country_iso", null);
    }

}
