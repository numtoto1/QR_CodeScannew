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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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


import org.apache.http.HttpRequest;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

public class MainActivity extends Activity {
	private final static int SCANNIN_GREQUEST_CODE = 1;

	private EditText mResult_num;
	private Button mButton,btn_submit;
	private String name = null;
	private Info_bean info;
	private ProgressDialog dialog;
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
			Log.e("flag","json--->"+jsonBean.toString());
			if (jsonBean.getStatus().equals("success")){
				showMessage( AppData.PROMPT_SUCCESS,true);
				Toast.makeText(MainActivity.this,jsonBean.getMessage(),Toast.LENGTH_SHORT).show();
			}else {
				showMessage( AppData.PROMPT_ERROR,true);
				Toast.makeText(MainActivity.this,jsonBean.getMessage(),Toast.LENGTH_SHORT).show();
			}

		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = getIntent();
		info = (Info_bean) intent.getSerializableExtra("info");
		initView();
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MipcaActivityCapture.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
			}
		});
		//点击提交
		btn_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String num=mResult_num.getText().toString().trim();
				if (num != null) {
					PostData(num);
				}else{
					Toast.makeText(MainActivity.this, "请扫描单号", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void PostData(String numble) {//上传
		String deviceId=info.getDeviceId();
		String operatorId=info.getOperatorId();
		String appPassword = info.getAppPassword();
		String signature=MD5.GetMD5Code("deviceId="
				+ deviceId +"numble="+numble+ "userId=" + operatorId
				+ appPassword);
		SharedPreferences sp = getSharedPreferences("crazyit", MODE_WORLD_READABLE);
		String appUser  =  sp.getString("appUser",null);
		Log.i("spappUser",appUser);
		RequestParams params = new RequestParams();
		params.addBodyParameter("appUser", appUser);
		params.addBodyParameter("deviceId", deviceId);
		params.addBodyParameter("signature", signature);
		params.addBodyParameter("userId", operatorId);
		params.addBodyParameter("numble", numble);
		HttpUtils http = new HttpUtils();
		http.send(com.lidroid.xutils.http.client.HttpRequest.HttpMethod.POST,
				"http://192.168.10.154/api/sign.html",
				params,
				new RequestCallBack<String>() {
					@Override
					public void onStart() {
					}

					@Override
					public void onLoading(long total, long current, boolean isUploading) {
						if (isUploading) {
							dialog.show();
						}
					}

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
							dialog.dismiss();
						//TODO 成功以后。
						Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
						Log.i("falg","---->"+responseInfo.toString());
						Message msg = Message.obtain();
						msg.obj = responseInfo.result;
						handler.sendMessage(msg);
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						dialog.dismiss();
						Toast.makeText(MainActivity.this,"上传失败，请检查网络。",Toast.LENGTH_LONG).show();
					}
				});
	}


	private void initView() {
		mResult_num = (EditText) findViewById(R.id.result);
		btn_submit= (Button) findViewById(R.id.btn_submit);
		mButton = (Button) findViewById(R.id.button1);
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
		dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
		dialog.setIcon(R.drawable.ic_launcher);//
		dialog.setMessage("稍等...");
		soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap<Integer, Integer>();
		soundPoolMap.put(1,soundPool.load(MainActivity.this, R.raw.success, 1));
		soundPoolMap.put(2,soundPool.load(MainActivity.this, R.raw.query, 1));
		soundPoolMap.put(3,soundPool.load(MainActivity.this, R.raw.error, 1));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case SCANNIN_GREQUEST_CODE:
				if (resultCode == RESULT_OK) {
					Bundle bundle = data.getExtras();
					mResult_num.setText(bundle.getString("result"));
				}
				break;
		}
	}
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File f = new File(filePath);
			f.delete(); // 删除空文件夹
		} catch (NullPointerException e) {
		} catch (Exception e) {
		}
	}
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile() && !temp.toString().contains("appconfig")) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	@Override
	protected void onDestroy() {
		//delFolder(sign_dir);
		super.onDestroy();
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
