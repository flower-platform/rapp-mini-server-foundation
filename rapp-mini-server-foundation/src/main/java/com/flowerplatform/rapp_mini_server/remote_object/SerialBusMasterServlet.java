package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.diozero.api.DigitalOutputDevice;
import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class SerialBusMasterServlet extends HttpServlet {

	private static final String SERIAL_NODE_ID_PREFIX = "serial/";

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getGlobal();
	
	private Semaphore semaphore = new Semaphore(1,  true);

	private SerialPort port;

	private DigitalOutputDevice writeEnableOutput;
	
	public SerialBusMasterServlet(String serialPortName, int baudRate, int writeEnablePin, int timeoutMillis) throws IOException {
		openPort(serialPortName, baudRate, writeEnablePin, timeoutMillis);
	}

	private void openPort(String portName, int baudRate, int writeEnablePin, int timeoutMillis) throws IOException {
    	logger.log(Level.FINE, "Opening port " + portName + "...");
    	try {
        	CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);  
            port = (SerialPort) portIdentifier.open("esp8266-updater", 2000);  
            port.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
            port.enableReceiveTimeout(timeoutMillis);
        	logger.log(Level.FINE, "Port open.");
        } catch (PortInUseException | UnsupportedCommOperationException | NoSuchPortException e) {
        	logger.log(Level.SEVERE, "Failed opening port.", e);
        	throw new IOException(e);
        }
    	if (writeEnablePin != -1) { // rs485
    		System.setProperty("PIGPIOD_HOST", "localhost");
    		System.setProperty("PIGPIOD_PORT", "8888");
    		writeEnableOutput = new DigitalOutputDevice(writeEnablePin);
    	}
    }

	private void clearInputBuffer() throws IOException {
		InputStream in = port.getInputStream();
		byte[] buf = new byte[128];
		while (in.available() > 0) {
			in.read(buf);
		}
	}

	private void sendData(byte[] data) throws IOException {
		clearInputBuffer();
		if (writeEnableOutput != null) { // rs485
			try { Thread.sleep(1); } catch (Exception e) { } // clients release rs485 bus 1ms after flushing (Serial.flush() bug of esp8266); must wait 1ms
			writeEnableOutput.on();
		}
		OutputStream out = port.getOutputStream();
//		TODO CM: not sending data length any more
//		out.write((data.length >> 8) & 0xFF);
//		out.write(data.length & 0xFF);
		out.write(data);
		out.flush();
		if (writeEnableOutput != null) {
			writeEnableOutput.off();
		}
	}

	private byte[] receiveData() {
		byte[] res = null;
		try {
			InputStream in = port.getInputStream();
			System.out.println("receiving data...");
			int size, c;
			if ((c = in.read()) == -1) {
				return null;
			}
			size = (c & 0xFF) << 8;
			if ((c = in.read()) == -1) {
				return null;
			}
			size |= c & 0xFF;
			System.out.println("size: " + size);

			res = new byte[size];
			int i = 0, n;
			while (i < size) {
				n = in.read(res,  i,  size - i);
				if (n == 0) {
					return null;
				}
				i += n;
			}
			System.out.println("received: " + new String(res));
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return res;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getContentLength() == 0) {
			return;
		}
		byte[] buf = new byte[request.getContentLength()];
		DataInputStream in = new DataInputStream(request.getInputStream());
		in.readFully(buf);
		String rawPacket = new String(buf);
		
		System.out.println("-> " + rawPacket);
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		
		FlowerPlatformRemotingProtocolPacket res = null;
		switch (packet.getCommand()) {
		case 'I': // invoke
			try { 
				semaphore.acquire(); 
				String nodeId = packet.nextField();
				if (nodeId.startsWith(SERIAL_NODE_ID_PREFIX)) {
					packet.setField(0, nodeId.substring(SERIAL_NODE_ID_PREFIX.length()));
				}
				sendData(packet.getRawData().getBytes());
				byte[] busResponse = receiveData();
				if (busResponse != null) {
					System.out.println(new String(busResponse));
					res = new FlowerPlatformRemotingProtocolPacket(new String(busResponse));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				semaphore.release();
			}
			break;
		}	
		
		if (res == null) {
			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'X');
		}
		
		System.out.println("<- " + res.getRawData());
		response.getWriter().print(res.getRawData());
	}
	
}
