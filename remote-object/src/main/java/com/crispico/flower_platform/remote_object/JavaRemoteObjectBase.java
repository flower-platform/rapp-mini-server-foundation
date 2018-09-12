package com.crispico.flower_platform.remote_object;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.flowerplatform.rapp_mini_server.shared.IRequestSender;
import com.flowerplatform.rapp_mini_server.shared.IScheduler;
import com.flowerplatform.rapp_mini_server.shared.ResponseCallback;

public class JavaRemoteObjectBase implements IRequestSender, IScheduler {

	private ExecutorService threadPool = Executors.newSingleThreadExecutor();
	 
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 
	@Override
	public void sendRequest(String url, String payload, ResponseCallback callback) {
		threadPool.submit(new HttpRequestTask(url, payload, callback));
	}

	@Override
	public void schedule(Runnable task, int millis) {
		scheduler.schedule(task, millis, TimeUnit.MILLISECONDS);
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
				conn.getOutputStream().write(payload.getBytes());
				conn.connect();
				in = conn.getInputStream();
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				byte[] data = new byte[16384];
				while (in.available() >= 0) {
					int n = in.read(data);
					buf.write(data, 0, n);
				}
				callback.onSuccess(new String(buf.toByteArray()));
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
