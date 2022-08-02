package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;

public class FormField implements Serializable {

	private static final long serialVersionUID = -8796666903160517827L;
	private String fieldName;
	private ContentProvider fieldContent;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public ContentProvider getFieldContent() {
		return fieldContent;
	}

	public void setFieldContent(ContentProvider fieldContent) {
		this.fieldContent = fieldContent;
	}


}