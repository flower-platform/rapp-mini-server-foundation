package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Cristian Spiescu
 */
public class FlowerPlatformRemotingProtocolScanner implements Closeable {

	protected static final String FLOWER_PLATFORM_REMOTING_PROTOCOL = "FPRP";
	
	protected static final int PROTOCOL_VERSION = 1;
	
	protected Scanner scanner;

//	protected BufferedReader reader;

	public FlowerPlatformRemotingProtocolScanner(InputStream inputStream) {
		super();
//		try {
//			reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//		} catch (UnsupportedEncodingException e1) {
//			throw new RuntimeException(e1);
//		}
		scanner = new Scanner(inputStream);
		scanner.useDelimiter("\0");
		if (!FLOWER_PLATFORM_REMOTING_PROTOCOL.equals(next())) {
			throw new IllegalArgumentException("The message doesn't start with the header: " + FLOWER_PLATFORM_REMOTING_PROTOCOL);
		}
		int version = -1;
		String versionStr = next();
		try {
			version = Integer.parseInt(versionStr);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Cannot parse protocol version: " + versionStr);
		}
		if (version != PROTOCOL_VERSION) {
			throw new IllegalArgumentException("Incorrect protocol version. Expected: " + PROTOCOL_VERSION + ", but received: " + version);
		}
	}
	
	public String next() {
		return scanner.next();
//		StringBuilder sb = new StringBuilder();
//		try {
//			while (true) {
//				int c = reader.read();
//				if (c == -1 || c == 0) {
//					break;
//				}
//				sb.append((char) c);
//			}
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		return sb.toString();
	}
	
	@Override
	public void close() throws IOException {
		scanner.close();
	}
	
}
