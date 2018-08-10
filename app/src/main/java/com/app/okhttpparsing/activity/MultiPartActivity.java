package com.app.okhttpparsing.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.okhttpparsing.R;
import com.app.okhttpparsing.model.MultiPartModel;
import com.app.okhttpparsing.parsing.ServiceHandlerFile;
import com.app.okhttpparsing.utils.AppLog;
import com.app.okhttpparsing.utils.CommonUtil;
import com.app.okhttpparsing.utils.Constant;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.graphics.BitmapFactory.decodeFile;
import static android.support.v4.content.FileProvider.getUriForFile;

public class MultiPartActivity extends AppCompatActivity implements ServiceHandlerFile.GetResponse {

    private static final int STORAGE_PERMS = 3215;
    private int REQUEST_CAMERA = 3, SELECT_FILE = 4;
    private Uri imageURI;
    private String mCurrentPhotoPath;
    private Bitmap rotatedBitmap = null;
    private File mFileImagePath = null;

    private ImageView mIvUpload;
    private Button mBtnUpload;
    private RelativeLayout mRoot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multipart);

        findViews();

    }

    private void findViews() {
        mRoot = findViewById(R.id.mRoot);
        mBtnUpload = findViewById(R.id.btnUpload);
        mIvUpload = findViewById(R.id.ivUpload);
        mIvUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED &&
                            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                        , Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                STORAGE_PERMS);
                    } else {
                        selectImage();
                    }
                } else {
                    selectImage();
                }

            }
        });

        mBtnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callServiceMultiPart(mFileImagePath.getName());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                } else {
                    if (!(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                        Snackbar snackbar = Snackbar.make(mRoot, getString(R.string.permission_never_asked)
                                , Snackbar.LENGTH_INDEFINITE);
                        snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
                        snackbar.setAction(getString(R.string.allow), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
                        snackbar.show();
                    }
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.from_library)
                , getString(R.string.cancel_image)};

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MultiPartActivity.this);
        builder.setTitle(getString(R.string.add_pic));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.take_photo))) {
                    cameraIntent();

                } else if (items[item].equals(getString(R.string.from_library))) {
                    galleryIntent();

                } else if (items[item].equals(getString(R.string.cancel_image))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * @param
     * @return
     * @throws
     * @purpose this method is used to pass intent to  gallery
     */
    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_FILE);
    }

    /**
     * @param
     * @return
     * @throws
     * @purpose this method is used to pass intent to  camera
     */
    private void cameraIntent() {
        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);*/
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(MultiPartActivity.this, "jpg", Environment.DIRECTORY_PICTURES);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            Uri photoURI;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoURI = getUriForFile(MultiPartActivity.this,
                        "com.app.emeeting.android.fileprovider",
                        photoFile);
            } else {
                photoURI = Uri.fromFile(photoFile);
            }
            imageURI = photoURI;

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_CAMERA);
        }
    }

    /**
     * @param
     * @return
     * @throws
     * @purpose this method is used to create an image file using file provider
     */

    public File createImageFile(Activity activity, String extension, String type) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = extension + "_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                "." + extension,         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
    }

    /**
     * @param
     * @return
     * @throws
     * @purpose this method is used to set Gallery image to imageview
     */
    private void onSelectFromGalleryResult(Intent data) {

        if (data != null) {
            String path = CommonUtil.getFilePath(MultiPartActivity.this, data.getData());
            Bitmap bitmap = CommonUtil.getBitmapFromUri(MultiPartActivity.this, data.getData());

            if (bitmap != null) {
                rotatedBitmap = CommonUtil.getRotatedBitmap(path, bitmap);
                Uri tempUri = getImageUri(MultiPartActivity.this, rotatedBitmap);
                String fileUri = CommonUtil.getRealPathFromURI(MultiPartActivity.this, tempUri);
                if (fileUri != null) {
                    mIvUpload.setImageBitmap(rotatedBitmap);
                    mFileImagePath = new File(fileUri);
                    //mStrFileName = mFileImagePath.getName();
                    //mTvProPic.setText(String.valueOf(mFileImagePath));
                }
                AppLog.LogE("FilePAth", "++" + mFileImagePath);
                //AppLog.LogE("FileName", "++" + mStrFileName);

                //performCrop();

            } else {
                Toast.makeText(MultiPartActivity.this, R.string.invalid_image_file, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * @param
     * @return
     * @throws
     * @purpose this method is used to set Camera image to imageview
     */

    private void onCaptureImageResult(Intent data) {

        File finalFile = null;

        finalFile = new File(mCurrentPhotoPath);
        Bitmap bitmap = getBitmapFromFile(mCurrentPhotoPath, MultiPartActivity.this);

        mFileImagePath = finalFile;
        //mStrFileName = mFileImagePath.getName();
        AppLog.LogE("FileNameCamera", "++" + mFileImagePath);
        //mTvProPic.setText(String.valueOf(mFileImagePath));
        rotatedBitmap = bitmap;

        //performCrop();
        mIvUpload.setImageBitmap(bitmap);
    }


    /**
     * @param
     * @return
     * @throws
     * @purpose this method is used for geting URI from Bitmap
     */
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        if (path != null) {
            return Uri.parse(path);
        } else {
            return null;
        }
    }

    /**
     * @param
     * @return
     * @throws
     * @purpose this method is used  to returns the bitmap from the file
     */
    public static Bitmap getBitmapFromFile(String selectedImagePath, Activity activity) {
        Bitmap bitmap;
        bitmap = decodeFile(selectedImagePath);
        bitmap = CommonUtil.getRotatedBitmap(selectedImagePath, bitmap);
        return bitmap;
    }


    @Override
    public void processFinish(String output, int request, boolean success) {
        if (request == 0) {
            handleResponseMultiPart(output);
        }
    }

    public void callServiceMultiPart(String strEmailId) {

        MultipartBuilder multipartBuilder = new MultipartBuilder().type(MultipartBuilder.FORM);
        multipartBuilder.addFormDataPart(Constant.WebServicesKeys.mMultiPartFile, strEmailId);
        if (mFileImagePath != null) {
            multipartBuilder.addFormDataPart(Constant.WebServicesKeys.mMultiPartFile, mFileImagePath.getName(), RequestBody.create(MediaType.parse("image*//*"), new File(String.valueOf(mFileImagePath))));
        }
        RequestBody formBody = multipartBuilder.build();
        ServiceHandlerFile sh = new ServiceHandlerFile(MultiPartActivity.this, Constant.Type.post
                , Constant.Urls.strMultiPartURL, formBody, true, 0);
        sh.setjsonRequest(false);
        sh.delegate = this;
        sh.execute();
    }

    private void handleResponseMultiPart(String output) {
        AppLog.LogE("handleResponseMultiPart", output);
        final Gson gson = new Gson();
        try {
            MultiPartModel multiPartModel = gson.fromJson(output, MultiPartModel.class);
            if (multiPartModel != null) {
                if (multiPartModel.getSuccess() == true) {
                    Toast.makeText(this, "" + multiPartModel.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "" + multiPartModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {

        }
    }
}
