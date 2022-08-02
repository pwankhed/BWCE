package com.ceva.eai.bwce.generic.cfw.model;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tibco.bw.palette.shared.java.BWActivityContext;
import com.tibco.bw.runtime.ActivityContext;

public class DocumentFactory implements Serializable {

	static private final long serialVersionUID = -5986117891664173026L;

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	public final String DOC_DATA_LAYOUT_INTERNAL = "internal";
	public final String DOC_DATA_LAYOUT_INTERNAL_BASE64 = "internal_base64";
	public final String DOC_DATA_LAYOUT_INTERNAL_DEREFERENCE = "internal_dereference";
	public final String DOC_DATA_LAYOUT_INTERNAL_DEREFERENCE_BASE64 = "internal_dereference_base64";
	public final String DOC_DATA_LAYOUT_EXTERNAL = "external";
	public final String DOC_DATA_LAYOUT_EXTERNAL_BASE64 = "external_base64";
	public final String DOC_DATA_LAYOUT_EXTERNAL_COMPRESSED_GZIP = "external_gzip";
	public final String DOC_DATA_LAYOUT_EXTERNAL_COMPRESSED_ZIP = "external_zip";
	private ActivityContext<?> context;
		

	@BWActivityContext
	public void assignContext(final ActivityContext<?> context) {
		this.context = context;
	}

	/* public static <N> Object retrieveJavaObject(ProcessContext<N> context, String key) {
		SerializableActivityResource objResource = null;
		Object declaredobject = null;
		ActivityResource resource = context.getProcessResource(key);
		if (resource instanceof SerializableActivityResource)
			objResource = (SerializableActivityResource) resource;
		if (objResource != null)
			declaredobject = objResource.getResource();
		return declaredobject;
	}

	  public static <N> String setJavaObjResource(ProcessContext<N> pc, Object javaObjectRef, ActivityContext<N> activityContext, String elementIndex) {
		    String claimCheckString = String.valueOf(javaObjectRef.getClass().getName()) + activityContext.getActivityId() + elementIndex;
		    JavaSerializableResourceImpl objResource = new JavaSerializableResourceImpl();
		    objResource.setResource(javaObjectRef);
		    pc.setProcessResource(claimCheckString, objResource);
		    return claimCheckString;
		  }
		  */
	  
	public DocumentFactory() {
	}

	public void addDocumentProperties(Document doc, String properties) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		try {
			doc.addPropertiesString(properties, true);
		} catch (Exception e) {
			logger.error("Error in addDocumentProperties:" + e, e);
			throw e;
		}
	}

	public void addDocumentProperties(Document doc, String[] properties) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		try {
			doc.addPropertiesStringArray(properties, true);
		} catch (Exception e) {
			logger.error("Error in addDocumentProperties:" + e, e);
			throw e;
		}
	}

	public void addDocumentProperties(Document doc, Set<DocumentProperty> properties) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		try {
			doc.addPropertiesSet(properties, true);
		} catch (Exception e) {
			logger.error("Error in addDocumentProperties:" + e, e);
			throw e;
		}
	}

	public void addDocumentProperties(Document doc, DocumentProperties properties) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		try {
			doc.addPropertiesSet(properties.getProperty(), true);
		} catch (Exception e) {
			logger.error("Error in addDocumentProperties:" + e, e);
			throw e;
		}
	}

	public DocumentProperties getDocumentProperties(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		return doc.getProperties();
	}

	public void resetDocumentProperties(Document doc, DocumentProperties properties) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		try {
			doc.clearProperties();
			doc.addPropertiesSet(properties.getProperty(), false);
		} catch (Exception e) {
			logger.error("Error in addDocumentProperties:" + e, e);
			throw e;
		}
	}

	public void resetDocumentProperties(Document doc, Set<DocumentProperty> properties) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		try {
			doc.clearProperties();
			doc.addPropertiesSet(properties, false);
		} catch (Exception e) {
			logger.error("Error in addDocumentProperties:" + e, e);
			throw e;
		}
	}

	public void deleteDocumentProperties(Document doc, String categoryMask, String nameMask) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		try {
			doc.retrieveProperties().deleteDocumentProperties(categoryMask, nameMask);
		} catch (Exception e) {
			logger.error("Error in deleteDocumentProperties:" + e, e);
			throw e;
		}
	}

	public String getDocumentProperty(Document doc, String category, String name, String defaultValue)
			throws Exception {
		String result = defaultValue;
		if (doc != null) {
			try {
				result = doc.getProperties().retrievePropertyValue(category, name, defaultValue);
			} catch (Exception e) {
				logger.error("Error in getDocumentProperty:" + e, e);
				throw e;
			}
		}
		return result;
	}

	public void setDocumentProperty(Document doc, String category, String name, String type, String value)
			throws Exception {
		if (doc != null && name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
			try {
				String cat = category;
				String typ = type;
				if (cat == null || cat.isEmpty()) {
					cat = Document.CATEGORY;
				}
				if (typ == null || typ.isEmpty()) {
					typ = DocumentProperty.STRING_TYPE;
				}
				doc.getProperties().addProperty(cat, name, typ, value);
			} catch (Exception e) {
				logger.error("Error in setDocumentProperty:" + e, e);
				throw e;
			}
		}
	}

	public String retrieveDocumentData(Document doc, String layout) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			if (DOC_DATA_LAYOUT_INTERNAL.equalsIgnoreCase(layout)) {
				result = doc.retrieveInternalString(false);
			} else if (DOC_DATA_LAYOUT_INTERNAL_BASE64.equalsIgnoreCase(layout)) {
				result = doc.retrieveInternalStringBase64(false);
			} else if (DOC_DATA_LAYOUT_INTERNAL_DEREFERENCE.equalsIgnoreCase(layout)) {
				result = doc.retrieveInternalString(true);
			} else if (DOC_DATA_LAYOUT_INTERNAL_DEREFERENCE_BASE64.equalsIgnoreCase(layout)) {
				result = doc.retrieveInternalStringBase64(true);
			} else if (DOC_DATA_LAYOUT_EXTERNAL.equalsIgnoreCase(layout)) {
				result = doc.retrieveContentString();
			} else if (DOC_DATA_LAYOUT_EXTERNAL_COMPRESSED_GZIP.equalsIgnoreCase(layout)) {
				result = doc.retrieveContentStringGZIP();
			} else if (DOC_DATA_LAYOUT_EXTERNAL_COMPRESSED_ZIP.equalsIgnoreCase(layout)) {
				result = doc.retrieveContentStringZIP();
			} else if (DOC_DATA_LAYOUT_EXTERNAL_BASE64.equalsIgnoreCase(layout)) {
				result = doc.retrieveContentStringBase64();
			} else {
				throw new Exception("Unknown document layout: " + layout);
			}
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentData:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentData(Document doc, String layout, boolean dereference) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			if (DOC_DATA_LAYOUT_INTERNAL.equalsIgnoreCase(layout)) {
				result = doc.retrieveInternalString(dereference);
			} else if (DOC_DATA_LAYOUT_INTERNAL_BASE64.equalsIgnoreCase(layout)) {
				result = doc.retrieveInternalStringBase64(false);
			} else if (DOC_DATA_LAYOUT_INTERNAL_DEREFERENCE.equalsIgnoreCase(layout)) {
				result = doc.retrieveInternalString(true);
			} else if (DOC_DATA_LAYOUT_INTERNAL_DEREFERENCE_BASE64.equalsIgnoreCase(layout)) {
				result = doc.retrieveInternalStringBase64(true);
			} else if (DOC_DATA_LAYOUT_EXTERNAL.equalsIgnoreCase(layout)) {
				result = doc.retrieveContentString();
			} else if (DOC_DATA_LAYOUT_EXTERNAL_COMPRESSED_GZIP.equalsIgnoreCase(layout)) {
				result = doc.retrieveContentStringGZIP();
			} else if (DOC_DATA_LAYOUT_EXTERNAL_COMPRESSED_ZIP.equalsIgnoreCase(layout)) {
				result = doc.retrieveContentStringZIP();
			} else if (DOC_DATA_LAYOUT_EXTERNAL_BASE64.equalsIgnoreCase(layout)) {
				result = doc.retrieveContentStringBase64();
			} else {
				throw new Exception("Unknown document layout: " + layout);
			}
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentData:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentDataExternal(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			result = doc.retrieveContentString();
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataExternal:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentDataExternalPart(Document doc, long length) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			result = doc.retrieveContentStringPart(length);
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataExternalPart:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public byte[] retrieveDocumentDataExternalBytes(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		byte[] result = new byte[0];
		try {
			result = doc.retrieveContentBytes();
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataExternal:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentDataExternalBase64(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			result = doc.retrieveContentStringBase64();
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataExternalBase64:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentDataExternalGZIP(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			result = doc.retrieveContentStringGZIP();
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataExternalGZIP:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentDataExternalZIP(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			result = doc.retrieveContentStringZIP();
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataExternalZIP:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentDataInternal(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			result = doc.retrieveInternalString(false);
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataInternal:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentDataInternal(Document doc, boolean dereference) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			result = doc.retrieveInternalString(dereference);
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataInternal:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String retrieveDocumentDataInternalBase64(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String result = "";
		try {
			result = doc.retrieveInternalStringBase64(false);
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataInternalBase64:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public byte[] retrieveDocumentDataInternalBytes(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		byte[] result = new byte[0];
		try {
			result = doc.retrieveInternalBytes(false);
		} catch (Exception e) {
			logger.error("Error in retrieveDocumentDataInternalBytes:" + e, e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public String toFile(Document doc) throws Exception {
		if (doc == null) {
			throw new Exception("No document supplied");
		}
		String fileReference = null;
		if (doc.isReference()) {
			fileReference = doc.retrieveInternalString(false);
		} else {
			InputStream dis = doc.retrieveContentStream();
			try {
				File outFile = doc.retrieveOverflowFile();
				fileReference = outFile.getAbsolutePath();
				FileOutputStream fos = new FileOutputStream(outFile);
				try {
					IOUtils.copyLarge(dis, fos);
				} finally {
					IOUtils.closeQuietly(fos);
				}
			} finally {
				IOUtils.closeQuietly(dis);
			}
		}
		return fileReference;
	}

	public Document createDocument(byte[] data, String[] properties) throws Exception {
		Document doc = null;
		try {
			doc = DocumentBuilder.newInstance(context).withDocumentPropertiesStringArray(properties).addBytesData(data)
					.build();
		} catch (Exception e) {
			logger.error("Error in createDocument:" + e, e);
			throw e;
		}
		return doc;
	}

	public Document createDocument(String data, String[] properties) throws Exception {
		Document doc = null;
		try {
			doc = DocumentBuilder.newInstance(context).withDocumentPropertiesStringArray(properties)
					.addStringData(data, Charset.defaultCharset().name()).build();
		} catch (Exception e) {
			logger.error("Error in createDocument:" + e, e);
			throw e;
		}
		return doc;
	}

	public Document createCloneDocument(Document source, String[] properties) throws Exception {
		if (source == null) {
			throw new Exception("No source document supplied");
		}
		Document cloneDoc = null;
		try {
			cloneDoc = DocumentBuilder.newInstance(context).withSourceDocument(source)
					.withDocumentPropertiesStringArray(properties).build();
		} catch (Exception e) {
			logger.error("Error in createCloneDocument:" + e, e);
			throw e;
		}
		return cloneDoc;
	}

	public Document createEmbeddedDocument(Document source, String[] properties, String envelopeXML,
			String insertXpathExpression) throws Exception {
		if (source == null) {
			throw new Exception("No source document supplied");
		}
		Document embeddedDoc = null;
		try {
			embeddedDoc = DocumentBuilder.newInstance(context).withSourceDocument(source)
					.withDocumentPropertiesStringArray(properties).buildEmbeddedXML(envelopeXML, insertXpathExpression);
		} catch (Exception e) {
			logger.error("Error in createEmbeddedDocument:" + e, e);
			throw e;
		}
		return embeddedDoc;
	}

	public Document createChildDocument(Document source, byte[] data, String[] properties) throws Exception {
		if (source == null) {
			throw new Exception("No source document supplied");
		}
		Document childDoc = null;
		try {
			childDoc = DocumentBuilder.newInstance(context).withDocumentPropertiesStringArray(properties)
					.withSourceDocument(source).addBytesData(data).build();
		} catch (Exception e) {
			logger.error("Error in createChildDocument:" + e, e);
			throw e;
		}
		return childDoc;
	}

	public Document createChildDocument(Document source, String data, String[] properties) throws Exception {
		if (source == null) {
			throw new Exception("No source document supplied");
		}
		Document childDoc = null;
		try {
			childDoc = DocumentBuilder.newInstance(context).withDocumentPropertiesStringArray(properties)
					.withSourceDocument(source).addStringData(data, Charset.defaultCharset().name()).build();
		} catch (Exception e) {
			logger.error("Error in createChildDocument:" + e, e);
			throw e;
		}
		return childDoc;
	}
}
