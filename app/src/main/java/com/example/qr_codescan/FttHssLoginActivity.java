package com.example.qr_codescan;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.qr_codescan.bean.Info_bean;
import com.example.qr_codescan.utils.Url;
import com.lidroid.xutils.exception.HttpException;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.io.InputStream;

//全时运手持机系统登录Activity
public class FttHssLoginActivity extends Activity {
	// Debugging
	private static final String TAG = "FttHssLoginActivity";
	static String deviceId = null;
	BluetoothSocket socket = null;
	private EditText username = null;
	private EditText password = null;
	private String user;
	private String psw;
	static String result;
	static String appUser1;
	static String appPassword;
	static String operatorId;
	static String signature, appUser;
	static String msg1, msg2;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	private Button btn_Register;
	private String status;
	private int statusCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置布局
		setContentView(R.layout.login_main);
		preferences = getSharedPreferences("crazyit", MODE_WORLD_READABLE);
		editor = preferences.edit();
		TelephonyManager telephonyManager;
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = telephonyManager.getDeviceId();
		username = (EditText) findViewById(R.id.edtuser);
		password = (EditText) findViewById(R.id.edtpsw);
		Button btn_login = (Button) findViewById(R.id.login);
        btn_Register= (Button) findViewById(R.id.register);
		btn_Register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				        Intent intent = new Intent();
						intent.setClass(FttHssLoginActivity.this,
								RegisterActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(intent);
			}
		});
		Button btnzhuce = (Button) findViewById(R.id.btnzhuce);

		btnzhuce.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				user = username.getText().toString();
				psw = password.getText().toString();

				new Thread() {
					public void run() {

						try {
							try {
								if (PostRequestzhuce() == HttpStatus.SC_OK) {
                                }
							} catch (HttpException e) {
								e.printStackTrace();
							}
						} catch (IOException e) {
							Log.i("Fatal",e.getMessage());

						}
					}
				}.start();
			}
		});
		// 界面初始化
		username.setText("");
		password.setText("");
		username.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					user = "";
					username.setText(user);
				} else if (arg1.getAction() == MotionEvent.ACTION_MOVE) {

				} else if (arg1.getAction() == MotionEvent.ACTION_UP) {

				}
				return false;
			}
		});
		password.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					psw = "";
					password.setText(psw);
				} else if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
				} else if (arg1.getAction() == MotionEvent.ACTION_UP) {
				}
				return false;
			}
		});
		username.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (username.getText().toString().length() == 0) {
						showMessage(AppData.PROMPT_WARN, "请输入用户账号！", false);
					}
				} else {
					username.selectAll();
				}
			}

		});
		password.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (password.getText().toString().length() == 0) {
						showMessage(AppData.PROMPT_WARN, "请输入密码！", false);
					}
				} else {
					password.selectAll();
				}
			}

		});

		btn_login.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				user = username.getText().toString();
				psw = password.getText().toString();
				Log.i("flag","username---"+user);
				Log.i("flag","password---"+psw);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							if (PostRequest() == HttpStatus.SC_OK) {
								Intent intent = new Intent();
								intent.setClass(FttHssLoginActivity.this,
										Scan_Writer.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								Info_bean info_bean = new Info_bean(user,appUser,appPassword,deviceId,signature
										,operatorId);
								Log.i("1111",info_bean.toString());
								intent.putExtra("info",info_bean);
								startActivity(intent);
							} else {
								Toast.makeText(FttHssLoginActivity.this, "-------", Toast.LENGTH_SHORT).show();
							}

						} catch (org.apache.commons.httpclient.HttpException e) {
							System.err.println("Fatal protocol violation: "
									+ e.getMessage());
						} catch (IOException e) {
							System.err.println("Fatal transport error: "
									+ e.getMessage());
						} catch (HttpException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
	}



//登录时向服务器请求数据。根据返回msg决定。。
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
//登录的请求。
	public int PostRequest() throws HttpException, IOException {
		HttpClient client = new HttpClient();
		StringBuilder sb = new StringBuilder();
		InputStream ins = null;
		statusCode = HttpStatus.SC_METHOD_FAILURE;

		JSONObject jsonObj = null;
		 appUser1 = preferences.getString("appUser",null);
		appPassword = preferences.getString("appPassword",null);
		Log.i("flag","preferences----->"+appUser1);
		Log.i("flag","preferences----->"+appPassword);
		//请求url。检查url对错
		PostMethod method = new PostMethod(Url.Login_url);
		//向服务器传的值。传入参数：appUser、deviceId、signature、username、password
		signature=MD5.GetMD5Code("deviceId="
				+ deviceId + "password=" + psw + "username=" + user
				+ appPassword);
		Log.i("flag","登录signature----->"+signature);
		//上传的参数
		NameValuePair[] param = {
				new NameValuePair("appUser", appUser1),
				new NameValuePair("deviceId", deviceId),
				new NameValuePair("signature", MD5.GetMD5Code("deviceId="
						+ deviceId + "password=" + psw + "username=" + user
						+ appPassword)),
				new NameValuePair("username", user),
				new NameValuePair("password", psw),
				 };
		Log.i("flag","appUser1----->"+appUser1);
		Log.i("flag","deviceId----->"+deviceId);
		Log.i("flag","user----->"+user);
		Log.i("flag","psw----->"+psw);
		method.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		method.setRequestBody(param);
		try {
			statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK) {
				ins = method.getResponseBodyAsStream();
				byte[] b = new byte[1024];
				int r_len = 0;
				while ((r_len = ins.read(b)) > 0) {
					sb.append(new String(b, 0, r_len, method
							.getResponseCharSet()));
				}
				result = sb.toString();
				Log.i("333",result);
				try {
					jsonObj = JSONObject.fromString(result);
					if (jsonObj != null) {
						//status、message、operatorId
						status = jsonObj.getString("status");
						Log.i("flag","status--->"+ status);
						msg1 = jsonObj.optString("message");
						operatorId = jsonObj.optString("userId");
						Log.i("flag","msg1--->"+msg1);
						if (status.equals("success")) {
							//showMessage(AppData.PROMPT_QUERY, msg1, true);
							Looper.prepare();
							statusCode = HttpStatus.SC_OK;
							Log.i("flag","statusCode#######"+ statusCode);
							Toast.makeText(FttHssLoginActivity.this,msg1, Toast.LENGTH_SHORT).show();
							//showMessage(AppData.PROMPT_QUERY, msg1, true)
							Intent intent = new Intent();
							intent.setClass(FttHssLoginActivity.this,
									Scan_Writer.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							Info_bean info_bean = new Info_bean(user,appUser,appPassword,deviceId,signature
									,operatorId);
							Log.i("1111",info_bean.toString());
							intent.putExtra("info",info_bean);
							startActivity(intent);
							Looper.loop();
						} else {
							//showMessage(AppData.PROMPT_WARN, msg1, false);
							Looper.prepare();
							statusCode = HttpStatus.SC_METHOD_FAILURE;
							Toast.makeText(FttHssLoginActivity.this,msg1, Toast.LENGTH_SHORT).show();
							//showMessage(AppData.PROMPT_QUERY, msg1, true);
							Looper.loop();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					statusCode = HttpStatus.SC_METHOD_FAILURE;
					showMessage(AppData.PROMPT_WARN, "登录信息异常，请重新登录。", false);
				}
			} else {
				System.err.println("Response Code: " + statusCode);
			}
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
	//注册时请求。
	public int PostRequestzhuce() throws HttpException, IOException {
		HttpClient client = new HttpClient();
		StringBuilder sb = new StringBuilder();
		InputStream ins = null;
		int statusCode = HttpStatus.SC_METHOD_FAILURE;
		JSONObject jsonObj = null;
		//注册时网址。
		PostMethod method = new PostMethod(Url.register);
		Log.i("flag","到这里了");
		//设备注册时deviceId、signature、username、password
		NameValuePair[] param = {
				new NameValuePair("deviceId", deviceId),
				new NameValuePair("signature", MD5.GetMD5Code("deviceId="
						+ deviceId + "password=" + psw + "username=" + user
						+ "runhengfeng")),
				new NameValuePair("username", user),
				new NameValuePair("password", psw),
				 };
		Log.i("flag","注册时账户："+user+"注册时密码："+psw+"设备id"+deviceId);
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
				result = sb.toString();
				Log.i("flag","result---------"+result);
				try {
					//从服务器返回的JSON字符串。解析。
					jsonObj = JSONObject.fromString(result);
					if (jsonObj != null) {
						String status = jsonObj.getString("status");
						msg2 = jsonObj.optString("message");
						appUser = jsonObj.optString("appUser");
						Log.i("flag","appUser------>"+appUser);
						appPassword = jsonObj.optString("appPassword");
						Log.i("flag","appPassword----->"+appPassword);
						//appUser,appPassword服务器获得，登录时需要。
						editor.putString("appUser", appUser);
						editor.putString("appPassword", appPassword);
						editor.commit();
						Log.i("flag",status+msg2+appUser);
						if (status.equals("success")) {
							Log.i("flag","msg2--->"+msg2);
							Looper.prepare();
						Toast.makeText(FttHssLoginActivity.this,msg2, Toast.LENGTH_SHORT).show();
							//showMessage(AppData.PROMPT_QUERY, msg2, true);
							Looper.loop();
						} else {
							Looper.prepare();
							statusCode = HttpStatus.SC_METHOD_FAILURE;
							Toast.makeText(FttHssLoginActivity.this,msg2, Toast.LENGTH_SHORT).show();
							//showMessage(AppData.PROMPT_WARN, msg2, false);
							Looper.loop();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					statusCode = HttpStatus.SC_METHOD_FAILURE;
					showMessage(AppData.PROMPT_WARN, "登录信息异常，请重新登录。", false);
				}
			} else {
				System.err.println("Response Code: " + statusCode);
			}
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
}
