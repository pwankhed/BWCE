package com.ceva.eai.bwce.generic.cfw.model;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ceva.eai.bwce.generic.cfw.utils.encoding.FileEncoding;
import com.ceva.eai.bwce.generic.cfw.utils.encoding.SmartEncodingInputStream;

public class Document implements Serializable {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private static final long serialVersionUID = 5141140172123851212L;
	protected static final String CATEGORY = "Doc";
	protected static final String DOCUMENT_ID = "DocumentID";
	protected static final String DOCUMENT_SIZE_BYTES = "DocumentSizeBytes";
	protected static final String DOCUMENT_SOURCE_ID = "DocumentSourceID";
	protected static final String DOCUMENT_IS_BASE64 = "DocumentIsBase64";
	protected static final String DOCUMENT_IS_COMPRESSED = "DocumentIsCompressed";
	protected static final String DOCUMENT_COMPRESSION_TYPE = "DocumentCompressionType";
	protected static final String DOCUMENT_IS_BINARY = "DocumentIsBinary";
	protected static final String DOCUMENT_IS_REFERENCE = "DocumentIsReference";
	protected static final String DOCUMENT_REFERENCE_IS_URL = "DocumentReferenceIsURL";
	protected static final String DOCUMENT_CATEGORY = "DocumentCategory";
	protected static final String DOCUMENT_NAME = "DocumentName";
	protected static final String DOCUMENT_TYPE = "DocumentType";
	protected static final String DOCUMENT_ENCODING = "DocumentEncoding";
	protected static final String DOCUMENT_MIME_TYPE = "DocumentMimeType";
	protected static final String DOCUMENT_CORRELATION_ID = "DocumentCorrelationID";
	protected static final String DOCUMENT_PRIORITY = "DocumentPriority";
	protected static final String COMPRESSION_TYPE_NONE = "NONE";
	protected static final String COMPRESSION_TYPE_ZIP = "ZIP";
	protected static final String COMPRESSION_TYPE_GZIP = "GZIP";
	protected static final String BINARY_CHECK_METHOD_LOOSE = "LOOSE";
	protected static final String BINARY_CHECK_METHOD_STRICT = "STRICT";
	protected static final String BINARY_CHECK_METHOD_MIME = "MIME";
	protected static final String BINARY_CHECK_METHOD_SKIP = "SKIP";
	protected static final String BASE64_CHECK_METHOD_BASIC = "BASIC";
	protected static final String BASE64_CHECK_METHOD_DECODE = "DECODE";
	protected static final String BASE64_CHECK_METHOD_DECODECRC = "DECODECRC";
	protected static final String BASE64_CHECK_METHOD_SKIP = "SKIP";

	protected static final String REGISTERED = "IsRegistered";
	protected static final String OVERRIDE_DISKBASE_PATH = "DiskBasePath";
	protected static final String OVERRIDE_DISKOVERFLOW_PATH = "DiskoverflowPath";
	protected static final String OVERRIDE_COMPRESSION_LIMIT = "CompressionLimitBytes";
	protected static final String OVERRIDE_DISKOVERFLOW_LIMIT = "DiskOverflowLimitBytes";
	protected static final String OVERRIDE_BASE64_CHECK_METHOD = "Base64CheckMethod";
	protected static final String OVERRIDE_BINARY_CHECK_METHOD = "BinaryCheckMethod";
	protected static final String OVERRIDE_BINARY_CHECK_LOOSE_MAXBYTES = "BinaryCheckLooseMaxBytes";
	protected static final String OVERRIDE_BINARY_CHECK_LOOSE_MAXNONTEXT = "BinaryCheckLooseMaxNonText";
	protected static final String OVERRIDE_INHERITED_CATEGORY_SUPPRESS_MASK = "InheritedCategorySuppressMask";
	protected static final String SPECIAL_FILENAME_CHARS = "/\\?%*:|\"<> '#&{}$!@";
	protected static final String SPECIAL_FILENAME_REPLACE_CHARS = "_________________________";
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private byte[] documentBytes = new byte[0];
	private String documentReference = "";
	private String documentID = "";
	private int documentPriority = 4;
	private boolean registered = false;
	private String documentSourceID = "";
	private String documentCorrelationID = "";
	private String documentCategory = "";
	private String documentName = "";
	private boolean documentNameGenerated = false;
	private String documentType = "";
	private String documentEncoding = "";
	private boolean encodingNonDefault;
	private long documentSizeBytes = -1;
	private String documentMimeType = "";
	private String compressionType = "";
	private boolean base64;
	private boolean compressed = false;
	private boolean reference = false;
	private boolean referenceIsUrl = false;
	private boolean base64In = false;
	private boolean binary = false;
	private long compressionLimitBytes = 1000000;
	private long diskoverflowLimitBytes = 5000000;
	private int maxReferenceNameLength = 4096;
	private String diskBaselocation = "/local/";
	private String overflowlocation = "/local/tibco/data/";
	private String binaryCheckMethod = BINARY_CHECK_METHOD_LOOSE;
	private String base64CheckMethod = BASE64_CHECK_METHOD_BASIC;
	private long binaryCheckLooseMaxBytes = 100000;
	private long binaryCheckLooseMaxNonText = 20;
	private String inheritedCategorySuppressMask = "Replay|Delay|DocFault|HandleError";
	private DocumentProperties properties = new DocumentProperties();

	/* GETTERS AND SETTERS START */
	public String getDocumentID() {
		return documentID;
	}

	public String getDocumentSourceID() {
		return documentSourceID;
	}

	public String getDocumentName() {
		return documentName;
	}

	public String getDocumentType() {
		return documentType;
	}

	public String getDocumentEncoding() {
		return documentEncoding;
	}

	public String getDocumentCorrelationID() {
		return documentCorrelationID;
	}

	public String getDocumentCategory() {
		return documentCategory;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public String getDocumentMimeType() {
		return documentMimeType;
	}

	public int getDocumentPriority() {
		return documentPriority;
	}

	private boolean isEncodingNonDefault() {
		return encodingNonDefault;
	}

	public boolean isBase64() {
		return base64;
	}

	public boolean isBinary() {
		return binary;
	}

	public boolean isReference() {
		return reference;
	}

	public boolean isReferenceIsUrl() {
		return referenceIsUrl;
	}

	public String getCompressionType() {
		return compressionType;
	}

	public boolean isRegistered() {
		return registered;
	}

	public boolean isDocumentNameGenerated() {
		return documentNameGenerated;
	}

	public long getDocumentSizeBytes() {
		return documentSizeBytes;
	}
	
	public DocumentProperties getProperties() {
		return properties;
	}
	
	/* ENV SETTINGS */
	protected void withCompressionLimitBytes(long compressionLimitBytes) {
		this.compressionLimitBytes = compressionLimitBytes;
	}

	protected void withDiskoverflowLimitBytes(long diskoverflowLimitBytes) {
		this.diskoverflowLimitBytes = diskoverflowLimitBytes;
	}

	protected void withMaxReferenceNameLength(int maxReferenceNameLength) {
		this.maxReferenceNameLength = maxReferenceNameLength;
	}

	protected void withDiskBaselocation(String diskBaselocation) {
		this.diskBaselocation = diskBaselocation;
	}

	protected void withOverflowlocation(String overflowlocation) {
		this.overflowlocation = overflowlocation;
	}

	protected void withBinaryCheckMethod(String binaryCheckMethod) {
		this.binaryCheckMethod = binaryCheckMethod;
	}

	protected void withBase64CheckMethod(String base64CheckMethod) {
		this.base64CheckMethod = base64CheckMethod;
	}

	protected void withBinaryCheckLooseMaxBytes(long binaryCheckLooseMaxBytes) {
		this.binaryCheckLooseMaxBytes = binaryCheckLooseMaxBytes;
	}

	protected void withBinaryCheckLooseMaxNonText(long binaryCheckLooseMaxNonText) {
		this.binaryCheckLooseMaxNonText = binaryCheckLooseMaxNonText;
	}

	protected void withInheritedCategorySuppressMask(String inheritedCategorySuppressMask) {
		this.inheritedCategorySuppressMask = inheritedCategorySuppressMask;
	}

	/* END ENV SETTINGS */

	private void setDocumentNameGenerated(boolean isGenerated) {
		this.documentNameGenerated = isGenerated;
	}

	private void setDocumentID(String documentID) {
		this.documentID = documentID;
		properties.addProperty(DOCUMENT_ID, DocumentProperty.STRING_TYPE, documentID);
	}

	private void setDocumentSourceID(String documentSourceID) {
		this.documentSourceID = documentSourceID;
		properties.addProperty(DOCUMENT_SOURCE_ID, DocumentProperty.STRING_TYPE, documentSourceID);
	}

	private void setDocumentCorrelationID(String documentCorrelationID) {
		this.documentCorrelationID = documentCorrelationID;
		properties.addProperty(DOCUMENT_CORRELATION_ID, DocumentProperty.STRING_TYPE, documentCorrelationID);
	}

	private void setDocumentCategory(String documentCategory) {
		this.documentCategory = documentCategory;
		properties.addProperty(DOCUMENT_CATEGORY, DocumentProperty.STRING_TYPE, documentCategory);
	}

	private void setDocumentName(String documentName) {
		this.documentName = documentName;
		properties.addProperty(DOCUMENT_NAME, DocumentProperty.STRING_TYPE, documentName);
	}

	private void setDocumentType(String documentType) {
		this.documentType = documentType;
		properties.addProperty(DOCUMENT_TYPE, DocumentProperty.STRING_TYPE, documentType);
	}

	private void setDocumentEncoding(String documentEncoding) {
		this.documentEncoding = documentEncoding;
		properties.addProperty(DOCUMENT_ENCODING, DocumentProperty.STRING_TYPE, documentEncoding);
	}

	private void setDocumentSizeBytes(long documentSizeBytes) {
		this.documentSizeBytes = documentSizeBytes;
		properties.addProperty(DOCUMENT_SIZE_BYTES, DocumentProperty.LONG_TYPE, Long.toString(documentSizeBytes));

	}

	private void setDocumentMimeType(String documentMimeType) {
		this.documentMimeType = documentMimeType;
		properties.addProperty(DOCUMENT_MIME_TYPE, DocumentProperty.STRING_TYPE, documentMimeType);
	}

	private void setDocumentPriority(int documentPriority) {
		this.documentPriority = documentPriority;
		properties.addProperty(DOCUMENT_PRIORITY, DocumentProperty.INT_TYPE, Integer.toString(documentPriority));
	}

	private void setCompressionType(String compressionType) {
		this.compressionType = compressionType;
		properties.addProperty(DOCUMENT_COMPRESSION_TYPE, DocumentProperty.STRING_TYPE, compressionType);
	}

	private void setCompressed(boolean compressed) {
		this.compressed = compressed;
		properties.addProperty(DOCUMENT_IS_COMPRESSED, DocumentProperty.BOOLEAN_TYPE, Boolean.toString(compressed));

	}

	private void setReference(boolean reference) {
		this.reference = reference;
		properties.addProperty(DOCUMENT_IS_REFERENCE, DocumentProperty.BOOLEAN_TYPE, Boolean.toString(reference));
	}

	private void setReferenceIsUrl(boolean reference_is_url) {
		this.referenceIsUrl = reference_is_url;
		properties.addProperty(DOCUMENT_REFERENCE_IS_URL, DocumentProperty.BOOLEAN_TYPE, Boolean.toString(reference_is_url));

	}

	private void setBase64In(boolean base64) {
		this.base64In = base64;
		properties.addProperty(DOCUMENT_IS_BASE64, DocumentProperty.BOOLEAN_TYPE, Boolean.toString(base64));

	}

	private void setRegistered(boolean registered) {
		this.registered = registered;
		properties.addProperty(REGISTERED, DocumentProperty.BOOLEAN_TYPE, Boolean.toString(registered));

	}

	private void setBinary(boolean binary) {
		this.binary = binary;
		properties.addProperty(DOCUMENT_IS_BINARY, DocumentProperty.BOOLEAN_TYPE, Boolean.toString(binary));
	}

	/* UTILITY METHODS */

	/* PROPERTIES */
	private void updateDocMetaProperties() {
		if (retrieveProperties().containsProperty(DOCUMENT_NAME)) {
			setDocumentName(properties.retrieveString(DOCUMENT_NAME, getDocumentName()));
			setDocumentNameGenerated(false);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_CATEGORY)) {
			setDocumentCategory(properties.retrieveString(DOCUMENT_CATEGORY, getDocumentCategory()));
		}
		if (retrieveProperties().containsProperty(DOCUMENT_TYPE)) {
			setDocumentType(properties.retrieveString(DOCUMENT_TYPE, getDocumentType()));
		}
		if (retrieveProperties().containsProperty(DOCUMENT_ENCODING)) {
			setDocumentEncoding(properties.retrieveString(DOCUMENT_ENCODING, getDocumentEncoding()));
		}
		if (retrieveProperties().containsProperty(DOCUMENT_MIME_TYPE)) {
			setDocumentMimeType(properties.retrieveString(DOCUMENT_MIME_TYPE, getDocumentMimeType()));
		}
		if (retrieveProperties().containsProperty(REGISTERED)) {
			setRegistered(properties.retrieveBool(REGISTERED, isRegistered()));
		}
		if (retrieveProperties().containsProperty(DOCUMENT_PRIORITY)) {
			setDocumentPriority(properties.retrieveInt(DOCUMENT_PRIORITY, getDocumentPriority()));
		}
	}

	private void updateDocIDProperties() {
		if (retrieveProperties().containsProperty(DOCUMENT_ID)) {
			setDocumentID(properties.retrieveString(DOCUMENT_ID, getDocumentID()));
		}
		if (retrieveProperties().containsProperty(DOCUMENT_SOURCE_ID)) {
			setDocumentSourceID(properties.retrieveString(DOCUMENT_SOURCE_ID, getDocumentSourceID()));
		}
		String corID = !DocumentPropertyUtils.isEmpty(getDocumentSourceID()) ? this.getDocumentSourceID() : getDocumentID();
		if (retrieveProperties().containsProperty(DOCUMENT_CORRELATION_ID)) {
			setDocumentCorrelationID(properties.retrieveString(DOCUMENT_CORRELATION_ID, corID));
		} else {
			setDocumentCorrelationID(corID);
		}
	}

	private void removeDocMetaProperties(DocumentPropertySet<DocumentProperty> properties) {
		properties.removeProperty(DOCUMENT_ID);
		properties.removeProperty(DOCUMENT_SOURCE_ID);
		properties.removeProperty(DOCUMENT_NAME);
		properties.removeProperty(DOCUMENT_TYPE);
		properties.removeProperty(DOCUMENT_ENCODING);
		properties.removeProperty(DOCUMENT_MIME_TYPE);
		properties.removeProperty(REGISTERED);
	}

	private void removeDocContentProperties(DocumentPropertySet<DocumentProperty> properties) {
		properties.removeProperty(DOCUMENT_IS_BASE64);
		properties.removeProperty(DOCUMENT_IS_COMPRESSED);
		properties.removeProperty(DOCUMENT_COMPRESSION_TYPE);
		properties.removeProperty(DOCUMENT_IS_BINARY);
		properties.removeProperty(DOCUMENT_IS_REFERENCE);
		properties.removeProperty(DOCUMENT_REFERENCE_IS_URL);
		properties.removeProperty(DOCUMENT_SIZE_BYTES);
	}

	private void removeDocContentProperties(Set<DocumentProperty> properties) {
		properties.remove(DocumentProperty.create().setName(DOCUMENT_IS_BASE64));
		properties.remove(DocumentProperty.create().setName(DOCUMENT_IS_COMPRESSED));
		properties.remove(DocumentProperty.create().setName(DOCUMENT_COMPRESSION_TYPE));
		properties.remove(DocumentProperty.create().setName(DOCUMENT_IS_BINARY));
		properties.remove(DocumentProperty.create().setName(DOCUMENT_IS_REFERENCE));
		properties.remove(DocumentProperty.create().setName(DOCUMENT_REFERENCE_IS_URL));
		properties.remove(DocumentProperty.create().setName(DOCUMENT_SIZE_BYTES));
	}
	
	protected final void addPropertiesSet(final Set<DocumentProperty> dprops, boolean filterContentProps) {
		if (dprops != null && !dprops.isEmpty()) {
			if (filterContentProps) {
				removeDocContentProperties(dprops);
			}
			retrieveProperties().addAll(dprops);
			updateDocIDProperties();
			updateDocMetaProperties();
		}
	}

	protected final void addPropertiesString(final String addprops, boolean filterContentProps) {
		if (addprops != null && addprops.length() > 0) {
			DocumentPropertySet<DocumentProperty> dprops = DocumentPropertyUtils.parsePropertyString(addprops);
			this.addPropertiesSet(dprops, filterContentProps);
		}
	}

	protected final void addPropertiesStringArray(final String[] addprops, boolean filterContentProps) {
		if (addprops != null && addprops.length > 0) {
			DocumentPropertySet<DocumentProperty> dprops = DocumentPropertyUtils.parsePropertyArray(addprops);
			this.addPropertiesSet(dprops, filterContentProps);
		}
	}


	protected final DocumentPropertySet<DocumentProperty> retrieveProperties() {
		return properties.asDocumentPropertySet();
	}

   /* CONTENT METHODS */
	private boolean doBase64DecodeCheck(byte[] data, boolean crcCheck) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return doBase64DecodeCheck(bais, crcCheck);
	}

	private boolean doBase64DecodeCheck(InputStream instream, boolean crcCheck) throws IOException {
		boolean b64;
		try {
			InputStream fis = new Base64InputStream(instream, crcCheck);

			try (OutputStream os = NullOutputStream.NULL_OUTPUT_STREAM) {
				IOUtils.copyLarge(fis, os);
			}
			b64 = true;
		} catch (CharConversionException cce) {
			b64 = false;
		}
		return b64;
	}

	private void checkStreamDocumentBase64(InputStream instream, boolean close) throws Exception {
		BufferedInputStream bis = new BufferedInputStream(instream);
		try {
			if (retrieveProperties().containsProperty(DOCUMENT_IS_BASE64)) {
				setBase64In(properties.retrieveBool(DOCUMENT_IS_BASE64, false));
			} else {
				boolean b64;
				switch (base64CheckMethod.toUpperCase()) {
				case BASE64_CHECK_METHOD_SKIP:
					b64 = false;
					break;
				case BASE64_CHECK_METHOD_DECODE:
					b64 = doBase64DecodeCheck(instream, false);
					break;
				case BASE64_CHECK_METHOD_DECODECRC:
					b64 = doBase64DecodeCheck(instream, true);
					break;
				case BASE64_CHECK_METHOD_BASIC:
				default:
					b64 = com.ceva.eai.bwce.generic.cfw.utils.encoding.Base64.isBase64(bis);
					break;
				}
				setBase64In(b64);
				logger.debug("Initial Stream is base64:" + b64);
			}
		} finally {
			if (close) {
				IOUtils.closeQuietly(bis);
			}
		}
	}

	private void checkStreamDocumentCompressed(InputStream instream, boolean isBase64, boolean close) throws Exception {
		BufferedInputStream bis;
		if (isBase64) {
			bis = new BufferedInputStream(new Base64InputStream(instream));
		} else {
			bis = new BufferedInputStream(instream);
		}
		try {
			if (!retrieveProperties().containsProperty(DOCUMENT_IS_COMPRESSED)) {
				setCompressionType(checkCompressionType(bis, close));
				setCompressed(!COMPRESSION_TYPE_NONE.equals(getCompressionType()));
				logger.debug(String.format("Stream checked for compression:%s", getCompressionType()));
			} else {
				setCompressionType(properties.retrieveString(DOCUMENT_COMPRESSION_TYPE, COMPRESSION_TYPE_NONE));
				setCompressed(
						properties.retrieveBool(DOCUMENT_IS_COMPRESSED, !COMPRESSION_TYPE_NONE.equals(getCompressionType())));
			}
		} finally {
			if (close) {
				IOUtils.closeQuietly(bis);
			}
		}
	}

	private boolean streamIsBinary(InputStream instream) throws IOException {
		boolean isBinary;
		String checkMethod = binaryCheckMethod;
		if (checkMethod == null || checkMethod.isEmpty()) {
			checkMethod = BINARY_CHECK_METHOD_LOOSE;
		}
		logger.debug("streamIsBinary using method:" + checkMethod);
		switch (checkMethod.toUpperCase()) {
		case BINARY_CHECK_METHOD_LOOSE:
			isBinary = !FileEncoding.SINGLE_INSTANCE.contentIsTextLoose(instream, true, binaryCheckLooseMaxNonText,
					binaryCheckLooseMaxBytes);
			logger.debug(String.format("check loose method:%s, maxNonText:%d, maxBytes:%d, result: isBinary:%s",
					checkMethod, binaryCheckLooseMaxNonText, binaryCheckLooseMaxBytes, isBinary));
			break;
		case BINARY_CHECK_METHOD_MIME:
			String mimeType = FileEncoding.SINGLE_INSTANCE.detectMimeType(instream);
			this.setDocumentMimeType(mimeType);
			isBinary = !FileEncoding.SINGLE_INSTANCE.mimeTypeIsText(mimeType);
			logger.debug(String.format("check mime method:%s, result: mimeType:%s, isBinary:%s", checkMethod, mimeType,
					isBinary));
			break;
		case BINARY_CHECK_METHOD_STRICT:
		default:
			isBinary = !FileEncoding.SINGLE_INSTANCE.contentIsText(instream, true);
			logger.debug(String.format("check strict method:%s, result: isBinary:%s", checkMethod, isBinary));
			break;
		}
		return isBinary;
	}

	private void checkStreamDocumentBinary(InputStream instream, boolean isBase64, boolean close) throws Exception {
		try {
			boolean skipBinaryCheck = BINARY_CHECK_METHOD_SKIP.equalsIgnoreCase(binaryCheckMethod);
			if (!skipBinaryCheck && !retrieveProperties().containsProperty(DOCUMENT_IS_BINARY)) {
				BufferedInputStream bis;
				if (isBase64) {
					bis = new BufferedInputStream(new Base64InputStream(instream));
				} else {
					bis = new BufferedInputStream(instream);
				}
				try {
					if (isCompressed() && COMPRESSION_TYPE_GZIP.equalsIgnoreCase(getCompressionType())) {
						InputStream gzis = new GZIPInputStream(bis);
						try {
							setBinary(streamIsBinary(gzis));
						} finally {
							if (close) {
								IOUtils.closeQuietly(gzis);
							}
						}
					} else if (isCompressed() && COMPRESSION_TYPE_ZIP.equalsIgnoreCase(getCompressionType())) {
						ZipInputStream zip = new ZipInputStream(bis);
						try {
							boolean found = false;
							ZipEntry entry;
							while ((entry = zip.getNextEntry()) != null) {
								if (!entry.isDirectory()) {
									found = true;
									setBinary(streamIsBinary(zip));
									if (entry.getSize() > 0) {
										setDocumentSizeBytes(entry.getSize());
									}
									break;
								}
							}
							if (!found) {
								setBinary(false);
							}
						} finally {
							if (close) {
								IOUtils.closeQuietly(zip);
							}
						}
					} else {
						setBinary(streamIsBinary(bis));
					}
				} finally {
					if (close) {
						IOUtils.closeQuietly(bis);
					}
				}
			} else {
				setBinary(properties.retrieveBool(DOCUMENT_IS_BINARY, false));
			}
			if (DocumentPropertyUtils.isEmpty(getDocumentMimeType())) {
				if (isBinary()) {
					setDocumentMimeType("application/octet-stream");
				} else {
					setDocumentMimeType("application/xml");
				}
			}
		} finally {
			if (close) {
				IOUtils.closeQuietly(instream);
			}
		}
	}

	@SuppressWarnings("resource")
	private void checkStreamDocumentEncoding(InputStream instream, String defaultEncoding, boolean isBase64,
			boolean close) throws Exception {
		Charset defaultEnc = (DocumentPropertyUtils.isEmpty(defaultEncoding)) ? Charset.defaultCharset()
				: Charset.forName(defaultEncoding);
		try {
			BufferedInputStream bis;
			if (isBase64) {
				bis = new BufferedInputStream(new Base64InputStream(instream));
			} else {
				bis = new BufferedInputStream(instream);
			}
			if (retrieveProperties().containsProperty(DOCUMENT_ENCODING)) {
				setDocumentEncoding(properties.retrieveString(DOCUMENT_ENCODING, defaultEnc.name()));
			} else {
				try {
					if (isCompressed() && COMPRESSION_TYPE_GZIP.equals(getCompressionType())) {
						InputStream gzis = new GZIPInputStream(bis);
						try {
							SmartEncodingInputStream smartIS = new SmartEncodingInputStream(gzis,
									(int) binaryCheckLooseMaxBytes, defaultEnc);
							setDocumentEncoding(smartIS.getEncoding().name());
						} finally {
							if (close) {
								IOUtils.closeQuietly(gzis);
							}
						}
					} else if (isCompressed() && COMPRESSION_TYPE_ZIP.equalsIgnoreCase(getCompressionType())) {
						ZipInputStream zip = new ZipInputStream(bis);
						try {
							boolean found = false;
							ZipEntry entry;
							while ((entry = zip.getNextEntry()) != null) {
								if (!entry.isDirectory()) {
									found = true;
									SmartEncodingInputStream smartIS = new SmartEncodingInputStream(zip,
											(int) binaryCheckLooseMaxBytes, defaultEnc);
									setDocumentEncoding(smartIS.getEncoding().name());
									break;
								}
							}
							if (!found) {
								setDocumentEncoding(defaultEncoding);
							}
						} finally {
							if (close) {
								IOUtils.closeQuietly(zip);
							}
						}
					} else {
						SmartEncodingInputStream smartIS = new SmartEncodingInputStream(bis,
								(int) binaryCheckLooseMaxBytes, defaultEnc);
						setDocumentEncoding(smartIS.getEncoding().name());
					}
				} finally {
					if (close) {
						IOUtils.closeQuietly(bis);
					}
				}
				logger.debug(String.format("Stream encoding check:%s", getDocumentEncoding()));
			}
		} finally {
			if (close) {
				IOUtils.closeQuietly(instream);
			}
		}
	}

	private void closeURLConnection(URLConnection connection) {
		try {
			Method m = connection.getClass().getMethod("disconnect");
			m.invoke(connection);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | SecurityException e) {
			// doesn't matter
		}
	}

	private long retrieveURLSize(final URL uri) {
		String contentLengthStr;
		URLConnection ucon;
		try {
			ucon = uri.openConnection();
			ucon.connect();
			try {
				contentLengthStr = ucon.getHeaderField("content-length");
				if (contentLengthStr == null || contentLengthStr.length() == 0) {
					contentLengthStr = Integer.toString(ucon.getContentLength());
				}
				if (!DocumentPropertyUtils.isEmpty(ucon.getContentType())) {
					setDocumentMimeType(ucon.getContentType());
				}
			} finally {
				closeURLConnection(ucon);
			}
		} catch (final IOException e1) {
			logger.debug("Got error getting url size:" + e1.getMessage(), e1);
			contentLengthStr = "-1";
		}
		return Long.parseLong(contentLengthStr);
	}

	private String removeSpecialFilenameChars(String str) {
		return replaceAllChars(str, SPECIAL_FILENAME_CHARS, SPECIAL_FILENAME_REPLACE_CHARS);
	}



	private String replaceAllChars(String str, String searchChars, String replaceChars) {
		if (DocumentPropertyUtils.isEmpty(str) || DocumentPropertyUtils.isEmpty(searchChars)) {
			return str;
		}
		if (replaceChars == null) {
			replaceChars = "";
		}
		boolean modified = false;
		int replaceCharsLength = replaceChars.length();
		int strLength = str.length();
		StringBuilder buf = new StringBuilder(strLength);
		for (int i = 0; i < strLength; i++) {
			char ch = str.charAt(i);
			int index = searchChars.indexOf(ch);
			if (index >= 0) {
				modified = true;
				if (index < replaceCharsLength) {
					buf.append(replaceChars.charAt(index));
				} else if (replaceChars.length() >= 1) {
					buf.append(replaceChars.charAt(0));
				}
			} else {
				buf.append(ch);
			}
		}
		if (modified) {
			return buf.toString();
		}
		return str;
	}

	private byte[] inputStreamToBytes(InputStream is, boolean isBase64) throws Exception {

		byte[] bytes = new byte[0];
		try {
			BufferedInputStream bis;
			if (isBase64) {
				bis = new BufferedInputStream(new Base64InputStream(is));
			} else {
				bis = new BufferedInputStream(is);
			}
			try {
				bytes = IOUtils.toByteArray(bis);
			} finally {
				IOUtils.closeQuietly(bis);
			}
		} finally {
			IOUtils.closeQuietly(is);
		}
		return bytes;
	}

	private void byteArrayToFile(byte[] dbytes, String filename, boolean isBase64) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(dbytes);
		try {
			BufferedInputStream bis;
			if (isBase64) {
				bis = new BufferedInputStream(new Base64InputStream(bais));
			} else {
				bis = new BufferedInputStream(bais);
			}

			try {
				FileOutputStream fos = new FileOutputStream(filename);
				try {
					IOUtils.copyLarge(bis, fos);
				} finally {
					IOUtils.closeQuietly(fos);
				}
			} finally {
				IOUtils.closeQuietly(bis);
			}
		} finally {
			IOUtils.closeQuietly(bais);
		}
	}

	protected final void initialize() throws Exception {
		initializeOverride();
		if (retrieveProperties().containsProperty(REGISTERED)) {
			setRegistered(properties.retrieveBool(REGISTERED, false));
		} else {
			setRegistered(false);
		}
		String guid = UUID.randomUUID().toString();
		if (retrieveProperties().containsProperty(DOCUMENT_ID)) {
			setDocumentID(properties.retrieveString(DOCUMENT_ID, guid));
		} else {
			setDocumentID(guid);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_SOURCE_ID)) {
			if (DocumentPropertyUtils.isEmpty(getDocumentSourceID())) {
				setDocumentSourceID(properties.retrieveString(DOCUMENT_SOURCE_ID, ""));
			} else {
				setDocumentSourceID(getDocumentSourceID());
			}
		} else {
			if (!DocumentPropertyUtils.isEmpty(getDocumentSourceID())) {
				setDocumentSourceID(getDocumentSourceID());
			}
		}
		if (retrieveProperties().containsProperty(DOCUMENT_IS_BASE64)) {
			setBase64In(properties.retrieveBool(DOCUMENT_IS_BASE64, false));
		}
		String corID = (!DocumentPropertyUtils.isEmpty(getDocumentSourceID()) ? getDocumentSourceID() : getDocumentID());
		if (retrieveProperties().containsProperty(DOCUMENT_CORRELATION_ID)) {
			setDocumentCorrelationID(properties.retrieveString(DOCUMENT_CORRELATION_ID, corID));
		} else {
			setDocumentCorrelationID(corID);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_PRIORITY)) {
			setDocumentPriority(properties.retrieveInt(DOCUMENT_PRIORITY, 4));
		} else {
			setDocumentPriority(4);
		}
	}

	private void initializeOverride() {
		if (retrieveProperties().containsProperty(OVERRIDE_DISKBASE_PATH)) {
			this.diskBaselocation = properties.retrieveString(OVERRIDE_DISKBASE_PATH, this.diskBaselocation);
			retrieveProperties().removeProperty(OVERRIDE_DISKBASE_PATH);
		}
		if (retrieveProperties().containsProperty(OVERRIDE_DISKOVERFLOW_PATH)) {
			this.overflowlocation = properties.retrieveString(OVERRIDE_DISKOVERFLOW_PATH, this.overflowlocation);
			retrieveProperties().removeProperty(OVERRIDE_DISKOVERFLOW_PATH);
		}
		if (retrieveProperties().containsProperty(OVERRIDE_COMPRESSION_LIMIT)) {
			this.compressionLimitBytes = properties.retrieveLong(OVERRIDE_COMPRESSION_LIMIT, this.compressionLimitBytes);
			retrieveProperties().removeProperty(OVERRIDE_COMPRESSION_LIMIT);
		}
		if (retrieveProperties().containsProperty(OVERRIDE_DISKOVERFLOW_LIMIT)) {
			this.diskoverflowLimitBytes = properties.retrieveLong(OVERRIDE_DISKOVERFLOW_LIMIT, this.diskoverflowLimitBytes);
			retrieveProperties().removeProperty(OVERRIDE_COMPRESSION_LIMIT);
		}
		if (retrieveProperties().containsProperty(OVERRIDE_BASE64_CHECK_METHOD)) {
			this.base64CheckMethod = properties.retrieveString(OVERRIDE_BASE64_CHECK_METHOD, this.base64CheckMethod);
			logger.debug("Overriden base64 check method to " + base64CheckMethod);
			retrieveProperties().removeProperty(OVERRIDE_BASE64_CHECK_METHOD);
		}
		if (retrieveProperties().containsProperty(OVERRIDE_BINARY_CHECK_METHOD)) {
			this.binaryCheckMethod = properties.retrieveString(OVERRIDE_BINARY_CHECK_METHOD, this.binaryCheckMethod);
			logger.debug("Overriden binary check method to " + binaryCheckMethod);
			retrieveProperties().removeProperty(OVERRIDE_BINARY_CHECK_METHOD);
		}
		if (retrieveProperties().containsProperty(OVERRIDE_BINARY_CHECK_LOOSE_MAXBYTES)) {
			this.binaryCheckLooseMaxBytes = properties.retrieveLong(OVERRIDE_BINARY_CHECK_LOOSE_MAXBYTES,
					this.binaryCheckLooseMaxBytes);
			retrieveProperties().removeProperty(OVERRIDE_BINARY_CHECK_LOOSE_MAXBYTES);
		}
		if (retrieveProperties().containsProperty(OVERRIDE_BINARY_CHECK_LOOSE_MAXNONTEXT)) {
			this.binaryCheckLooseMaxNonText = properties.retrieveLong(OVERRIDE_BINARY_CHECK_LOOSE_MAXNONTEXT,
					this.binaryCheckLooseMaxNonText);
			retrieveProperties().removeProperty(OVERRIDE_BINARY_CHECK_LOOSE_MAXNONTEXT);
		}
		if (retrieveProperties().containsProperty(OVERRIDE_INHERITED_CATEGORY_SUPPRESS_MASK)) {
			this.inheritedCategorySuppressMask = properties.retrieveString(OVERRIDE_INHERITED_CATEGORY_SUPPRESS_MASK,
					this.inheritedCategorySuppressMask);
			retrieveProperties().removeProperty(OVERRIDE_INHERITED_CATEGORY_SUPPRESS_MASK);
		}
	}

	private void initializeDocMeta() {
		if (retrieveProperties().containsProperty(DOCUMENT_TYPE)) {
			setDocumentType(properties.retrieveString(DOCUMENT_TYPE, "DOC"));
		} else {
			if (!DocumentPropertyUtils.isEmpty(getDocumentType())) {
				setDocumentType(getDocumentType());
			} else {
				setDocumentType("DOC");
			}
		}
		if (retrieveProperties().containsProperty(DOCUMENT_NAME)) {
			String docName = properties.retrieveString(DOCUMENT_NAME, "");
			if (DocumentPropertyUtils.isEmpty(docName)) {
				docName = getDocumentType() + "_" + getDocumentID();
				setDocumentNameGenerated(true);
			} else {
				setDocumentNameGenerated(false);
			}
			setDocumentName(docName);
		} else {
			if (!DocumentPropertyUtils.isEmpty(getDocumentName())) {
				setDocumentName(getDocumentName());
				setDocumentNameGenerated(false);
			} else {
				setDocumentName(getDocumentType() + "_" + getDocumentID());
				setDocumentNameGenerated(true);
			}
		}
		if (retrieveProperties().containsProperty(DOCUMENT_CATEGORY)) {
			setDocumentCategory(properties.retrieveString(DOCUMENT_CATEGORY, ""));
		} else if (!DocumentPropertyUtils.isEmpty(getDocumentCategory())) {
			setDocumentCategory(getDocumentCategory());
		}
	}

	private void initializeFinalize() throws Exception {
		encodingNonDefault = Charset.defaultCharset().compareTo(Charset.forName(documentEncoding)) != 0;
		base64 = (!reference && (binary || compressed || isEncodingNonDefault()) || base64In);
		properties.addProperty(DOCUMENT_IS_BASE64, DocumentProperty.BOOLEAN_TYPE, Boolean.toString(base64));
		if (retrieveProperties().containsProperty(DOCUMENT_MIME_TYPE)) {
			setDocumentMimeType(properties.retrieveString(DOCUMENT_MIME_TYPE, ""));
		} else if (!DocumentPropertyUtils.isEmpty(getDocumentMimeType())) {
			setDocumentMimeType(getDocumentMimeType());
		} else {
			setDocumentMimeType(isBinary() ? "application/octet-stream" : "text/plain");
		}
	}

	protected void initializeClone(final Document source) {
		if (source != null) {
			DocumentPropertySet<DocumentProperty> dprops = new DocumentPropertySet<>();
			dprops.addAll(source.retrieveProperties());
			dprops.removeProperty(DOCUMENT_ID, CATEGORY);
			dprops.removeProperty(REGISTERED, CATEGORY);
			retrieveProperties().addAll(dprops);
			setRegistered(false);
			setDocumentPriority(source.getDocumentPriority());
			setDocumentSourceID(source.getDocumentSourceID());
			setDocumentCorrelationID(source.getDocumentCorrelationID());
			setDocumentCategory(source.getDocumentCategory());
			setDocumentName(source.getDocumentName());
			setDocumentType(source.getDocumentType());
			setDocumentEncoding(source.getDocumentEncoding());
			setDocumentSizeBytes(source.getDocumentSizeBytes());
			this.documentBytes = source.documentBytes;
			this.documentReference = source.documentReference;
			setDocumentMimeType(source.getDocumentMimeType());
			setCompressionType(source.getCompressionType());
			setCompressed(source.isCompressed());
			setReference(source.isReference());
			setReferenceIsUrl(source.isReferenceIsUrl());
			setBase64In(source.base64In);
			setBinary(source.isBinary());
			base64 = source.base64;
			encodingNonDefault = source.encodingNonDefault;
			initializeOverride();
			if (!DocumentPropertyUtils.isEmpty(inheritedCategorySuppressMask)) {
				retrieveProperties().deleteDocumentProperties(inheritedCategorySuppressMask, "");
			}
		}
	}

	protected void initializeFromParent(final Document parent) {
		if (parent != null) {
			DocumentPropertySet<DocumentProperty> dprops = new DocumentPropertySet<>();
			dprops.addAll(parent.retrieveProperties());
			removeDocContentProperties(dprops);
			removeDocMetaProperties(dprops);
			retrieveProperties().addAll(dprops);
			setDocumentSourceID(parent.getDocumentID());
			initializeOverride();
			if (!DocumentPropertyUtils.isEmpty(inheritedCategorySuppressMask)) {
				retrieveProperties().deleteDocumentProperties(inheritedCategorySuppressMask, "");
			}
		}
	}

	protected void initializeContent(final byte[] data) throws Exception {
		if (data != null) {
			checkIsReference(data);
			if (isReference()) {
				if (isReferenceIsUrl()) {
					logger.debug("Data is URL");
					initializeUrlReference(new String(data));
					initializeDocMeta();
				} else {
					logger.debug("Data is local reference");
					initializeLocalReference(new String(data));
					initializeDocMeta();
				}
			} else {
				logger.debug("Data is InMemory");
				initializeDocMeta();
				initializeInMemory(data);
			}
		}
		initializeFinalize();
	}

	protected void initializeContent(final String data) throws Exception {
		if (data != null) {
			checkIsReference(data);
			if (isReference()) {
				if (isReferenceIsUrl()) {
					logger.debug("Data is URL");
					initializeUrlReference(data);
					initializeDocMeta();
				} else {
					logger.debug("Data is local reference");
					initializeLocalReference(data);
					initializeDocMeta();
				}
			} else {
				logger.debug("Data is InMemory");
				if (!base64In) {
					if (retrieveProperties().containsProperty(DOCUMENT_ENCODING)) {
						setDocumentEncoding(properties.retrieveString(DOCUMENT_ENCODING, Charset.defaultCharset().name()));
					} else {
						checkStreamDocumentEncoding(new ByteArrayInputStream(data.getBytes()),
								Charset.defaultCharset().name(), base64In, true);
					}
					initializeDocMeta();
					initializeInMemory(data.getBytes(getDocumentEncoding()));
				} else {
					initializeDocMeta();
					initializeInMemory(data.getBytes());
				}
			}
		}
		initializeFinalize();
	}

	private void checkIsReference(final Object data) {
		boolean identified = false;
		if (retrieveProperties().containsProperty(DOCUMENT_IS_REFERENCE)) {
			setReference(properties.retrieveBool(DOCUMENT_IS_REFERENCE, false));
			if (retrieveProperties().containsProperty(DOCUMENT_REFERENCE_IS_URL)) {
				setReferenceIsUrl(properties.retrieveBool(DOCUMENT_REFERENCE_IS_URL, false));
				identified = true;
			}
		}
		if (!identified) {
			String referenceStr = null;
			if (data instanceof byte[]) {
				byte[] refb = (byte[]) data;
				if (refb.length <= maxReferenceNameLength) {
					referenceStr = new String(refb);
				}
			} else if (data instanceof String) {
				String refs = (String) data;
				if (refs.length() <= maxReferenceNameLength) {
					referenceStr = refs;
				}
			} else {
				String dataClass = (data != null ? data.getClass().getName() : " null");
				throw new RuntimeException("Unknown data object " + dataClass);
			}
			if (referenceStr != null && !DocumentPropertyUtils.isEmpty(referenceStr)) {
				if (referenceStr.startsWith(this.diskBaselocation)) {
					setReference(true);
					setReferenceIsUrl(false);
				} else {
					try {
						new URL(referenceStr);
						setReference(true);
						setReferenceIsUrl(true);
					} catch (MalformedURLException mue) {
					}
				}
			} else {
				setReference(false);
				setReferenceIsUrl(false);
			}
		}
	}

	private boolean initializeLocalReference(final String reference) throws Exception {
		boolean success;

		File refFile = new File(reference);
		if (retrieveProperties().containsProperty(DOCUMENT_IS_BASE64)) {
			setBase64In(properties.retrieveBool(DOCUMENT_IS_BASE64, false));
		} else {
			checkStreamDocumentBase64(new FileInputStream(reference), true);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_COMPRESSION_TYPE)) {
			setCompressionType(properties.retrieveString(DOCUMENT_COMPRESSION_TYPE, COMPRESSION_TYPE_NONE));
		}
		if (retrieveProperties().containsProperty(DOCUMENT_IS_COMPRESSED)) {
			setCompressed(properties.retrieveBool(DOCUMENT_IS_COMPRESSED, false));
		} else {
			checkStreamDocumentCompressed(new FileInputStream(reference), base64In, true);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_ENCODING)) {
			setDocumentEncoding(properties.retrieveString(DOCUMENT_ENCODING, Charset.defaultCharset().name()));
		} else {
			checkStreamDocumentEncoding(new FileInputStream(reference), Charset.defaultCharset().name(), base64In,
					true);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_IS_BINARY) || getDocumentEncoding().startsWith("UTF-16")) {
			setBinary(properties.retrieveBool(DOCUMENT_IS_BINARY, false));
		} else {
			checkStreamDocumentBinary(new FileInputStream(reference), base64In, true);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_SIZE_BYTES)) {
			setDocumentSizeBytes(properties.retrieveLong(DOCUMENT_SIZE_BYTES, -1));
		} else {
			setDocumentSizeBytes(refFile.length());
		}
		if (retrieveProperties().containsProperty(DOCUMENT_CATEGORY)) {
			setDocumentCategory(properties.retrieveString(DOCUMENT_CATEGORY, getDocumentCategory()));
		} else {
			if (refFile.getParent() != null) {
				setDocumentCategory(refFile.getParent());
			}
		}
		if (retrieveProperties().containsProperty(DOCUMENT_NAME)) {
			setDocumentName(properties.retrieveString(DOCUMENT_NAME, getDocumentName()));
		} else {
			setDocumentName(refFile.getName());
		}
		logger.debug("Doc size bytes : " + getDocumentSizeBytes() + " compress limit: " + compressionLimitBytes
				+ " overflow limit:" + diskoverflowLimitBytes);
		if (getDocumentSizeBytes() > 0 && getDocumentSizeBytes() <= diskoverflowLimitBytes) {
			if (!isCompressed() && getDocumentSizeBytes() >= compressionLimitBytes) {
				logger.debug("Document is between compression limit and overflowlimit so reading compressed.");
				byte[] dbytes = compressInputStream(new FileInputStream(reference), base64In);
				setBase64In(false);
				setCompressed(true);
				setCompressionType(COMPRESSION_TYPE_GZIP);
				initializeInMemory(dbytes);
				success = true;
			} else {
				logger.debug("Document is smaller then compression limit so reading inmemory.");
				setBase64In(false);
				initializeInMemory(inputStreamToBytes(new FileInputStream(reference), false));
				success = true;
			}
		} else {
			setReference(true);
			setReferenceIsUrl(false);
			this.documentReference = reference;
			this.documentBytes = new byte[0];
			success = true;
		}
		return success;
	}

	private boolean initializeUrlReference(final String reference) throws Exception {
		boolean success = false;
		try {
			URL url = new URL(reference);
			if (retrieveProperties().containsProperty(DOCUMENT_IS_BASE64)) {
				setBase64In(properties.retrieveBool(DOCUMENT_IS_BASE64, false));
			} else {
				checkStreamDocumentBase64(url.openStream(), true);
			}
			if (retrieveProperties().containsProperty(DOCUMENT_COMPRESSION_TYPE)) {
				setCompressionType(properties.retrieveString(DOCUMENT_COMPRESSION_TYPE, COMPRESSION_TYPE_NONE));
			}
			if (retrieveProperties().containsProperty(DOCUMENT_IS_COMPRESSED)) {
				setCompressed(properties.retrieveBool(DOCUMENT_IS_COMPRESSED, false));
			} else {
				checkStreamDocumentCompressed(url.openStream(), base64In, true);
			}
			if (retrieveProperties().containsProperty(DOCUMENT_ENCODING)) {
				setDocumentEncoding(properties.retrieveString(DOCUMENT_ENCODING, Charset.defaultCharset().name()));
			} else {
				checkStreamDocumentEncoding(url.openStream(), Charset.defaultCharset().name(), base64In, true);
			}
			if (retrieveProperties().containsProperty(DOCUMENT_IS_BINARY)
					|| getDocumentEncoding().startsWith("UTF-16")) {
				setBinary(properties.retrieveBool(DOCUMENT_IS_BINARY, false));
			} else {
				checkStreamDocumentBinary(url.openStream(), base64In, true);
			}
			if (retrieveProperties().containsProperty(DOCUMENT_SIZE_BYTES)) {
				setDocumentSizeBytes(properties.retrieveLong(DOCUMENT_SIZE_BYTES, -1));
			} else {
				setDocumentSizeBytes(retrieveURLSize(url));
			}
			if (retrieveProperties().containsProperty(DOCUMENT_CATEGORY)) {
				setDocumentCategory(properties.retrieveString(DOCUMENT_CATEGORY, getDocumentCategory()));
			} else {
				if (url.getPath() != null) {
					setDocumentCategory(url.getPath());
				}
			}
			if (retrieveProperties().containsProperty(DOCUMENT_NAME)) {
				setDocumentName(properties.retrieveString(DOCUMENT_NAME, getDocumentName()));
			} else {
				setDocumentName(url.getFile());
			}
			logger.debug("Doc size bytes : " + getDocumentSizeBytes() + " compress limit: " + compressionLimitBytes
					+ " overflow limit:" + diskoverflowLimitBytes);
			if (getDocumentSizeBytes() > 0 && getDocumentSizeBytes() <= diskoverflowLimitBytes) {
				if (!isCompressed() && getDocumentSizeBytes() >= compressionLimitBytes) {
					logger.debug("Document is between compression limit and overflowlimit so reading compressed.");
					byte[] dbytes = compressInputStream(url.openStream(), base64In);
					setBase64In(false);
					setCompressed(true);
					setCompressionType(COMPRESSION_TYPE_GZIP);
					initializeInMemory(dbytes);
				} else {
					logger.debug("Document is smaller then compression limit so reading inmemory.");
					byte[] dbytes = inputStreamToBytes(url.openStream(), base64In);
					setBase64In(false);
					initializeInMemory(dbytes);
				}
			} else {
				if ("file".equalsIgnoreCase(url.getProtocol())) {
					this.documentReference = url.getPath();
					setReference(true);
					setReferenceIsUrl(false);
				} else {
					setReference(true);
					setReferenceIsUrl(true);
					this.documentReference = reference;
					this.documentBytes = new byte[0];
				}

			}
			success = true;
		} catch (MalformedURLException e) {
			logger.debug("Caught exception in check reference:" + e.getMessage(), e);
		}
		return success;
	}

	protected File retrieveOverflowFile() {
		String dfolder = sdf.format(new Date());
		String docCategory = getDocumentCategory();
		if (docCategory == null) {
			docCategory = "";
		}
		if (docCategory.startsWith(this.overflowlocation)) {
			docCategory = docCategory.substring(this.overflowlocation.length());
		}
		if (DocumentPropertyUtils.isEmpty(getDocumentName())) {
			initializeDocMeta();
		}
		String filename = overflowlocation + File.separator + dfolder + File.separator + removeSpecialFilenameChars(
				((docCategory.length() > 0) ? ("_" + docCategory) : "") + getDocumentName());
		File overflowFile = new File(filename);
		File parentDir = overflowFile.getParentFile();
		if (parentDir != null) {
			if (!parentDir.exists()) {
				parentDir.mkdirs();
				logger.debug(String.format("created non-existing parent folder %s",
						overflowFile.getParentFile().getAbsolutePath()));
			} else if (parentDir.isFile()) {
				File mvFile = new File(parentDir.getAbsolutePath() + "_" + System.currentTimeMillis());
				parentDir.renameTo(mvFile);
				parentDir = overflowFile.getParentFile();
				parentDir.mkdirs();
				logger.debug(String.format("moved existing file %s to %s", parentDir.getAbsolutePath(),
						mvFile.getAbsolutePath()));
			}
		}
		while (overflowFile.exists()) {
			String filename1 = filename + "_" + System.currentTimeMillis();
			overflowFile = new File(filename1);
			if (overflowFile.exists()) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException ie) {
					// ignore
				}
			}
		}
		logger.debug(String.format("Overflow file set to %s", overflowFile.getAbsolutePath()));
		return overflowFile;
	}

	private void initializeInMemory(final byte[] data) throws Exception {
		// make sure reference variable are reset
		this.documentReference = "";
		setReference(false);
		setReferenceIsUrl(false);

		byte[] dbytes = data;
		setDocumentSizeBytes(dbytes.length);
		if (retrieveProperties().containsProperty(DOCUMENT_SIZE_BYTES)) {
			setDocumentSizeBytes(properties.retrieveLong(DOCUMENT_SIZE_BYTES, getDocumentSizeBytes()));
		}
		if (retrieveProperties().containsProperty(DOCUMENT_IS_BASE64)) {
			setBase64In(properties.retrieveBool(DOCUMENT_IS_BASE64, false));
		} else {
			boolean b64;
			switch (base64CheckMethod.toUpperCase()) {
			case BASE64_CHECK_METHOD_SKIP:
				b64 = false;
				break;
			case BASE64_CHECK_METHOD_DECODE:
				b64 = doBase64DecodeCheck(dbytes, false);
				break;
			case BASE64_CHECK_METHOD_DECODECRC:
				b64 = doBase64DecodeCheck(dbytes, true);
				break;
			case BASE64_CHECK_METHOD_BASIC:
			default:
				b64 = com.ceva.eai.bwce.generic.cfw.utils.encoding.Base64.isBase64(dbytes);
				break;
			}
			setBase64In(b64);
			logger.debug("Initial Data is base64:" + b64);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_COMPRESSION_TYPE)) {
			setCompressionType(properties.retrieveString(DOCUMENT_COMPRESSION_TYPE, COMPRESSION_TYPE_NONE));
		}
		if (retrieveProperties().containsProperty(DOCUMENT_IS_COMPRESSED)) {
			setCompressed(properties.retrieveBool(DOCUMENT_IS_COMPRESSED, false));
		} else {
			checkStreamDocumentCompressed(new ByteArrayInputStream(dbytes), base64In, true);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_ENCODING)) {
			setDocumentEncoding(properties.retrieveString(DOCUMENT_ENCODING, Charset.defaultCharset().name()));
		} else {
			checkStreamDocumentEncoding(new ByteArrayInputStream(dbytes), Charset.defaultCharset().name(), base64In,
					true);
		}
		if (retrieveProperties().containsProperty(DOCUMENT_IS_BINARY) || getDocumentEncoding().startsWith("UTF-16")) {
			setBinary(properties.retrieveBool(DOCUMENT_IS_BINARY, false));
		} else {
			checkStreamDocumentBinary(new ByteArrayInputStream(dbytes), base64In, true);
		}
		if (dbytes.length >= diskoverflowLimitBytes) {
			File overflowFile = retrieveOverflowFile();
			String filename = overflowFile.getAbsolutePath();
			logger.debug(String.format("Document size (%d) is larger then diskoverflowLimit (%d), writing to disk:%s",
					dbytes.length, diskoverflowLimitBytes, filename));
			byteArrayToFile(dbytes, filename, base64In);
			setBase64In(false);
			setReference(true);
			setReferenceIsUrl(false);
			this.documentReference = filename;
			this.documentBytes = new byte[0];
			if (retrieveProperties().containsProperty(DOCUMENT_CATEGORY)) {
				setDocumentCategory(properties.retrieveString(DOCUMENT_CATEGORY, getDocumentCategory()));
			} else {
				if (overflowFile.getParent() != null) {
					setDocumentCategory(overflowFile.getParent());
				}
			}
		} else if (!isCompressed() && dbytes.length >= compressionLimitBytes) {
			logger.debug(
					String.format("Document size (%d) is larger then compressionLimitBytes (%d), applying compression",
							dbytes.length, compressionLimitBytes));
			dbytes = compressByteArray(dbytes, base64In);
			setBase64In(false);
			setCompressed(true);
			setCompressionType(COMPRESSION_TYPE_GZIP);
		}
		if (!isReference()) {
			this.documentBytes = dbytes;
		}
	}

	protected byte[] compressByteArray(byte[] data, boolean isBase64) throws IOException {
		return compressInputStream(new ByteArrayInputStream(data), isBase64);
	}

	protected byte[] compressInputStream(InputStream is, boolean isBase64) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			try {
				BufferedInputStream bis;
				if (isBase64) {
					bis = new BufferedInputStream(new Base64InputStream(is));
				} else {
					bis = new BufferedInputStream(is);
				}
				try {
					GZIPOutputStream zos = new GZIPOutputStream(baos);
					try {
						IOUtils.copy(bis, zos);
					} finally {
						IOUtils.closeQuietly(zos);
					}
				} finally {
					IOUtils.closeQuietly(bis);
				}
			} finally {
				IOUtils.closeQuietly(is);
			}
		} finally {
			IOUtils.closeQuietly(baos);
		}
		byte[] output = baos.toByteArray();
		return output;
	}

	private String checkCompressionType(BufferedInputStream instream, boolean close) {
		final int ZIP_MAGIC = 0x4b50;
		String ctype = COMPRESSION_TYPE_NONE;
		try {
			int magic = 0;
			try {
				instream.mark(2);
				magic = instream.read() & 0xff | ((instream.read() << 8) & 0xff00);
				instream.reset();
			} catch (IOException e) {
				logger.warn("Error checking compression type:" + e.getMessage(), e);
			}
			if (magic == GZIPInputStream.GZIP_MAGIC) {
				ctype = COMPRESSION_TYPE_GZIP;
			} else if (magic == ZIP_MAGIC) {
				ctype = COMPRESSION_TYPE_ZIP;
			}
		} finally {
			if (close) {
				IOUtils.closeQuietly(instream);
			}
		}
		return ctype;
	}

	public byte[] retrieveContentBytes() throws Exception {
		byte[] data = new byte[0];
		InputStream inStream = retrieveContentStream();
		try {
			data = IOUtils.toByteArray(inStream);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return data;
	}

	public String retrieveContentStringBase64() throws Exception {
		String result = "";
		InputStream inStream = retrieveContentStream();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				OutputStream base64Out = new Base64OutputStream(baos, true, 0, new byte[0]);
				try {
					IOUtils.copyLarge(inStream, base64Out);
				} finally {
					IOUtils.closeQuietly(base64Out);
				}
			} finally {
				IOUtils.closeQuietly(baos);
			}
			result = baos.toString();
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return result;
	}

	public String retrieveContentString() throws Exception {
		String result = "";
		InputStream inStream = retrieveContentStream();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				if (isBinary()) {
					Base64OutputStream base64Out = new Base64OutputStream(baos, true, 0, new byte[0]);
					try {
						IOUtils.copyLarge(inStream, base64Out);
					} finally {
						IOUtils.closeQuietly(base64Out);
					}
				} else {
					IOUtils.copyLarge(inStream, baos);
				}
			} finally {
				IOUtils.closeQuietly(baos);
			}
			result = baos.toString(getDocumentEncoding());
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return result;
	}

	public String retrieveContentStringPart(long length) throws Exception {
		String result = "";
		InputStream inStream = retrieveContentStream();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				if (isBinary()) {
					Base64OutputStream base64Out = new Base64OutputStream(baos, true, 0, new byte[0]);
					try {
						IOUtils.copyLarge(inStream, base64Out, 0, length);
					} finally {
						IOUtils.closeQuietly(base64Out);
					}
				} else {
					IOUtils.copyLarge(inStream, baos, 0, length);
				}
			} finally {
				IOUtils.closeQuietly(baos);
			}
			result = baos.toString(getDocumentEncoding());
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return result;
	}

	public InputStream retrieveContentStream() throws Exception {
		InputStream inStream;
		if (isReference()) {
			if (isReferenceIsUrl()) {
				URL url = new URL(documentReference);
				inStream = url.openStream();
			} else {
				inStream = new FileInputStream(documentReference);
			}
		} else {
			inStream = new ByteArrayInputStream(documentBytes);
		}
		if (base64In) {
			inStream = new Base64InputStream(inStream);
		}
		if (isCompressed()) {
			if (COMPRESSION_TYPE_GZIP.equalsIgnoreCase(getCompressionType())) {
				inStream = new GZIPInputStream(inStream);
			} else {
				ZipInputStream zip = new ZipInputStream(inStream);
				inStream = zip;
				ZipEntry entry;
				// find first file
				while ((entry = zip.getNextEntry()) != null) {
					if (!entry.isDirectory()) {
						break;
					}
				}
			}
		}
		return inStream;
	}
	
    public InputStream retrieveEmbeddableStream() throws Exception {
        InputStream inStream = retrieveContentStream();
        if (binary) {
            inStream = new Base64InputStream(inStream, true);
        }
        return inStream;
    }	

	public Reader retrieveContentReader() throws Exception {
		String encoding = getDocumentEncoding();
		if (DocumentPropertyUtils.isEmpty(encoding)) {
			encoding = "UTF-8";
		}
		return new InputStreamReader(retrieveContentStream(), encoding);
	}

	public InputStream retrieveContentStreamForZIP() throws Exception {
		InputStream inStream;
		if (isReference()) {
			if (isReferenceIsUrl()) {
				URL url = new URL(documentReference);
				inStream = url.openStream();
			} else {
				inStream = new FileInputStream(documentReference);
			}
		} else {
			inStream = new ByteArrayInputStream(documentBytes);
		}
		if (base64In) {
			inStream = new Base64InputStream(inStream);
		}
		if (isCompressed() && COMPRESSION_TYPE_GZIP.equalsIgnoreCase(getCompressionType())) {
			inStream = new GZIPInputStream(inStream);
		}
		return inStream;
	}

	@SuppressWarnings("resource")
	public String retrieveContentStringZIP() throws Exception {
		String result = "";
		InputStream inStream = retrieveContentStreamForZIP();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				OutputStream outStream = new Base64OutputStream(baos, true, 0, new byte[0]);
				if (!isCompressed() || !COMPRESSION_TYPE_ZIP.equalsIgnoreCase(getCompressionType())) {
					outStream = new ZipOutputStream(new Base64OutputStream(baos, true, 0, new byte[0]));
					ZipEntry e = new ZipEntry(this.getDocumentName());
					((ZipOutputStream) outStream).putNextEntry(e);
				}
				try {
					IOUtils.copyLarge(inStream, outStream);
				} finally {
					IOUtils.closeQuietly(outStream);
				}
			} finally {
				IOUtils.closeQuietly(baos);
			}
			result = baos.toString();
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return result;
	}

	public InputStream retrieveContentStreamForGZIP() throws Exception {
		InputStream inStream;
		if (isReference()) {
			if (isReferenceIsUrl()) {
				URL url = new URL(documentReference);
				inStream = url.openStream();
			} else {
				inStream = new FileInputStream(documentReference);
			}
		} else {
			inStream = new ByteArrayInputStream(documentBytes);
		}
		if (base64In) {
			inStream = new Base64InputStream(inStream);
		}
		if (isCompressed() && COMPRESSION_TYPE_ZIP.equalsIgnoreCase(getCompressionType())) {
			ZipInputStream zip = new ZipInputStream(inStream);
			inStream = zip;
			ZipEntry entry;
			// find first file
			while ((entry = zip.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					break;
				}
			}
		}
		return inStream;
	}

	@SuppressWarnings("resource")
	public String retrieveContentStringGZIP() throws Exception {
		String result = "";
		InputStream inStream = retrieveContentStreamForGZIP();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				OutputStream outStream = new Base64OutputStream(baos, true, 0, new byte[0]);
				if (!isCompressed() || !COMPRESSION_TYPE_GZIP.equalsIgnoreCase(getCompressionType())) {
					outStream = new GZIPOutputStream(new Base64OutputStream(baos, true, 0, new byte[0]));
				}
				try {
					IOUtils.copyLarge(inStream, outStream);
				} finally {
					IOUtils.closeQuietly(outStream);
				}
			} finally {
				IOUtils.closeQuietly(baos);
			}
			result = baos.toString();
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return result;
	}

	public String retrieveInternalString(boolean dereference) throws Exception {
		String result = "";
		InputStream inStream = retrieveInternalStream(dereference);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				boolean needBase64 = !base64In && (isBinary() || isCompressed() || isEncodingNonDefault())
						&& ((isReference() && dereference) || (!isReference()));
				if (needBase64) {
					Base64OutputStream base64Out = new Base64OutputStream(baos, true, 0, new byte[0]);
					try {
						IOUtils.copyLarge(inStream, base64Out);
					} finally {
						IOUtils.closeQuietly(base64Out);
					}
				} else {
					IOUtils.copyLarge(inStream, baos);
				}
			} finally {
				IOUtils.closeQuietly(baos);
			}
			result = baos.toString();
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return result;
	}

	public String retrieveInternalStringBase64(boolean dereference) throws Exception {
		String result = "";
		InputStream inStream = retrieveInternalStream(dereference);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				if (!base64In) {
					Base64OutputStream base64Out = new Base64OutputStream(baos, true, 0, new byte[0]);
					try {
						IOUtils.copyLarge(inStream, base64Out);
					} finally {
						IOUtils.closeQuietly(base64Out);
					}
				} else {
					IOUtils.copyLarge(inStream, baos);
				}
			} finally {
				IOUtils.closeQuietly(baos);
			}
			result = baos.toString();
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return result;
	}

	public byte[] retrieveInternalBytes(boolean dereference) throws Exception {
		byte[] data = new byte[0];
		InputStream inStream = retrieveInternalStream(dereference);
		try {
			data = IOUtils.toByteArray(inStream);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return data;
	}

	public InputStream retrieveInternalStream(boolean dereference) throws Exception {
		InputStream inStream;
		if (isReference()) {
			if (dereference) {
				inStream = new FileInputStream(documentReference);
			} else {
				inStream = new ByteArrayInputStream(documentReference.getBytes());
			}
		} else {
			inStream = new ByteArrayInputStream(documentBytes);
		}
		return inStream;
	}

	public Reader retrieveInternalReader(boolean dereference) throws Exception {
		Reader reader;
		if (isReference()) {
			if (dereference) {
				reader = new FileReader(documentReference);
			} else {
				reader = new StringReader(documentReference);
			}
		} else {
			reader = new InputStreamReader(new ByteArrayInputStream(documentBytes));
		}
		return reader;
	}

	public Document() {
	}

	public void clearProperties() {
		this.properties.clearProperties();
	}

}
