package com.ceva.eai.bwce.generic.cfw.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;

import com.ceva.eai.bwce.generic.cfw.model.Document;
import com.tibco.bw.palette.shared.java.BWActivityContext;
import com.tibco.bw.runtime.ActivityContext;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;

public class HttpJettyClient {
	private ActivityContext<?> context;
	protected Object[] document = null;
	protected Object request = null;
	protected Object response = null;
	protected Object fault = null;
	private HttpClient httpClient;
		
	@BWActivityContext
	public void assignContext(final ActivityContext<?> context) {
		this.context = context;
	}

	private HttpClientRequest castRequest() {
		return (HttpClientRequest) request;
	}

	private SSLContext getDefaultSSLContext() {
		try {
			SSLContext sc = SSLContext.getDefault();
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			return sc;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private SslContextFactory createSSLContextFactory(HttpClientRequest request) {
		SslContextFactory sslFactory = new SslContextFactory.Client();
		if (request.getTransportSecurity() != null) {
			try {
				TransportSecurity ts = request.getTransportSecurity();
				sslFactory.setTrustAll(ts.isTrustAll());
				sslFactory.setTrustStorePath(ts.getThrustStoreURL());
				sslFactory.setTrustStorePassword(ts.getThrustStorePassword());
				sslFactory.setTrustStoreType(ts.getThrustStoreType());
				sslFactory.setKeyStorePath(ts.getKeyStoreURL());
				sslFactory.setKeyStorePassword(ts.getKeyStorePassword());
				sslFactory.setKeyStoreType(ts.getKeyStoreType());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			SSLContext sc = getDefaultSSLContext();
			sslFactory.setSslContext(sc);
		}
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		sslFactory.setHostnameVerifier(allHostsValid);
		sslFactory.setTrustAll(true);
		return sslFactory;

	}

	private void createHttpClient() throws Exception {
		HttpClientRequest request = castRequest();

		int maxTotalConnections = 200;
		int maxConnectionsPerHost = 20;

		QueuedThreadPool clientThreads = new QueuedThreadPool(maxTotalConnections, maxConnectionsPerHost);
		clientThreads.setName("HttpRequestClient");
		clientThreads.setLowThreadsThreshold(maxConnectionsPerHost);
		clientThreads.setMaxThreads(maxTotalConnections);

		SslContextFactory sslcf = createSSLContextFactory(request);
		httpClient = new HttpClient(sslcf);
		// httpClient.setMaxConnectionsPerDestination(0);
		// httpClient.setIdleTimeout(0);
		// httpClient.setConnectTimeout(0);
		httpClient.setFollowRedirects(request.isFollowRedirect());
		// httpClient.setTCPNoDelay(false);
		// httpClient.setMaxRedirects(0);
		// httpClient.setRequestBufferSize(0);
		// httpClient.setResponseBufferSize(0);

		AuthenticationStore authStore = httpClient.getAuthenticationStore();
		URI uri = new URI(request.getBaseURL());
		if (request.getBasicAuthentication() != null) {
			BasicAuthentication basicAuthentication = new BasicAuthentication(uri,
					request.getBasicAuthentication().getAuthenticationRealm(),
					request.getBasicAuthentication().getUserName(), request.getBasicAuthentication().getPassword());
			if (request.getBasicAuthentication().isNonPreemptive()) {
				authStore.addAuthentication(basicAuthentication);
			} else {
				authStore.addAuthenticationResult(
						new BasicAuthentication.BasicResult(uri, request.getBasicAuthentication().getUserName(),
								request.getBasicAuthentication().getPassword()));
			}
		}
		if (request.getHttpProxy() != null) {
			ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
			URI proxyURI = new URI(request.getHttpProxy().getProxyURL());
			HttpProxy proxy = new HttpProxy(proxyURI.getHost(), proxyURI.getPort());
			if (proxyConfig == null) {
				proxyConfig = new ProxyConfiguration();
			}
			proxyConfig.getProxies().add(proxy);
			if (request.getHttpProxy().getUserName() != null) {
				authStore.addAuthentication(new BasicAuthentication(proxyURI, request.getHttpProxy().getProxyURL(),
						request.getHttpProxy().getUserName(), request.getHttpProxy().getPassword()));
				if (request.getBasicAuthentication() != null) {
					authStore.addAuthentication(new DigestAuthentication(uri, uri.toString(),
							request.getBasicAuthentication().getUserName(),
							request.getBasicAuthentication().getPassword()));
				}
			}
		}
		httpClient.setExecutor(clientThreads);
		httpClient.start();
	}
	
	private Document findDocument(String documentId) {
		Document doc = null;
		for (Object docObj: document) {
			if (docObj instanceof Document) {
				Document d = (Document)docObj;
				if (documentId.equals(d.getDocumentID())) {
					doc = d;
					break;
				}
			}
		}
		return doc;
	}
	
	private InputStream getContentInputStream(ContentProvider cp) throws Exception {
        InputStream is=null;
        if (cp.getContentBinary()!=null && cp.getContentBinary().length>0) {
        	is = new ByteArrayInputStream(cp.getContentBinary());
        } else if (cp.getContentText()!=null && cp.getContentText().length()>0) {
        	is = new ByteArrayInputStream(cp.getContentText().getBytes("UTF-8"));
        } else if (cp.getContentFile()!=null && cp.getContentFile().length()>0) {
        	is = new FileInputStream(cp.getContentFile());
        } else if (cp.getContentDocumentId()!=null && cp.getContentDocumentId().length()>0) {
        	Document doc = findDocument(cp.getContentDocumentId());
        	if (doc!=null) {
        		is = doc.retrieveContentStream();
        	}                	
        }
        return is;		
	}
	
	private String getContentFileName(ContentProvider cp) throws Exception {
        String result = "attachment";
        if (cp.getContentName()!=null && cp.getContentName().length()>0) {
        	result = cp.getContentName();
        } else if (cp.getContentBinary()!=null && cp.getContentBinary().length>0) {
        	result = "bin_attachment";
        } else if (cp.getContentText()!=null && cp.getContentText().length()>0) {
        	result = "text_attachment";
        } else if (cp.getContentFile()!=null && cp.getContentFile().length()>0) {
        	File file = new File(cp.getContentFile());
        	result = file.getName();
        } else if (cp.getContentDocumentId()!=null && cp.getContentDocumentId().length()>0) {
        	Document doc = findDocument(cp.getContentDocumentId());
        	if (doc!=null) {
        		result = doc.getDocumentName();
        	}  else {
        		result = "document";
        	}
        }
        return result;		
	}	

	public Request createHttpRequest() throws Exception {
		HttpClientRequest request = castRequest();
		String uriString = request.getBaseURL();
		if (request.getQueryString() != null && request.getQueryString().length()>0) {
			uriString = uriString + "?" + request.getQueryString();
		}
		URI uri = new URI(uriString);
		Request req = httpClient.newRequest(uri);
		req = req.method(request.getMethod().toUpperCase());
		// populate query params
		if (request.getQueryParamField() != null) {
			List<QueryParamField> qparams = request.getQueryParamField();
			for (QueryParamField qpf : qparams) {
				if (qpf.getFieldBinary() != null) {
					req.param(qpf.getFieldName(), new String(qpf.getFieldBinary()));
				} else {
					req.param(qpf.getFieldName(), qpf.getFieldText());
				}
			}
		}
		if (request.getBasicAuthentication() != null) {
			byte[] credentials = new String(request.getBasicAuthentication().getUserName() + ":"
					+ request.getBasicAuthentication().getPassword()).getBytes(Charset.defaultCharset());
			String authorization = "Basic " + Base64.getEncoder().encodeToString(credentials);
			req.header(HttpHeader.AUTHORIZATION, authorization);
		}
		if (request.getHttpProxy() != null && !request.getHttpProxy().isNonPreemptive()
				&& request.getHttpProxy().getUserName() != null) {
			byte[] credentials = new String(
					request.getHttpProxy().getUserName() + ":" + request.getHttpProxy().getPassword())
							.getBytes(Charset.defaultCharset());
			String proxyAuthorization = Base64.getEncoder().encodeToString(credentials);
			req.header(HttpHeader.PROXY_AUTHORIZATION, proxyAuthorization);
		}
        if (request.getContentMultiPartFormField()!=null ||  request.getMimeEnvelope()!=null) {
        	MultiPartContentProvider multiPart = new MultiPartContentProvider();
            List<MultiPartFormField> mpffs = request.getContentMultiPartFormField();
            for (MultiPartFormField mpff: mpffs) {
            	HttpFields fields = new HttpFields();        	
                List<TransportHeader> headers  = mpff.getHeader();
                for (TransportHeader hdr: headers) {
                	fields.add(hdr.getName(), hdr.getValue());
                }
				InputStream is = getContentInputStream(mpff.getFieldContent());
				if (is != null) {
					if ("file".equalsIgnoreCase(mpff.getFieldType())) {
						String fileName = getContentFileName(mpff.getFieldContent());
						multiPart.addFilePart(mpff.getFieldName(), fileName, new InputStreamContentProvider(is), fields);
					} else {
						multiPart.addFieldPart(mpff.getFieldName(), new InputStreamContentProvider(is), fields);
					}
				}                     	
            }
			if (request.getMimeEnvelope() != null && request.getMimeEnvelope().getMimePart() != null) {
				List<MimePart> mparts = request.getMimeEnvelope().getMimePart();
				for (MimePart mp : mparts) {
					HttpFields fields = new HttpFields();
					if (mp.getMimeHeaders() != null) {
						List<TransportHeader> headers = mp.getMimeHeaders().getMimeHeader();
						for (TransportHeader hdr : headers) {
							fields.add(hdr.getName(), hdr.getValue());
						}
					}
					InputStream is = getContentInputStream(mp.getContent());
					if (is != null) {
						// multiPart.addFieldPart(mp., new InputStreamContentProvider(is), fields);
					}

				}
			}
            req.content(multiPart);
        }		

		return req;

	}

	public void invoke() {

	}

	public Object[] getDocument() {
		return document;
	}

	public void setDocument(Object[] val) {
		document = val;
	}

	public Object getRequest() {
		return request;
	}

	public void setRequest(Object val) {
		request = val;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object val) {
		response = val;
	}

	public Object getFault() {
		return fault;
	}

	public void setFault(Object val) {
		fault = val;
	}
}
