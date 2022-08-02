package com.ceva.eai.bwce.generic.cfw.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;

import com.ceva.eai.bwce.generic.cfw.utils.xmlio.SimpleXPathMerge;
import com.tibco.bw.runtime.ActivityContext;
import com.tibco.bw.runtime.ModulePropertyNotFoundException;
import com.tibco.bw.runtime.ModulePropertyNotRegisteredException;
import com.tibco.bw.runtime.ProcessPropertyNotFoundException;
import com.tibco.bw.runtime.ProcessPropertyNotRegisteredException;

public class DocumentBuilder implements Serializable {

	private static final long serialVersionUID = -6371674779521088491L;
	/* global var names */
	protected static final String TIBCO_GV_MODULE_NAME = "//CommonFramework//";
	protected static final String TIBCO_GV_DISKBASE_PATH = "/CommonCore/Docs/diskBasePath";
	protected static final String TIBCO_GV_DISKOVERFLOW_PATH = "/CommonCore/Docs/diskoverflowPath";
	protected static final String TIBCO_GV_COMPRESSION_LIMIT = "/CommonCore/Docs/compressionLimitBytes";
	protected static final String TIBCO_GV_DISKOVERFLOW_LIMIT = "/CommonCore/Docs/diskOverflowLimitBytes";
	protected static final String TIBCO_GV_BASE64_CHECK_METHOD = "/CommonCore/Docs/base64CheckMethod";
	protected static final String TIBCO_GV_BINARY_CHECK_METHOD = "/CommonCore/Docs/binaryCheckMethod";
	protected static final String TIBCO_GV_BINARY_CHECK_LOOSE_MAXBYTES = "/CommonCore/Docs/binaryCheckLooseMaxBytes";
	protected static final String TIBCO_GV_BINARY_CHECK_LOOSE_MAXNONTEXT = "/CommonCore/Docs/binaryCheckLooseMaxNonText";
	protected static final String TIBCO_GV_INHERITED_CATEGORY_SUPPRESS_MASK = "/CommonCore/Docs/inheritedCategorySuppressMask";
	protected static final String TIBCO_GV_MAX_REFERENCENAME_LENGTH = "/CommonCore/Docs/MaxReferenceNameLength";

	/* env fields */
	private long compressionLimitBytes = 1000000;
	private long diskoverflowLimitBytes = 5000000;
	private int maxReferenceNameLength = 4096;
	private String diskBaselocation = "/local/";
	private String overflowlocation = "/local/tibco/data/";
	private String binaryCheckMethod = Document.BINARY_CHECK_METHOD_LOOSE;
	private String base64CheckMethod = Document.BASE64_CHECK_METHOD_BASIC;
	private long binaryCheckLooseMaxBytes = 100000;
	private long binaryCheckLooseMaxNonText = 10;
	private String inheritedCategorySuppressMask = "Replay|Delay|DocFault|HandleError";
	private Document sourceDocument;
	/* internal fields */
	private DeferredFileOutputStream dataStream;
	private DocumentPropertySet<DocumentProperty> properties = new DocumentPropertySet<DocumentProperty>();

	public static DocumentBuilder newInstance(final ActivityContext<?> ac) {
		// System.out.println(ac.getModuleName());
		return new DocumentBuilder().withEnvBase64CheckMethod(retrieveEnvString(ac, TIBCO_GV_BASE64_CHECK_METHOD))
				.withEnvBinaryCheckLooseMaxBytes(retrieveEnvLong(ac, TIBCO_GV_BINARY_CHECK_LOOSE_MAXBYTES))
				.withEnvBinaryCheckLooseMaxNonText(retrieveEnvLong(ac, TIBCO_GV_BINARY_CHECK_LOOSE_MAXNONTEXT))
				.withEnvBinaryCheckMethod(retrieveEnvString(ac, TIBCO_GV_BINARY_CHECK_METHOD))
				.withEnvCompressionLimitBytes(retrieveEnvLong(ac, TIBCO_GV_COMPRESSION_LIMIT))
				.withEnvDiskBaselocation(retrieveEnvString(ac, TIBCO_GV_DISKBASE_PATH))
				.withEnvDiskoverflowLimitBytes(retrieveEnvLong(ac, TIBCO_GV_DISKOVERFLOW_LIMIT))
				.withEnvInheritedCategorySuppressMask(retrieveEnvString(ac, TIBCO_GV_INHERITED_CATEGORY_SUPPRESS_MASK))
				.withEnvMaxReferenceNameLength(retrieveEnvInt(ac, TIBCO_GV_MAX_REFERENCENAME_LENGTH))
				.withEnvOverflowlocation(retrieveEnvString(ac, TIBCO_GV_DISKOVERFLOW_PATH));
	}

	private static String retrieveEnvString(final ActivityContext<?> ac, final String name) {
		String result = null;
		try {
			result = ac.getProcessProperty(name);
		} catch (ProcessPropertyNotRegisteredException | ProcessPropertyNotFoundException e) {
			// e.printStackTrace();
		}
		if (result == null) {
			try {
				ac.registerModuleProperty(TIBCO_GV_MODULE_NAME + name);
				result = ac.getModuleProperty(TIBCO_GV_MODULE_NAME + name);
			} catch (ModulePropertyNotRegisteredException e) {
				// e.printStackTrace();
			} catch (ModulePropertyNotFoundException e) {
				throw new RuntimeException(String.format("Env property: %s cannot be found.", name));
			}
		}
		// System.out.println(String.format("Env property: %s=%s", name, result));
		return result;
	}

	private static long retrieveEnvLong(final ActivityContext<?> ac, final String name) {
		long result = Long.parseLong(retrieveEnvString(ac, name));
		return result;
	}

	private static int retrieveEnvInt(final ActivityContext<?> ac, final String name) {
		int result = Integer.parseInt(retrieveEnvString(ac, name));
		return result;
	}

	public DocumentBuilder withEnvCompressionLimitBytes(long limit) {
		if (limit > 0) {
			this.compressionLimitBytes = limit;
		}
		return this;
	}

	public DocumentBuilder withEnvDiskoverflowLimitBytes(long limit) {
		if (limit > 0) {
			this.diskoverflowLimitBytes = limit;
		}
		return this;
	}

	public DocumentBuilder withEnvMaxReferenceNameLength(int maxLength) {
		if (maxLength > 0) {
			this.maxReferenceNameLength = maxLength;
		}
		return this;
	}

	public DocumentBuilder withEnvDiskBaselocation(String location) {
		if (!DocumentPropertyUtils.isEmpty(location)) {
			this.diskBaselocation = location;
		}
		return this;
	}

	public DocumentBuilder withEnvOverflowlocation(String location) {
		if (!DocumentPropertyUtils.isEmpty(location)) {
			this.overflowlocation = location;
		}
		return this;
	}

	public DocumentBuilder withEnvBinaryCheckMethod(String method) {
		if (!DocumentPropertyUtils.isEmpty(method)) {
			this.binaryCheckMethod = method;
		}
		return this;
	}

	public DocumentBuilder withEnvBase64CheckMethod(String method) {
		if (!DocumentPropertyUtils.isEmpty(method)) {
			this.base64CheckMethod = method;
		}
		return this;
	}

	public DocumentBuilder withEnvBinaryCheckLooseMaxBytes(long maxBytes) {
		if (maxBytes > 0) {
			this.binaryCheckLooseMaxBytes = maxBytes;
		}
		return this;
	}

	public DocumentBuilder withEnvBinaryCheckLooseMaxNonText(long maxBytes) {
		if (maxBytes > 0) {
			this.binaryCheckLooseMaxNonText = maxBytes;
		}
		return this;
	}

	public DocumentBuilder withEnvInheritedCategorySuppressMask(String mask) {
		if (!DocumentPropertyUtils.isEmpty(mask)) {
			this.inheritedCategorySuppressMask = mask;
		}
		return this;
	}

	public Document getSourceDocument() {
		return sourceDocument;
	}

	public void setSourceDocument(Document sourceDocument) {
		this.sourceDocument = sourceDocument;
	}

	public DocumentBuilder withSourceDocument(Document sourceDocument) {
		this.sourceDocument = sourceDocument;
		return this;
	}

	public DocumentBuilder withDocumentPropertiesStringArray(String[] stringArray) {
		if (stringArray != null) {
			this.properties.addAll(DocumentPropertyUtils.parsePropertyArray(stringArray));
		}
		return this;
	}

	public DocumentBuilder withDocumentProperties(DocumentProperties properties) {
		if (properties != null) {
			this.properties.addAll(properties.getProperty());
		}
		return this;
	}

	public DocumentBuilder withDocumentPropertySet(Set<DocumentProperty> propertySet) {
		if (propertySet != null) {
			this.properties.addAll(propertySet);
		}
		return this;
	}

	private DeferredFileOutputStream getDeferredOutputStream() throws IOException {
		if (dataStream == null) {
			dataStream = new DeferredFileOutputStream((int) this.diskoverflowLimitBytes,
					File.createTempFile("doc", "tmp"));
		}
		return dataStream;
	}

	public DocumentBuilder addStringData(String data, String encoding) throws IOException {
		if (data != null) {
			Charset charset;
			if (encoding == null || encoding.length() == 0) {
				charset = Charset.defaultCharset();
			} else {
				charset = Charset.forName(encoding);
			}
			IOUtils.write(data.getBytes(charset), getDeferredOutputStream());
		}
		return this;
	}

	public DocumentBuilder addBytesData(byte[] data) throws IOException {
		if (data != null) {
			IOUtils.write(data, getDeferredOutputStream());
		}
		return this;
	}

	public DocumentBuilder addFileData(File reference) throws IOException {
		if (reference != null && reference.exists() && reference.isFile()) {
			try (FileInputStream fis = new FileInputStream(reference)) {
				IOUtils.copy(fis, getDeferredOutputStream());
			}
		}
		return this;
	}

	public DocumentBuilder addStreamData(InputStream instream) throws IOException {
		if (instream != null) {
			IOUtils.copy(instream, getDeferredOutputStream());
		}
		return this;
	}

	public DocumentBuilder addReaderData(Reader reader) throws IOException {
		if (reader != null) {
			IOUtils.copy(reader, getDeferredOutputStream(), "UTF-8");
		}
		return this;
	}

	public Document build() throws Exception {
		Document doc = new Document();
		doc.withCompressionLimitBytes(compressionLimitBytes);
		doc.withDiskoverflowLimitBytes(diskoverflowLimitBytes);
		doc.withMaxReferenceNameLength(maxReferenceNameLength);
		doc.withDiskBaselocation(diskBaselocation);
		doc.withOverflowlocation(overflowlocation);
		doc.withBinaryCheckMethod(binaryCheckMethod);
		doc.withBase64CheckMethod(base64CheckMethod);
		doc.withBinaryCheckLooseMaxBytes(binaryCheckLooseMaxBytes);
		doc.withBinaryCheckLooseMaxNonText(binaryCheckLooseMaxNonText);
		doc.withInheritedCategorySuppressMask(inheritedCategorySuppressMask);
		if (sourceDocument != null) {
			if (this.dataStream == null) {
				doc.initializeClone(sourceDocument);
			} else {
				doc.initializeFromParent(sourceDocument);
			}
		}
		if (!this.properties.isEmpty()) {
			doc.addPropertiesSet(properties, false);
			//properties.clear();
		}
		doc.initialize();
		byte[] data = null;
		if (this.dataStream != null) {
			this.dataStream.close();
			if (this.dataStream.isInMemory()) {
				data = dataStream.getData();
			} else {
				File overflowSrc = this.dataStream.getFile();
				File overflowDest = doc.retrieveOverflowFile();
				if (!overflowSrc.renameTo(overflowDest)) {
					throw new Exception(String.format("Unable to relocate %s to %s", overflowSrc.getAbsolutePath(),
							overflowDest.getAbsolutePath()));
				}
				data = overflowDest.getAbsolutePath().getBytes();
			}
		}
		doc.initializeContent(data);
		return doc;
	}

	public Document buildEmbeddedXML(String envelopeXML, String insertXpathExpression) throws Exception {
		// build tmp doc object
		Document tmpdoc = build();
		// create new builder for wrapped document
		DocumentBuilder wrapBuilder = new DocumentBuilder()
				.withEnvBase64CheckMethod(this.base64CheckMethod)
				.withEnvBinaryCheckLooseMaxBytes(this.binaryCheckLooseMaxBytes)
				.withEnvBinaryCheckLooseMaxNonText(this.binaryCheckLooseMaxNonText)
				.withEnvBinaryCheckMethod(this.binaryCheckMethod)
				.withEnvCompressionLimitBytes(this.compressionLimitBytes)
				.withEnvDiskBaselocation(this.diskBaselocation)
				.withEnvDiskoverflowLimitBytes(this.diskoverflowLimitBytes)
				.withEnvInheritedCategorySuppressMask(this.inheritedCategorySuppressMask)
				.withEnvMaxReferenceNameLength(this.maxReferenceNameLength)
				.withEnvOverflowlocation(this.overflowlocation);
		// create merged document
		SimpleXPathMerge merger = new SimpleXPathMerge();
		List<SimpleXPathMerge.MergeReference> refs = new ArrayList<>();
		SimpleXPathMerge.MergeReference r1 = SimpleXPathMerge.MergeReference
				.createMergeReference()
				.asPCData()
				.asChildTag()
				.addRootTag()
				.withParentMergePath(insertXpathExpression)
				.withPayloadStream(tmpdoc.retrieveEmbeddableStream(), tmpdoc.getDocumentEncoding());
		refs.add(r1);
		try (Reader r = new StringReader(envelopeXML); Writer w = new OutputStreamWriter(wrapBuilder.getDeferredOutputStream())) {
			merger.merge(r, w, refs);
		}
		// create final embedded document instance
		Document embeddedDoc = wrapBuilder
				.withSourceDocument(this.sourceDocument)
				.withDocumentPropertySet(this.properties)
				.build();
		System.out.println(embeddedDoc.getProperties().asDocumentPropertySet());
		return embeddedDoc;
	}
}
