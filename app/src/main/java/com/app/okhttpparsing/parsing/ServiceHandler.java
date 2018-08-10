package com.app.okhttpparsing.parsing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.app.okhttpparsing.R;
import com.app.okhttpparsing.utils.AppSharedPref;
import com.app.okhttpparsing.utils.CommonUtil;
import com.app.okhttpparsing.utils.Constant;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

/**
 * ServiceHandler.java : this activity is responsible to
 * calling and handling web services.

 */

public class ServiceHandler extends AsyncTask<Void, Void, Boolean> {

    private final SerrializationOFObject sob = new SerrializationOFObject();
    private final boolean LocalcacheEnabled = false;
    private final int localCacheLifeCycle = 0;
    /**
     * @return root path
     */
    private final LinkedHashSet<String> allFile = new LinkedHashSet<String>();
    public GetResponse delegate = null;
    Activity mActivity;
    private String jsonArrayResponce = "";
    private ProgressDialog dialog;
    private boolean LocalshowinbuiltPorgressbar;
    private String ErrorType = "";
    private boolean success = true;
    private boolean isFilePost = false;
    private String lType = "";
    private int mRequestCode = 0;
    private HashMap<String, Object> lParamter = new HashMap<String, Object>();
    private RequestBody mRequestBody;
    private ArrayList<Object> jsonDataArrayList = new ArrayList<Object>();
    private LinkedHashMap<String, Object> jsonDataHashMapList = new LinkedHashMap<String, Object>();
    private boolean cancel = false;
    private boolean isJsonRequest;
    private HashMap<String, String> fileArray;
    private long totalSize;
    private String Lurl;
    private String fileName = "";
    private boolean iFileUpload = false;
    private File cacheDir;
    private ArrayList<Object> jsonData = null;

    //	private ArrayList<Question> dataArrayList;
    private boolean isAuth;

    /**
     * @param activity
     * @param type
     * @param mUrl
     * @param Paramter
     * @param showinbuiltPorgressbar
     */
    public ServiceHandler(Activity activity, String type, String mUrl, HashMap<String, Object> Paramter, boolean showinbuiltPorgressbar, int requestCode, boolean isAuth) {
        mActivity = activity;
        LocalshowinbuiltPorgressbar = showinbuiltPorgressbar;
        lType = type;
        lParamter = Paramter;
        cancel = false;
        Lurl = mUrl;
        mRequestCode = requestCode;
        this.isAuth = isAuth;
        if (showinbuiltPorgressbar) {
            dialog = new ProgressDialog(mActivity.getApplicationContext());
        }
    }


    /**
     * @param activity
     * @param type
     * @param mUrl
     * @param Paramter
     * @param showinbuiltPorgressbar
     */
    public ServiceHandler(Activity activity, String type, String mUrl, RequestBody Paramter, boolean showinbuiltPorgressbar, int requestCode, boolean isAuth) {
        mActivity = activity;
        LocalshowinbuiltPorgressbar = showinbuiltPorgressbar;
        lType = type;
        mRequestBody = Paramter;

        cancel = false;
        Lurl = mUrl;
        mRequestCode = requestCode;
        this.isAuth = isAuth;
        if (showinbuiltPorgressbar) {
            dialog = new ProgressDialog(mActivity);
//            dialog = new ProgressDialog(mActivity.getApplicationContext());
        }
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        if (CheckUserOnline(mActivity) || checkWifi(mActivity)) {
            try {
                if (lType.equalsIgnoreCase(Constant.Type.post)) {
                    jsonArrayResponce = urlResponsePost();
                    Log.e("Print Responce", jsonArrayResponce);
                } else if (lType.equalsIgnoreCase(Constant.Type.get)) {
                    jsonArrayResponce = urlResponseGET();
                }

				/*else if (lType.equalsIgnoreCase("PATCH"))
                {
					jsonArrayResponce = urlResponsePatch();
				}*/
            } catch (Exception e) {
                Log.e("MyPharmacyOptions", "Error :: " + e);
                ErrorType = e.getMessage();
                success = false;
            }
            return success;
        } else {
            ErrorType = mActivity.getResources().getString(R.string.internetmsg);
            success = false;
            return success;
        }
    }


    @Override
    protected void onCancelled() {
        if (LocalshowinbuiltPorgressbar) {
            delegate.processFinish(jsonArrayResponce, mRequestCode, success);
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
        }

//		invokemethod(jsonData);
        // Toast.makeText(activity, "Task has been canceled", 300).show();
        success = false;
        cancel = true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (cancel) {
            cancel = false;
        } else {
            if (!result) {
                if (LocalshowinbuiltPorgressbar) {
                    if (this.dialog.isShowing()) {
                        this.dialog.dismiss();
                    }
                }
//                showCommonAlert(ErrorType, mActivity);
                CommonUtil.displayToast(mActivity, ErrorType);
            } else {
                if (LocalshowinbuiltPorgressbar) {
                    if (this.dialog.isShowing()) {
                        this.dialog.dismiss();
                    }

                }

                if (delegate != null) {

                    delegate.processFinish(jsonArrayResponce, mRequestCode, success);
                } else {
                    Log.e("ApiAccess", "You have not assigned IApiAccessResponse delegate");
                }

            }
        }
//		mWakeHimUp.release();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onPreExecute() {
        if (LocalshowinbuiltPorgressbar) {
            this.dialog.setTitle(mActivity.getResources().getString(R.string.loading));
            this.dialog.setMessage(mActivity.getResources().getString(R.string.waiting));
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

       /*PowerManager mPowerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
        mWakeHimUp = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyLock");
		mWakeHimUp.acquire();*/

    }

    /**
     * @param message
     * @param _aActivity used to show alert dialog
     */


    /**
     * @param context
     * @return below methods are used to check ineternet connection
     */

    private boolean CheckUserOnline(Context context) {
        try {
            ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conManager == null) {
                return false;
            }
            NetworkInfo info = conManager.getActiveNetworkInfo();
            if (info == null) {
                return false;
            }
            return info != null;

        } catch (Exception e) {
            e.getMessage();
            return false;
        }
    }

    private boolean checkWifi(Context context) {
        try {
            WifiManager ObjWifiManage = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (ObjWifiManage == null) {
                return false;
            }
            return ObjWifiManage.isWifiEnabled();

        } catch (Exception e) {
            e.getMessage();
            return false;
        }
    }


    private LinkedHashMap<String, Object> parseJSONObject(JSONObject jsonObject) {
        LinkedHashMap<String, Object> LinkedHashMap = new LinkedHashMap<String, Object>();

        for (Iterator<?> iterator = jsonObject.keys(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            Object value = null;
            if (key != null) {
                key = key.trim();
                if (!key.equals("")) {
                    try {
                        value = jsonObject.getString(key);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (value != null) {
                        String className = null;
                        try {
                            className = value.getClass().getCanonicalName().toString();
                        } catch (Exception e) {
                            className = "";
                        }

                        if (className.equals("org.json.JSONObject")) {
                            JSONObject jsonObject2 = (JSONObject) value;
                            Object o = parseJSONObject(jsonObject2);
                            LinkedHashMap.put(key, o);
                        } else if (className.equals("org.json.JSONArray")) {
                            JSONArray jsonArray = (JSONArray) value;
                            Object o = parseJSONArray(jsonArray);
                            LinkedHashMap.put(key, o);
                        } else if (className.equalsIgnoreCase("java.lang.String")) {
                            String stringValue = (String) value; // if there is
                            // JSONArray
                            // or
                            // JSONObject
                            // inside
                            // String

                            stringValue = stringValue.trim();

                            if (stringValue.startsWith("[")) {
                                JSONArray jsonStirngArray = getJSONArrayFromString(stringValue);
                                if (jsonStirngArray != null) {
                                    Object o = parseJSONArray(jsonStirngArray);
                                    LinkedHashMap.put(key, o);
                                }
                            } else if (stringValue.startsWith("{")) {
                                JSONObject jsonStringObject = getJSONObjectFromString(stringValue);
                                if (jsonStringObject != null) {
                                    Object o = parseJSONObject(jsonStringObject);
                                    LinkedHashMap.put(key, o);
                                }
                            } else {
                                LinkedHashMap.put(key, value);
                            }
                        }
                    } else {
                        LinkedHashMap.put(key, value);
                    }
                }
            }
        }
        return LinkedHashMap;
    }

    private JSONObject getJSONObjectFromString(String stringValue) {
        JSONObject jsonObject = null;
        try {
            JSONTokener jsonTokener = new JSONTokener(stringValue);
            jsonObject = new JSONObject(jsonTokener);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jsonObject == null) {
            try {
                jsonObject = new JSONObject(stringValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return jsonObject;

    }

    private JSONArray getJSONArrayFromString(String stringValue) {
        JSONArray jsonArray = null;
        try {
            JSONTokener jsonTokener = new JSONTokener(stringValue);
            jsonArray = new JSONArray(jsonTokener);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jsonArray == null) {
            try {
                jsonArray = new JSONArray(stringValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return jsonArray;
    }

    private ArrayList<Object> parseJSONArray(JSONArray jsonArray) {
        ArrayList<Object> arrayList = new ArrayList<Object>();

        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            try {
                Object o = jsonArray.get(i);
                if (o != null) {
                    String className = o.getClass().getCanonicalName()
                            .toString();
                    if (className != null) {
                        if (className.equals("org.json.JSONArray")) {
                            JSONArray subArray = (JSONArray) o;
                            Object object = parseJSONArray(subArray);
                            if (object != null) {
                                arrayList.add(object);
                            }
                        } else if (className.equals("org.json.JSONObject")) {
                            JSONObject subObject = (JSONObject) o;
                            Object object = parseJSONObject(subObject);
                            if (object != null) {
                                arrayList.add(object);
                            }
                        } else if (className.equals("java.lang.String")) {
                            String s = (String) o;
                            arrayList.add(s);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }


    public void isFilePost(boolean isFilePost) {
        this.isFilePost = isFilePost;
        /*if (isFilePost && dialog != null)
        {
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMessage("Uploading...");
		}*/
    }

    public void setFile(HashMap<String, String> fileArray) {
        this.fileArray = fileArray;
    }

    /**
     * For Base64 String upload this method is used
     *
     * @param flag
     */
    public void setFileUpload(boolean flag) {
        iFileUpload = flag;
    }


    public void setjsonRequest(boolean isjsonRequest) {
        this.isJsonRequest = isjsonRequest;
    }


    public String urlResponsePost() {
        OkHttpClient client = new OkHttpClient();
        Response postResponse = null;
        String mRespons = "";
        try {

            if (isAuth) {
                Request request = new Request.Builder()
                        .url(Lurl)
                        .post(mRequestBody)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", AppSharedPref.getInstance(mActivity).getDataString(AppSharedPref.loginTokenKey))
                        .build();

                Log.e("Loginkey--", "" + AppSharedPref.getInstance(mActivity).getDataString(AppSharedPref.loginTokenKey));
                Log.e("url--", "" + Lurl);
                client.setConnectTimeout(90, TimeUnit.SECONDS);
                client.setReadTimeout(90, TimeUnit.SECONDS);
                client.setWriteTimeout(90, TimeUnit.SECONDS);
                postResponse = client.newCall(request).execute();
                mRespons = postResponse.body().string();
            } else {
                Request request = new Request.Builder()
                        .url(Lurl)
                        .post(mRequestBody)
                        .addHeader("Content-Type", "application/json")
                        .build();
                client.setConnectTimeout(90, TimeUnit.SECONDS);
                client.setReadTimeout(90, TimeUnit.SECONDS);
                client.setWriteTimeout(90, TimeUnit.SECONDS);
                postResponse = client.newCall(request).execute();
                mRespons = postResponse.body().string();
            }
            Log.e("URLLLL", "" + Lurl);

        } catch (Exception e) {
            Log.e("URLLLL", "" + Lurl);
            e.printStackTrace();
            success = false;
            ErrorType = mActivity.getString(R.string.str_something_went_worng);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CommonUtil.displayToast(mActivity, ErrorType);
                }
            });
        }

        return mRespons;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    public String urlResponseGET() {
        OkHttpClient client = new OkHttpClient();
        Response postResponse = null;
        String mRespons = "";
        try {

            if (isAuth) {
                Request request = new Request.Builder()
                        .url(Lurl)
                        .get()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", AppSharedPref.getInstance(mActivity).getDataString(AppSharedPref.loginTokenKey))
                        .build();

                Log.e("Loginkey--", "" + AppSharedPref.getInstance(mActivity).getDataString(AppSharedPref.loginTokenKey));
                Log.e("url--", "" + Lurl);

                client.setConnectTimeout(90, TimeUnit.SECONDS);
                client.setReadTimeout(90, TimeUnit.SECONDS);
                client.setWriteTimeout(90, TimeUnit.SECONDS);
                postResponse = client.newCall(request).execute();
                mRespons = postResponse.body().string();
            } else {
                Request request = new Request.Builder()
                        .url(Lurl)
                        .get()
                        .addHeader("Content-Type", "application/json")
                        .build();
                client.setConnectTimeout(90, TimeUnit.SECONDS);
                client.setReadTimeout(90, TimeUnit.SECONDS);
                client.setWriteTimeout(90, TimeUnit.SECONDS);
                postResponse = client.newCall(request).execute();
                mRespons = postResponse.body().string();
            }

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            ErrorType = mActivity.getString(R.string.str_something_went_worng);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CommonUtil.displayToast(mActivity, ErrorType);
                }
            });
        }

        return mRespons;
    }

    private String convertToURL(HashMap<String, String> appendData) {
        StringBuilder sbAppendToURL = null;
        if (appendData != null) {
            Iterator<String> ikey = appendData.keySet().iterator();
            sbAppendToURL = new StringBuilder();
            while (ikey.hasNext()) {
                String dataKey = ikey.next();
                if (!dataKey.equalsIgnoreCase("") && !appendData.get(dataKey).equalsIgnoreCase("")) {
                    sbAppendToURL.append(dataKey + "=" + appendData.get(dataKey) + "&");
                }
            }
            if (!sbAppendToURL.toString().trim().equalsIgnoreCase("")) {
                return sbAppendToURL.toString().trim().substring(0, sbAppendToURL.toString().trim().length() - 1);
            }
        }
        return "";
    }

    public boolean perFormedCachingOnJson(String url, int cachLifelifeCycle) {
        String fileUrl = md5(url);
        File file = new File(RooTAxis() + "/" + fileUrl);

        long time = file.lastModified();
        if (time == 0) {
            return true;
        }
        time += cachLifelifeCycle;
        Calendar calender = Calendar.getInstance();

        long currentTime = calender.getTimeInMillis();
        currentTime = System.currentTimeMillis();

        if (currentTime < time) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * @param in
     * @return md4 string
     */
    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String RooTAxis() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "VideoList");
        } else {
            cacheDir = mActivity.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File[] fileAll = cacheDir.listFiles();
        for (File file : fileAll) {
            allFile.add(file.getAbsolutePath());
        }
        return cacheDir.getAbsolutePath();
    }

    public interface GetResponse {
        void processFinish(String output, int request, boolean success);
    }

    /**
     * *@param bitmap
     *
     * @return converting bitmap and return a string
     */
    /*@SuppressLint("NewApi")
    public StringBody BitMapToString(String path) {
        UploadPicture uploadPicture = new UploadPicture(mActivity);
        Bitmap bitmap = uploadPicture.setPic(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        StringBody stringBody = null;
        try {
            stringBody = new StringBody(Base64.encodeToString(b, Base64.DEFAULT).toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return stringBody;
    }*/

}