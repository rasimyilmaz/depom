package com.rasimyilmaz.depom;
//example.androidhive
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

public class JSONParser2 {

	static JSONObject jObj = null;

	// constructor
	public JSONParser2() {

	}
	// Volley Function
	private JSONObject getData(Context context, String url, String method){
		// Instantiate the RequestQueue.
		RequestQueue queue = MySingleton.getInstance(context).getRequestQueue();//Volley.newRequestQueue(this)
		// Request a string response from the provided URL.
		Integer methodInt;
		switch (method){
			case "GET":
				methodInt=Request.Method.GET;
				break;
			case "PUT":
				methodInt=Request.Method.PUT;
				break;
			default:
				methodInt=0;
		}

		jsonObjectRequest.setTag(MainActivity.TAG);
		// Add the request to the RequestQueue.
		queue.add(jsonObjectRequest);
		//MySingleton.getInstance (this).addToRequestQueue(stringRequest);

	}
}
