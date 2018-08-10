package com.app.okhttpparsing.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;

import com.squareup.okhttp.MediaType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Constant {


    public static String dateFormater(String dateFromJSON, String expectedFormat, String oldFormat) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(oldFormat);
        Date date = null;
        String convertedDate = null;
        try {
            date = dateFormat.parse(dateFromJSON);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(expectedFormat);
            convertedDate = simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    public static int getWidth(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    public static int getHieght(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int hieght = size.y;
        return hieght;
    }

    public static boolean checkNotifDate(Date notifDate, Date currentDate) {
        long diff = currentDate.getTime() - notifDate.getTime();
        AppLog.LogE("DateDifference", "--" + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) == 0;
    }

    public static Date getformattedDate(String date, String formate) {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        Date notifDate = null;
        try {
            notifDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return notifDate;
    }
    /*public static boolean isInternetAvailable(Context context, boolean toast) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                if (toast) {
                    Toast.makeText(context, "" + context.getResources().getString(R.string.toast_no_internet), Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/


    public static boolean isInternetAvailable(Context context) {
        ConnectionDetector cd = new ConnectionDetector(context);
        Boolean isInternetPresent = cd.isConnectingToInternet();

        if (!isInternetPresent) {
            // Toast.makeText(context,"Internet connection is not available.",Toast.LENGTH_LONG).show();
            return false;
        } else {
            return isInternetPresent;
        }
    }

    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }


    public static int getSpecificDate(String date, int value) {
        String data[] = date.split("-");
        String subdata[] = data[data.length - 1].split(" ");
        /*AppLog.LogE("subdata","--"+subdata[0]);
        for (int i = 0; i < data.length; i++) {

            AppLog.LogE("Year","--"+data[i]);
        }*/

        int parsed;
        if (value == 0) {
            parsed = Integer.valueOf(data[0]);
            return parsed;
        }
        if (value == 1) {
            parsed = Integer.valueOf(data[1]);
            return parsed;
        }
        if (value == 2) {
            parsed = Integer.valueOf(subdata[0]);
            return parsed;
        }
        return 0;
    }


    public static class Type {
        public static String post = "POST";
        public static String get = "GET";
    }


    public static class WebServicesKeys {
        public static String mPostName = "name";
        public static String mPostJob = "job";
        public static String mMultiPartFile = "file";
    }


    public static class Urls {

        public static String strGetURL = "https://reqres.in/api/users/2";
        public static String strPostURL = "https://reqres.in/api/users/";
        public static String strMultiPartURL = "http://mushtaq.16mb.com/retrofit_example/upload_image.php";


    }
}
