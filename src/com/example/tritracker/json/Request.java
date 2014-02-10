package com.example.tritracker.json;

import com.google.gson.Gson;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Request<T> extends Thread {
	private int error = 0;
	private String url = "";
	private JSONcallback<T> callback;
	private final Class<T> type;

	public interface JSONcallback<T> {
		public void run(T r, int error);
	}

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
			T result = null;

			if (responseString != null)
				result = new Gson().fromJson(responseString, type);

			callback.run(result, error);
		}
	}

	public Request(Class<T> type, JSONcallback<T> call, String url) {
		this.url = url;
		this.callback = call;
		this.type = type;
		this.setName("JSON Request - " + type.toString());
	}

	public boolean hasFailed() {
		return error != 0;
	}
}