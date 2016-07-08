package com.example.qr_codescan;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.qr_codescan.utils.Url;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;


import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2016/7/7.
 */
public class RegisterActivity extends Activity {
    private EditText et_username,et_password,et_name,et_phone;
    private Button btn_register;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String status;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_main);
        initView();
        initData();
    }

    private void initData() {
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_username.getText().toString().equals("")||et_password.getText().toString().equals("")){
                    Toast.makeText(RegisterActivity.this,"请输入账户密码！！",Toast.LENGTH_SHORT).show();
                }else{
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    PostRequestzhuce();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                }
            }
        });
    }

    private void initView() {
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_name = (EditText) findViewById(R.id.et_name);
        et_phone= (EditText) findViewById(R.id.et_phone);
        btn_register = (Button) findViewById(R.id.btn_register);
    }

        public int PostRequestzhuce() throws HttpException, IOException {
            username = et_username.getText().toString();
            password = et_password.getText().toString();
            name = et_name.getText().toString();
            phone = et_phone.getText().toString();
            HttpClient client = new HttpClient();
            StringBuilder sb = new StringBuilder();
            InputStream ins = null;
            int statusCode = HttpStatus.SC_METHOD_FAILURE;
            JSONObject jsonObj = null;
            //注册时网址。
            PostMethod method = new PostMethod(Url.register_url);
            Log.i("flag","到这里了username"+username+password);
            NameValuePair[] param = {
                    new NameValuePair("username", username),
                    new NameValuePair("password", password),
                    new NameValuePair("realname", name),
                    new NameValuePair("phone", phone),
            };
            method.setRequestHeader("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            method.setRequestBody(param);
            try {
                // Execute the method.
                statusCode = client.executeMethod(method);
                // System.out.println(statusCode);
                if (statusCode == HttpStatus.SC_OK) {
                    ins = method.getResponseBodyAsStream();
                    byte[] b = new byte[1024];
                    int r_len = 0;
                    while ((r_len = ins.read(b)) > 0) {
                        sb.append(new String(b, 0, r_len, method
                                .getResponseCharSet()));
                    }
                   String result = sb.toString();
                    Log.i("flag","result---------"+result);
                    try {
                        //从服务器返回的JSON字符串。解析。
                        jsonObj =JSONObject.fromString(result);
                        Log.i("flag",jsonObj.toString());
                        if (jsonObj != null) {
                           status = jsonObj.getString("status");
                            String msg = jsonObj.getString("message");
                            if (status.equals("success")) {
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this,msg, Toast.LENGTH_SHORT).show();
                                //showMessage(AppData.PROMPT_QUERY, msg2, true);
                                finish();
                                Looper.loop();

                            } else {
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this,msg, Toast.LENGTH_SHORT).show();
                                //showMessage(AppData.PROMPT_QUERY, msg2, true);
                                Looper.loop();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Response Code: " + statusCode);
                }
            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Fatal transport error: " + e.getMessage());
            } finally {
                if (jsonObj == null) {
                    statusCode = HttpStatus.SC_METHOD_FAILURE;
                }
                method.releaseConnection();
                if (ins != null) {
                    ins.close();
                }
            }
            return statusCode;
        }
    public void showMessage(int msgType, final String infomsg, boolean isSound) {
        try {
            if (isSound) {
                if (msgType == AppData.PROMPT_SUCCESS) {

                } else if (msgType == AppData.PROMPT_QUERY) {

                } else {
                }
            }
            Message msg = AppData.getInstance().getmHandler()
                    .obtainMessage(AppData.MESSAGE_TOAST);


            Bundle bundle = new Bundle();
            bundle.putString(AppData.TOAST, infomsg);
            msg.setData(bundle);
            AppData.getInstance().getmHandler().sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

