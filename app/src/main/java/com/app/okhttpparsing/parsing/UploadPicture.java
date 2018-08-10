package com.app.okhttpparsing.parsing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * UploadPicture.java : this activity is responsible to
 * handle service of upload picture
 *
 */

public class UploadPicture {
    Activity act;

    Intent takePictureIntent;

    public UploadPicture(Activity acti) {
        act = acti;
    }

    public String mCurrentPhotoPath;

    public void dispatchTakePictureIntent(int actionCode) {
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (Exception e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        } // switch
        act.startActivityForResult(takePictureIntent, actionCode);
    }

    private File setUpPhotoFile() {
        try {
            long current = System.currentTimeMillis();
            File file = new File("/mnt/sdcard/" + current + ".png");
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public void setPic(ImageView mImageView, Intent intent) {

		/* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Figure out which way needs to be reduced less */
        final int REQUIRED_SIZE = 100;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = bmOptions.outWidth, height_tmp = bmOptions.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        /* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scale;

		/* Decode the JPEG file into a Bitmap */
        try {
            Matrix matrix = new Matrix();
            matrix.setRotate(getCameraPhotoOrientation(act, Uri.fromFile(new File(mCurrentPhotoPath)), mCurrentPhotoPath));
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

            mImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.getMessage();
        }
    }


    public Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = act.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        mCurrentPhotoPath = filePath;

        // Decode image size

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(act.getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        //for video blog we have to change image size 140 to 70
        final int REQUIRED_SIZE = 100;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(act.getContentResolver().openInputStream(selectedImage), null, o2);

    }

    private int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public Bitmap setPic(String path) {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);

        final int REQUIRED_SIZE = 70;

        int width_tmp = bmOptions.outWidth, height_tmp = bmOptions.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scale;

        try {
            return BitmapFactory.decodeFile(path, bmOptions);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
