package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HttpClientRequest implements Serializable {

	private static final long serialVersionUID = -4235137799516766399L;

	private String httpVersion;
	private String httpCookiePolicy;
	private String httpVirtualHost;
	private String method;
	private String baseURL;
	private ContentProvider content;
	private List<FormField> contentFormField = new ArrayList<>();
	private List<MultiPartFormField> contentMultiPartFormField = new ArrayList<>();
	private MimeEnvelope mimeEnvelope = new MimeEnvelope();
	private String queryString;
	private List<QueryParamField> queryParamField = new ArrayList<>();
	private BasicAuthentication basicAuthentication;
	private boolean followRedirect;
	private HttpProxy httpProxy;
	private HttpHeaders requestHeaders = new HttpHeaders();
	private int requestTimeoutMS;
	private int connectionTimeoutMS;
	private TransportSecurity transportSecurity;
	
	//private int socketTimeoutMS;
	//private int retryCount;
	//private String host;
	
	

	public String getHttpVersion() {
		return httpVersion;
	}

	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}

	public String getHttpCookiePolicy() {
		return httpCookiePolicy;
	}

	public void setHttpCookiePolicy(String httpCookiePolicy) {
		this.httpCookiePolicy = httpCookiePolicy;
	}

	public String getHttpVirtualHost() {
		return httpVirtualHost;
	}

	public void setHttpVirtualHost(String httpVirtualHost) {
		this.httpVirtualHost = httpVirtualHost;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}


	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public List<QueryParamField> getQueryParamField() {
		return queryParamField;
	}

	public void setQueryParamField(List<QueryParamField> queryParamField) {
		this.queryParamField = queryParamField;
	}

	public List<FormField> getContentFormField() {
		return contentFormField;
	}

	public void setContentFormField(List<FormField> contentFormField) {
		this.contentFormField = contentFormField;
	}

	public List<MultiPartFormField> getContentMultiPartFormField() {
		return contentMultiPartFormField;
	}

	public void setContentMultiPartFormField(List<MultiPartFormField> contentMultiPartFormField) {
		this.contentMultiPartFormField = contentMultiPartFormField;
	}
	
	public BasicAuthentication getBasicAuthentication() {
		return basicAuthentication;
	}

	public void setBasicAuthentication(BasicAuthentication basicAuthentication) {
		this.basicAuthentication = basicAuthentication;
	}

	public boolean isFollowRedirect() {
		return followRedirect;
	}

	public void setFollowRedirect(boolean followRedirect) {
		this.followRedirect = followRedirect;
	}

	public HttpProxy getHttpProxy() {
		return httpProxy;
	}

	public void setHttpProxy(HttpProxy httpProxy) {
		this.httpProxy = httpProxy;
	}

	public int getRequestTimeoutMS() {
		return requestTimeoutMS;
	}

	public void setRequestTimeoutMS(int requestTimeoutMS) {
		this.requestTimeoutMS = requestTimeoutMS;
	}
	
	public int getConnectionTimeoutMS() {
		return connectionTimeoutMS;
	}
	
	public void setConnectionTimeoutMS(int connectionTimeoutMS) {
		this.connectionTimeoutMS = connectionTimeoutMS;
	}

	public void setTransportSecurity(TransportSecurity transportSecurity) {
		this.transportSecurity = transportSecurity;
	}

	public TransportSecurity getTransportSecurity() {
		return transportSecurity;
	}
	
	public HttpHeaders getRequestHeaders() {
		return requestHeaders;
	}
	
	public void setRequestHeaders(HttpHeaders requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public MimeEnvelope getMimeEnvelope() {
		return mimeEnvelope;
	}

	public void setMimeEnvelope(MimeEnvelope mimeEnvelope) {
		this.mimeEnvelope = mimeEnvelope;
	}

	public ContentProvider getContent() {
		return content;
	}

	public void setContent(ContentProvider content) {
		this.content = content;
	}

}
