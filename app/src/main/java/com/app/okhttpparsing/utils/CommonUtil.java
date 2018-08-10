package com.app.okhttpparsing.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.okhttpparsing.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CommonUtil {

    private static final String TAG = "RadaeeCommonUtil";
    private static final int CACHE_LIMIT = 1024;

    public static String getThumbName(String path) {
        try {
            File file = new File(path);
            long lastModifiedDate = file.lastModified();
            return CommonUtil.md5(path + lastModifiedDate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * loads image in the imageview passed in parameter
     *
     * @param activity
     * @param imageView
     * @param imageUri
     */
    public static void loadImage(Activity activity, ImageView imageView, String imageUri, int placeHolder) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
        }
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).showImageOnFail(placeHolder)
                .showImageOnFail(placeHolder)
                .showImageOnLoading(placeHolder).cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        imageLoader.displayImage(imageUri, imageView, imageOptions);
    }

    public static boolean isNullString(String string) {
        try {
            if (string == null || string.trim().equalsIgnoreCase("null") || string.trim().length() < 0 || string.trim().equals("")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return true;
        }
    }


    public static Bitmap loadThumb(File pictureFile) {
        try {
            if (!pictureFile.exists())
                return null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveThumb(Bitmap image, File pictureFile) {
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    static File getOutputMediaFile(Context context, String thumbName) {
        File dir = new File(context.getCacheDir() + "/thumbnails");
        if (dir.exists()) { //too many caches
            File files[] = dir.listFiles();
            if (files.length > CACHE_LIMIT)
                files[0].deleteOnExit();
        }
        File file = new File(context.getCacheDir() + "/thumbnails/" + thumbName + ".png");
        if (!file.exists()) {
            File mediaStorageDir = new File(context.getCacheDir() + "/thumbnails");

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            // Create a media file name
            String mImageName = thumbName + ".png";
            return new File(mediaStorageDir.getPath() + File.separator + mImageName);
        } else {
            return file;
        }
    }

    private static String md5(String input) {
        return md5(input.getBytes());
    }

    public static String md5(byte[] input) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(input);
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Void displayToast(Context context, String strToast) {
        Toast.makeText(context, strToast, Toast.LENGTH_SHORT).show();
        return null;
    }

    static private String m_types[] = new String[]{"null", "boolean", "int", "real", "string", "name", "array", "dictionary", "reference", "stream"};

    static private String get_type_name(int type) {
        if (type >= 0 && type < m_types.length) return m_types[type];
        else return "unknown";
    }


    /**
     * @return DateTime String object<br/>
     * format as (D:YYYYMMDDHHmmSSOHH'mm') where:<br/>
     * YYYY is the year<br/>
     * MM is the month<br/>
     * DD is the day (01–31)<br/>
     * HH is the hour (00–23)<br/>
     * mm is the minute (00–59)<br/>
     * SS is the second (00–59)<br/>
     * O is the relationship of local time to Universal Time (UT), denoted by one of the characters +, −, or Z (see below)<br/>
     * HH followed by ' is the absolute value of the offset from UT in hours (00–23)<br/>
     * mm followed by ' is the absolute value of the offset from UT in minutes (00–59)<br/>
     * more details see PDF-Reference-1.7 section 3.8.3
     */
    public static String getCurrentDate() {
        String datePattern = "yyyyMMddHHmmssZ''";
        String date = new SimpleDateFormat(datePattern, Locale.getDefault()).format(new Date());
        return "D:" + date.substring(0, date.length() - 3) + "'" + date.substring(date.length() - 3);
    }

    /**
     * This method is responsible for getting file Path from Image URI
     */
    public static String getFilePath(Context context, Uri data) {
        String path = "";

        // For non-gallery application
        path = data.getPath();

        // For gallery application
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(data, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }

    /**
     * This method is responsible for getting Bitmap from Image URI
     */
    public static Bitmap getBitmapFromUri(Context context, Uri data) {
        Bitmap bitmap = null;

        // Starting fetch image from file
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(data);
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // BitmapFactory.decodeFile(path, options);
            BitmapFactory.decodeStream(is, null, options);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            is = context.getContentResolver().openInputStream(data);
            bitmap = BitmapFactory.decodeStream(is, null, options);
            if (bitmap == null) {
                Toast.makeText(context, R.string.image_not_loaded, Toast.LENGTH_SHORT).show();
                return null;
            }
            is.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * This method is responsible for getting rotated Bitmap from Image.
     *
     */
    public static Bitmap getRotatedBitmap(String path, Bitmap bitmap) {
        Bitmap rotatedBitmap = bitmap;
        Matrix matrix = new Matrix();
        ExifInterface exif = null;
        int orientation = 1;
        try {
            if (path != null) {
                // Getting Exif information of the file
                exif = new ExifInterface(path);
            }
            if (exif != null) {
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.preRotate(270);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.preRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.preRotate(180);
                        break;
                }
                // Rotates the image according to the orientation
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    /**
     * This method is responsible for getting Path from Image URI
     *
     */
    public static String getRealPathFromURI(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String filePath = cursor.getString(idx);
            cursor.close();
            return filePath;
        } else {
            Toast.makeText(context, R.string.read_image_fail, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}
