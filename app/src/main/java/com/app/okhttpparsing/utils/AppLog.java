package com.app.okhttpparsing.utils;

import android.util.Log;


public class AppLog {

    private static boolean isProd = false;

    public static void LogD(String tag, String message) {
        if (!isProd) {
            Log.d(tag, message);
        }
    }

    public static void LogE(String tag, String message) {
        if (!isProd) {
            Log.e(tag, message);
        }
    }

    public static void LogW(String tag, String message) {
        if (!isProd) {
            Log.d(tag, message);
        }
    }

    public static void LogI(String tag, String message) {
        if (!isProd) {
            Log.i(tag, message);
        }
    }
}
