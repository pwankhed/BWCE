package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;

public class TransportHeader implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String value;
	private String type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
