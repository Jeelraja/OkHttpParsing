package com.app.okhttpparsing.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class AppSharedPref {

    public static String loginTokenKey = "loginToken";
    private static AppSharedPref mSharedPreferenceUtils;
    public SharedPreferences mSharedPreferences;
    protected Context mContext;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    private AppSharedPref(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences("eMeetingPreferences", Context.MODE_PRIVATE);
        mSharedPreferencesEditor = mSharedPreferences.edit();

    }

    /**
     * Creates single instance of AppSharedPref
     * *
     * * @param context context of Activity or Service
     * * @return Returns instance of AppSharedPref
     */
    public static synchronized AppSharedPref getInstance(Context context) {
        if (mSharedPreferenceUtils == null) {
            if (context != null) {
                mSharedPreferenceUtils = new AppSharedPref(context);
            }
        }
        return mSharedPreferenceUtils;
    }

    /**
     * Stores String value in preference      *
     * * @param key   key of preference
     * * @param value value for that key
     */
    public void setDataString(String key, String value) {
        mSharedPreferencesEditor.putString(key, value);
        mSharedPreferencesEditor.commit();
    }

    public void setDataInt(String key, int value) {
        mSharedPreferencesEditor.putInt(key, value);
        mSharedPreferencesEditor.commit();
    }

    public void setDataBoolean(String key, boolean value) {
        mSharedPreferencesEditor.putBoolean(key, value);
        mSharedPreferencesEditor.commit();
    }



    public String getDataString(String key) {
        return mSharedPreferences.getString(key, "");
    }

    public boolean getDataBoolean(String keyFlag) {
        return mSharedPreferences.getBoolean(keyFlag, false);
    }

    public void removeKey(String key) {
        if (mSharedPreferencesEditor != null) {
            mSharedPreferencesEditor.remove(key);
            mSharedPreferencesEditor.commit();
        }
    }

    /**
     * Clears all the preferences stored
     */
    public void clear() {
        mSharedPreferencesEditor.clear().commit();
    }

    public boolean getSaveLogedIn() {
        return mSharedPreferences.getBoolean("saveLogIn", false);
    }

    public void setSaveLogedIn(Boolean IsComplete) {
        mSharedPreferences.edit().putBoolean("saveLogIn", IsComplete).commit();
    }


    public void setBorderWidth(String key, int value) {
        mSharedPreferencesEditor.putInt(key, value);
        mSharedPreferencesEditor.commit();
    }

    public int getBorderWidth(String key) {
        return mSharedPreferences.getInt(key, 3);
    }
}