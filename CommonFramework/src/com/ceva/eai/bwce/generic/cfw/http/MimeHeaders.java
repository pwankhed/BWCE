package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MimeHeaders implements Serializable {

	private static final long serialVersionUID = 7691074651675218415L;
	private List<TransportHeader> mimeHeader = new ArrayList<>();
	private String contentId;
	private String contentType;
	private String contentDisposition;
	private String contentEncoding;
	private String contentTransferEncoding;

	
	public List<TransportHeader> getMimeHeader() {
		return mimeHeader;
	}
	
	public void setMimeHeader(List<TransportHeader> mimeHeader) {
		this.mimeHeader = mimeHeader;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentDisposition() {
		return contentDisposition;
	}

	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getContentTransferEncoding() {
		return contentTransferEncoding;
	}

	public void setContentTransferEncoding(String contentTransferEncoding) {
		this.contentTransferEncoding = contentTransferEncoding;
	}
	
	public String getContentId() {
		return contentId;
	}
	
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

}
