package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;

public class MimePart implements Serializable {

	private static final long serialVersionUID = -8270481213503609586L;
	private MimeHeaders mimeHeaders = new MimeHeaders();
	private ContentProvider content;

	public MimeHeaders getMimeHeaders() {
		return mimeHeaders;
	}

	public void setMimeHeaders(MimeHeaders mimeHeaders) {
		this.mimeHeaders = mimeHeaders;
	}

	public ContentProvider getContent() {
		return content;
	}

	public void setContent(ContentProvider content) {
		this.content = content;
	}

}
