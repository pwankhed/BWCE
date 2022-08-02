package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MultiPartFormField implements Serializable {

	private static final long serialVersionUID = -430253704991969799L;
	private List<TransportHeader> header = new ArrayList<>();	
	private String fieldName;
	//private String fieldFileName;
	private String fieldType;
	private ContentProvider fieldContent;
		
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getFieldType() {
		return fieldType;
	}
	
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
		
	public List<TransportHeader> getHeader() {
		return header;
	}
	public void setHeader(List<TransportHeader> header) {
		this.header = header;
	}

	public ContentProvider getFieldContent() {
		return fieldContent;
	}

	public void setFieldContent(ContentProvider fieldContent) {
		this.fieldContent = fieldContent;
	}
	
}