package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransportSecurity implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<String> TLSSupportedProtocol = new ArrayList<>();
	private List<String> TLSSupportedCipher = new ArrayList<>();
	private List<String> TLSDisabledCipher = new ArrayList<>();
	private boolean trustAll;
	private String keyStoreURL;
	private String keyStoreType;
	private String keyStorePassword;
	private String thrustStoreURL;
	private String thrustStoreType;
	private String thrustStorePassword;

	public List<String> getTLSSupportedProtocol() {
		return TLSSupportedProtocol;
	}

	public void setTLSSupportedProtocol(List<String> tLSSupportedProtocol) {
		TLSSupportedProtocol = tLSSupportedProtocol;
	}

	public List<String> getTLSSupportedCipher() {
		return TLSSupportedCipher;
	}

	public void setTLSSupportedCipher(List<String> tLSSupportedCipher) {
		TLSSupportedCipher = tLSSupportedCipher;
	}

	public List<String> getTLSDisabledCipher() {
		return TLSDisabledCipher;
	}

	public void setTLSDisabledCipher(List<String> tLSDisabledCipher) {
		TLSDisabledCipher = tLSDisabledCipher;
	}

	public boolean isTrustAll() {
		return trustAll;
	}

	public void setTrustAll(boolean trustAll) {
		this.trustAll = trustAll;
	}

	public String getKeyStoreURL() {
		return keyStoreURL;
	}

	public void setKeyStoreURL(String keyStoreURL) {
		this.keyStoreURL = keyStoreURL;
	}

	public String getKeyStoreType() {
		return keyStoreType;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getThrustStoreURL() {
		return thrustStoreURL;
	}

	public void setThrustStoreURL(String thrustStoreURL) {
		this.thrustStoreURL = thrustStoreURL;
	}

	public String getThrustStoreType() {
		return thrustStoreType;
	}

	public void setThrustStoreType(String thrustStoreType) {
		this.thrustStoreType = thrustStoreType;
	}

	public String getThrustStorePassword() {
		return thrustStorePassword;
	}

	public void setThrustStorePassword(String thrustStorePassword) {
		this.thrustStorePassword = thrustStorePassword;
	}

}
