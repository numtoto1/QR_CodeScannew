package com.example.qr_codescan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.qr_codescan.bean.Info_bean;
import com.example.qr_codescan.bean.JsonBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/7/4.
 */
public class Scan_Writer extends Activity {
private Button btn_submit,btn_sign;
    private Bitmap mSignBitmap;
    private File file;
    private Info_bean info_bean;
    private ProgressDialog dialog;
    private String sign_dir;
    private String name = null;
    private HashMap<Integer, Integer> soundPoolMap;
    private SoundPool soundPool;
    private Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String result = (String) msg.obj;
            Log.e("flag",result.toString());
            Gson gson = new Gson();
            Type type = new TypeToken<JsonBean>() {
            }.getType();
            JsonBean jsonBean = gson.fromJson(result, type);
            Log.e("flag","jsonBean "+jsonBean.toString());
            if (jsonBean.getStatus().equals("success")){
                showMessage( AppData.PROMPT_SUCCESS,true);
                Toast.makeText(Scan_Writer.this,jsonBean.getMessage(),Toast.LENGTH_SHORT).show();
            }else {
                showMessage( AppData.PROMPT_ERROR,true);
                Toast.makeText(Scan_Writer.this,jsonBean.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.writer_main);
        initView();
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(Scan_Writer.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("info",info_bean);
                    startActivity(intent);
            }
        });
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WritePadDialog writeTabletDialog = new WritePadDialog(
                        Scan_Writer.this, new DialogListener() {
                    @Override
                    public void refreshActivity(Object object) {
                        mSignBitmap = (Bitmap) object;
                        createFile();
                        Toast.makeText(Scan_Writer.this, "签名生成成功", Toast.LENGTH_SHORT).show();
                        PostData(sign_dir,name);
                    }
                });
                writeTabletDialog.show();
            }
        });
    }

    private void initView() {
        btn_sign= (Button) findViewById(R.id.btn_sign);
        btn_submit= (Button) findViewById(R.id.btn_submit);
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.setIcon(R.drawable.ic_launcher);//
        Intent intent = getIntent();
        info_bean= (Info_bean) intent.getSerializableExtra("info");
        sign_dir = Environment.getExternalStorageDirectory() + File.separator+ "quanshi";
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);
        soundPoolMap = new HashMap<Integer, Integer>();
        soundPoolMap.put(1,soundPool.load(Scan_Writer.this, R.raw.success, 1));
        soundPoolMap.put(2,soundPool.load(Scan_Writer.this, R.raw.query, 1));
        soundPoolMap.put(3,soundPool.load(Scan_Writer.this, R.raw.error, 1));
        file = new File(sign_dir);
        if (!file.exists()) {
            file.mkdir();
        }
    }
    /**
     * 创建手写签名文件
     *
     * @return
     */
    private String createFile() {
        ByteArrayOutputStream baos = null;
        try {
            name =System.currentTimeMillis() + ".jpg";
            baos = new ByteArrayOutputStream();
            mSignBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] photoBytes = baos.toByteArray();
            if (photoBytes != null) {
                new FileOutputStream(new File(sign_dir,name)).write(photoBytes);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return name;
    }
    private void PostData(String Path,String name) {//上传
        String deviceId=info_bean.getDeviceId();
        String operatorId=info_bean.getOperatorId();
        String appPassword = info_bean.getAppPassword();
        String signature=MD5.GetMD5Code("deviceId="
                + deviceId  + "userId=" + operatorId
                + appPassword);
        SharedPreferences sp = getSharedPreferences("crazyit", MODE_WORLD_READABLE);
        String appUser  =  sp.getString("appUser",null);
        Log.i("spappUser",appUser);
        RequestParams params = new RequestParams();
        params.addBodyParameter("file", new File(Path,name),"image/jpg");
        params.addBodyParameter("appUser", appUser);
        params.addBodyParameter("deviceId", deviceId);
        params.addBodyParameter("signature", signature);
        params.addBodyParameter("userId", operatorId);
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST,
                "http://192.168.10.154/api/uploadSignature.html",
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        dialog.setMessage("开始上传...");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        dialog.setMessage("正在上传中...");
                        if (isUploading) {
                            dialog.show();
                        }
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        dialog.dismiss();
                        //TODO 成功以后。
                        Toast.makeText(Scan_Writer.this, "上传成功", Toast.LENGTH_SHORT).show();
                        Log.i("flag","上传成功返回.."+responseInfo.toString());
                        Message msg = Message.obtain();
                        msg.obj = responseInfo.result;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        dialog.dismiss();
                        Toast.makeText(Scan_Writer.this,"上传失败，请检查网络。",Toast.LENGTH_LONG).show();
                    }
                });
    }
    public void showMessage(int msgType,boolean isSound) {
        try {
            if (isSound) {
                if (msgType == AppData.PROMPT_SUCCESS) {
                    soundPool.play(soundPoolMap.get(1), 1, 1, 0, 0, 1);
                } else if (msgType == AppData.PROMPT_QUERY) {
                    soundPool.play(soundPoolMap.get(2), 1, 1, 0, 0, 1);
                }else {
                    soundPool.play(soundPoolMap.get(3), 1, 1, 0, 0, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
