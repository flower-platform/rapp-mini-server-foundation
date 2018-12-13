package com.crispico.flower_platform.remote_object;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.crispico.flower_platform.remote_object.shared.IRequestSender;
import com.crispico.flower_platform.remote_object.shared.IScheduler;
import com.crispico.flower_platform.remote_object.shared.ResponseCallback;

public class JavaRemoteObjectBase implements IRequestSender, IScheduler {

	private ExecutorService threadPool = Executors.newSingleThreadExecutor();
	 
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 
	@Override
	public void sendRequest(String url, String payload, ResponseCallback callback) {
		threadPool.submit(new HttpRequestTask(url, payload, callback));
	}
	
	private ScheduledFuture<?> scheduledFuture;
	
	@Override
	public void schedule(Runnable task, int millis) {
		clear();
		scheduledFuture = scheduler.schedule(task, millis, TimeUnit.MILLISECONDS);
	}

	public void clear() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
	}
	
	class HttpRequestTask implements Runnable {

		private String url;
		
		private String payload;

		private ResponseCallback callback;
		
		public HttpRequestTask(String url, String payload, ResponseCallback callback) {
			this.url = url;
			this.payload = payload;
			this.callback = callback;
		}

		@Override
		public void run() {
			HttpURLConnection conn;
			InputStream in = null;
			try {
				conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.getOutputStream().write(payload.getBytes());
				conn.connect();
				conn.setReadTimeout(5000);
				in = conn.getInputStream();
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				byte[] data = new byte[16384];
				int n = 0;
				while ((n = in.read(data)) != -1) {
					buf.write(data, 0, n);
				}
				String respData = new String(buf.toByteArray());
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					callback.onSuccess(respData);
				} else {
					callback.onError(respData);
				}
				in.close();
			} catch (IOException e) {
				callback.onError(e.toString());
			} finally {
				if (in != null) {
					try { in.close(); } catch (Exception e) { e.printStackTrace(); }
				}
			}
		}
		
	}

}
