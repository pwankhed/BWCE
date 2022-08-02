package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;

public class ContentProvider implements Serializable {

	private static final long serialVersionUID = -1177348804752694119L;
	private String contentName;
	private byte[] contentBinary;
	private String contentText;
	private String contentFile;
	private String contentDocumentId;

	public String getContentName() {
		return contentName;
	}
	
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}
	
	public byte[] getContentBinary() {
		return contentBinary;
	}

	public void setContentBinary(byte[] contentBinary) {
		this.contentBinary = contentBinary;
	}

	public String getContentText() {
		return contentText;
	}

	public void setContentText(String contentText) {
		this.contentText = contentText;
	}

	public String getContentFile() {
		return contentFile;
	}

	public void setContentFile(String contentFile) {
		this.contentFile = contentFile;
	}

	public String getContentDocumentId() {
		return contentDocumentId;
	}

	public void setContentDocumentId(String contentDocumentId) {
		this.contentDocumentId = contentDocumentId;
	}
}
