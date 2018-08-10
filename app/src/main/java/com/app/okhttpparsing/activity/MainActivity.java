package com.app.okhttpparsing.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.okhttpparsing.R;
import com.app.okhttpparsing.model.GetModel;
import com.app.okhttpparsing.model.Model;
import com.app.okhttpparsing.model.PostModel;
import com.app.okhttpparsing.parsing.ServiceHandler;
import com.app.okhttpparsing.parsing.ServiceHandlerFile;
import com.app.okhttpparsing.utils.AppLog;
import com.app.okhttpparsing.utils.Constant;
import com.google.gson.Gson;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ServiceHandlerFile.GetResponse, ServiceHandler.GetResponse, View.OnClickListener {

    private Button mBtnFormEncoding, mBtnMultiPart, mBtnGet;
    private TextView mTvResponse;
    private File mFileImagePath = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }

    private void findViews() {
        mBtnFormEncoding = findViewById(R.id.btnFormEncoding);
        mBtnMultiPart = findViewById(R.id.btnMultiPart);
        mBtnGet = findViewById(R.id.btnGet);
        mTvResponse = findViewById(R.id.tvResponse);
        mBtnFormEncoding.setOnClickListener(this);
        mBtnMultiPart.setOnClickListener(this);
        mBtnGet.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFormEncoding:
                callServiceFormBuilder("morpheus", "leader");
                break;

            case R.id.btnMultiPart:
                //callServiceMultiPart("", "", "", "");
                Intent intent = new Intent(MainActivity.this, MultiPartActivity.class);
                startActivity(intent);
                break;

            case R.id.btnGet:
                callServiceGet();
                break;
        }
    }

    @Override
    public void processFinish(String output, int request, boolean success) {
        if (request == 1) {
            handleResponseFormBuilder(output);
        } else if (request == 2) {
            handleResponseGet(output);
        }
    }


    public void callServiceFormBuilder(String strPostName, String strPostJob) {
        RequestBody formBody = new FormEncodingBuilder()
                .add(Constant.WebServicesKeys.mPostName, strPostName)
                .add(Constant.WebServicesKeys.mPostJob, strPostJob)
                .build();
        ServiceHandler sh = new ServiceHandler(MainActivity.this, Constant.Type.post, Constant.Urls.strPostURL, formBody, true, 1, true);
        sh.setjsonRequest(false);
        sh.delegate = this;
        sh.execute();
    }

    private void handleResponseFormBuilder(String output) {
        AppLog.LogE("handleResponseFormBuilder", output);
        mTvResponse.setText(output);
        final Gson gson = new Gson();
        try {
            PostModel model = gson.fromJson(output, PostModel.class);
            if (model != null) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callServiceGet() {
        RequestBody formBody = new FormEncodingBuilder()
                .build();
        ServiceHandler sh = new ServiceHandler(MainActivity.this, Constant.Type.get, Constant.Urls.strGetURL, formBody, true, 2, true);
        sh.setjsonRequest(false);
        sh.delegate = this;
        sh.execute();
    }

    private void handleResponseGet(String output) {
        AppLog.LogE("handleResponseGet", "--" + output);
        mTvResponse.setText(output);
        final Gson gson = new Gson();
        try {
            final GetModel model = gson.fromJson(output, GetModel.class);
            if (model != null) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // in case of you want to pass object in your request

    RequestBody post(String url, String json) throws IOException {
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, json);
        return body;

    }

    private void callPostWebService() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("key", "value");
            jsonObj.put("key", "value");
            jsonObj.put("key", "value");
            jsonObj.put("key", "value");

            RequestBody jsonReqBody = post(Constant.Urls.strGetURL, jsonObj.toString());
            ServiceHandler sh = new ServiceHandler(MainActivity.this, Constant.Type.post, Constant.Urls.strGetURL, jsonReqBody, false, 2, true);
            sh.setjsonRequest(false);
            sh.delegate = this;
            sh.execute();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
