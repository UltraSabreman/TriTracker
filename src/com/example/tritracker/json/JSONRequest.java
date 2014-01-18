package com.example.tritracker.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.example.tritracker.Util.JSONcallback;
import com.google.gson.Gson;

public class JSONRequest extends Thread {
	private int error = 0;
	private String url = "";
	private JSONcallback callback;
	
	public void run() {
		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
		HttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpResponse response;
		String responseString = null;
		try {
			response = httpclient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ConnectTimeoutException e) {
			error = 1;
		} catch (NoHttpResponseException e) {
			error = 2;
		} catch (ClientProtocolException e) {
			error = 3;
		} catch (IOException e) {
			error = 4;
		}

		if (callback != null) {
			JSONResult result = null;
			
			if (responseString != null)
				result = new Gson().fromJson(responseString, JSONResult.class);		
			
			callback.run(result, error);
		}
	}

	public JSONRequest(JSONcallback call, String url) {
		this.url = url;
		this.callback = call;
		this.setName("JSON Request");
	}
	
	public boolean hasFailed() {
		return error != 0;
	}
}