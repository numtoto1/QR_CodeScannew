package com.example.qr_codescan.utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 
 * 对应MainActivity.java中的JsonObjectRequest-POST2:
 * 如果服务端并不支持json的请求方式,比如常见的spring mvc服务端,就很难支持json的请求方式,
 * 那么就需要客户端以普通的post方式进行提交,服务端返回json串
 * 首先在Activity类里,继承Request实现一个NormalPostRequest类
 * 
 * @author jiatao
 *
 */
public class NormalPostRequest extends Request<JSONObject> {

	private Map<String, String> mMap;
    private Listener<JSONObject> mListener;
    
    public NormalPostRequest(String url, Map<String, String> map, Listener<JSONObject> listener, ErrorListener errorListener) {
        super(Request.Method.POST, url, errorListener);
        mListener = listener;
        mMap = map;
    }
     
    //mMap是已经按照前面的方式,设置了参数的实例
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mMap;
    }
     
    //此处因为response返回值需要json数据,和JsonObjectRequest类一样即可
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }

}
