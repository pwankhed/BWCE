package com.ceva.eai.bwce.generic.ftp;

import java.io.Serializable;

public class FtpConnection implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String Host;
	private int Port;
	private String Username;
	private String Password;
	
	public String getHost() {
		return Host;
	}
	public void setHost(String host) {
		this.Host = host;
	}
	public int getPort() {
		return Port;
	}
	public void setPort(int port) {
		this.Port = port;
	}
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		this.Username = username;
	}
	public String getPassword() {
		return Password;
	}
	public void setPassword(String password) {
		this.Password = password;
	}
	
	public FtpConnection() {

	}	

}

