package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;

public class QueryParamField implements Serializable {

	private static final long serialVersionUID = 7972689875417413297L;
	private String fieldName;
	private String fieldText;
	private byte[] fieldBinary;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldText() {
		return fieldText;
	}

	public void setFieldText(String fieldText) {
		this.fieldText = fieldText;
	}

	public byte[] getFieldBinary() {
		return fieldBinary;
	}

	public void setFieldBinary(byte[] fieldBinary) {
		this.fieldBinary = fieldBinary;
	}

}
