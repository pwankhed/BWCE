package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;

public class HttpProxy implements Serializable {
	private static final long serialVersionUID = 1L;

	private String proxyURL;
	private String userName;
	private String password;
	private boolean nonPreemptive;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProxyURL() {
		return proxyURL;
	}

	public void setProxyURL(String proxyURL) {
		this.proxyURL = proxyURL;
	}

	public boolean isNonPreemptive() {
		return nonPreemptive;
	}

	public void setNonPreemptive(boolean nonPreemptive) {
		this.nonPreemptive = nonPreemptive;
	}
}
