package com.example.qr_codescan;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import net.sf.json.JSONArray;


import java.util.UUID;

public class AppData extends Application {

	public static final boolean D = false;
	public static enum ServerOrCilent{
		NONE,
		SERVICE,
		CILENT
	};
    public static String BlueToothAddress = "null";
    public static ServerOrCilent serviceOrCilent = ServerOrCilent.NONE;
    public static boolean isOpen = false;
    public static boolean isReadWeight = true;
	private RequestQueue queues;
	// ScaleActivity使用
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int POST_RESULT = 6;
	public static final int RESTART_BLUETOOTH = 7;

	// Prompting message types sent from Activity
	public static final int PROMPT_SUCCESS = 7;
	public static final int PROMPT_SUCCESS2 = 11;
	public static final int PROMPT_QUERY = 8;
	public static final int PROMPT_WARN = 9;
	public static final int PROMPT_ERROR = 10;
	
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	public static final int START_BLUETOOTH = 3;

	// BluetoothService使用
	// Name for the SDP record when creating server socket
	public static final String NAME = "QSYBTW";// 全时运蓝牙秤

	// Unique UUID for this application
	// private static final UUID MY_UUID =
	// UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// private static final UUID MY_UUID =
	// UUID.fromString("896db752-44cc-4dda-954b-57dca98db571");

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	private static AppData instance = null;

	private String token="0123456789ABCDEF";
	private String codeflag="RHF";
	private String exitcode="0000";
	private String scncode="";
	
	private boolean isScanOpen = false; // 是否已打开扫描设置
	private boolean isStartOpen = false;	
	
	private boolean bAccepting=false;
	private boolean bPaired=false;//true for Debug , default is false
	private static final String TAG = "AppData";
	private TextView tvdbg=null;
	private EditText etwght=null;
	private EditText etscode=null;
	private JSONArray permissionarr=null;
	private String weight = null;
	QSYDataManager sqlmgr = null;
	
	private Object lock = new Object();
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}

	public RequestQueue getRequestQueue(){
		if (queues == null) {
			queues = Volley.newRequestQueue(getApplicationContext());
		}
		return queues;
	}
	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		VolleyLog.d("Adding request to queue: %s", req.getUrl());
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (queues != null) {
			queues.cancelAll(tag);
		}
	}

	
	public void finishActivity(final Activity acty, String stitle, String syes, String sno)
	{
        new AlertDialog.Builder(acty).setTitle(stitle)
        .setIcon(android.R.drawable.ic_dialog_info) 
        .setPositiveButton(syes, new DialogInterface.OnClickListener() {
     
            @Override
            public void onClick(DialogInterface dialog, int which) {
            // 点击“确认”后的操作 
            	acty.finish();      
            } 
        }) 
        .setNegativeButton(sno, new DialogInterface.OnClickListener() {
     
            @Override
            public void onClick(DialogInterface dialog, int which) {
            // 点击“返回”后的操作,这里不设置没有任何操作 
            } 
        }).show();    	
	}
	
	@SuppressWarnings("deprecation")
	public void exitApp()
	{
		//sqlmgr.exitQSYDataManager();
		
		int currentVersion = android.os.Build.VERSION.SDK_INT;  
        if (currentVersion > android.os.Build.VERSION_CODES.ECLAIR_MR1) {  
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);  
            System.exit(0);
        } else {// android2.1  
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            am.restartPackage(getPackageName());  
        } 	
	}
	
	public static synchronized AppData getInstance() {
		if (null == instance) {
			instance = new AppData();
		}
		return instance;
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AppData.MESSAGE_TOAST:{
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(AppData.TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
			case AppData.POST_RESULT: {
				synchronized (tvdbg) {
					if(tvdbg!=null)tvdbg.setText((CharSequence) ((String) msg.obj));
				}
				break;
			}
			case AppData.MESSAGE_READ: {
				try {
					String readBuf = (String) msg.obj;
					int length = readBuf.length();
					StringBuffer stringBuffer = new StringBuffer();
					if (readBuf != null && length > 0) {
						for (int i = 0; i < length; i++) {
							char c = readBuf.charAt(length - 1 - i);
							stringBuffer.append(c);
						}

						double dbl = Double.valueOf(stringBuffer.toString());
						synchronized (etwght){
							if(etwght!=null)etwght.setText(String.valueOf(dbl));
						}
						isReadWeight = false;
						this.removeMessages(AppData.MESSAGE_READ);
					}
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			}
		}
	};
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCodeflag() {
		return codeflag;
	}

	public void setCodeflag(String codeflag) {
		this.codeflag = codeflag;
	}

	public synchronized TextView getTvdbg() {
		return tvdbg;
	}
	public synchronized void setTvdbg(TextView tvdbg) {
		this.tvdbg = tvdbg;
	}
	public synchronized EditText getEtwght() {
		return etwght;
	}
	public synchronized void setEtwght(EditText etwght) {
		this.etwght = etwght;
	}
	public synchronized EditText getEtscode() {
		return etscode;
	}
	public synchronized void setEtscode(EditText etscode) {
		this.etscode = etscode;
	}

	public synchronized boolean isbAccepting() {
		return bAccepting;
	}
	public synchronized void setbAccepting(boolean bAccepting) {
		this.bAccepting = bAccepting;
	}	
	public synchronized boolean isbPaired() {
		return bPaired;
	}
	public synchronized void setbPaired(boolean bPaired) {
		this.bPaired = bPaired;
	}
	public synchronized String getScncode() {
		return scncode;
	}
	public synchronized void setScncode(String scncode) {
		this.scncode = scncode;
	}	
	public synchronized boolean isScanOpen() {
		return isScanOpen;
	}
	public synchronized void setScanOpen(boolean isScanOpen) {
		this.isScanOpen = isScanOpen;
	}
	public synchronized boolean isStartOpen() {
		return isStartOpen;
	}
	public synchronized void setStartOpen(boolean isStartOpen) {
		this.isStartOpen = isStartOpen;
	}	
	public Object getLock() {
		return lock;
	}
	public void setLock(Object lock) {
		this.lock = lock;
	}
	public Handler getmHandler() {
		return mHandler;
	}
	public JSONArray getPermissionarr() {
		return permissionarr;
	}
	public void setPermissionarr(JSONArray permissionarr) {
		this.permissionarr = permissionarr;
	}
	public String getExitcode() {
		return exitcode;
	}
	public void setExitcode(String exitcode) {
		this.exitcode = exitcode;
	}
	public QSYDataManager getSqlmgr() {
		return sqlmgr;
	}
	public void setSqlmgr(QSYDataManager sqlmgr) {
		this.sqlmgr = sqlmgr;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}	
}
