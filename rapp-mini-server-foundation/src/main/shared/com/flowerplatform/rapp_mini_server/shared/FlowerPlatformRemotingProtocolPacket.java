package com.flowerplatform.rapp_mini_server.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlowerPlatformRemotingProtocolPacket {

	public static final String PROTOCOL_HEADER = "FPRP";
	
	public static final String VERSION = "1";
	
	private static final char TERM = '\0';
	
	private static final char EOT = (char) 0x04;
			
	
	private List<String> packetFields;
	
	private String securityToken;
	
	private char command;
	
	private int currentFieldIndex;
	
	
	public FlowerPlatformRemotingProtocolPacket(String securityToken, char command) {
		this.securityToken = securityToken;
		this.command = command;
		packetFields = new ArrayList<>();
	}
	
	public FlowerPlatformRemotingProtocolPacket(String rawPacket) {
		char t = rawPacket.charAt(rawPacket.length() - 1);
		if (t != EOT) {
			throw new IllegalArgumentException("Invalid packet terminator char: " + t); 
		}
		rawPacket = rawPacket.substring(0,  rawPacket.length() - 1);
		packetFields = Arrays.asList(rawPacket.split("\0"));
		if (packetFields.size() < 4) {
			throw new RuntimeException("Invalid packet. Raw data: " + rawPacket);
		}
		if (!packetFields.get(0).equals(PROTOCOL_HEADER)) {
			throw new IllegalArgumentException("Invalid packet Header: " + packetFields.get(0)); 
		}
		if (!packetFields.get(1).equals(VERSION)) {
			throw new IllegalArgumentException("Unsupported protocol version: " + packetFields.get(0)); 
		}
		securityToken = packetFields.get(2);
		command = packetFields.get(3).charAt(0);
		currentFieldIndex = 4;
	}
	
	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public char getCommand() {
		return command;
	}

	public void setCommand(char command) {
		this.command = command;
	}
	
	public String nextField() {
		if (currentFieldIndex >= packetFields.size()) {
			return null;
		}
		return packetFields.get(currentFieldIndex++);
	}

	public int availableFieldCount() {
		return packetFields.size() - currentFieldIndex; 
	}

	public void addField(String fieldValue) {
		packetFields.add(fieldValue);
	}
	
	public String getRawData() {
		StringBuilder sb = new StringBuilder();
		sb.append(PROTOCOL_HEADER);
		sb.append(TERM);
		sb.append(VERSION);
		sb.append(TERM);
		sb.append(securityToken);
		sb.append(TERM);
		sb.append(command);
		sb.append(TERM);
		for (String s : packetFields) {
			sb.append(s);
			sb.append(TERM);
		}
		sb.append(EOT);
		return sb.toString();
	}
	
}
