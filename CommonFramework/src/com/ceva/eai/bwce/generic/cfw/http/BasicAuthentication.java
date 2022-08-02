package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;

public class BasicAuthentication implements Serializable {

	private static final long serialVersionUID = -3663617762079567302L;
	private String authenticationRealm;
	private String userName;
	private String password;
	private boolean nonPreemptive;

	public String getAuthenticationRealm() {
		return authenticationRealm;
	}

	public void setAuthenticationRealm(String authenticationRealm) {
		this.authenticationRealm = authenticationRealm;
	}

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

	public boolean isNonPreemptive() {
		return nonPreemptive;
	}

	public void setNonPreemptive(boolean nonPreemptive) {
		this.nonPreemptive = nonPreemptive;
	}

}
