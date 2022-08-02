package com.ceva.eai.bwce.generic.cfw.utils.xmlio;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import com.ceva.eai.bwce.generic.cfw.utils.xmlio.in.SimpleImportHandler;
import com.ceva.eai.bwce.generic.cfw.utils.xmlio.in.SimpleImporter;
import com.ceva.eai.bwce.generic.cfw.utils.xmlio.in.SimplePath;
import com.ceva.eai.bwce.generic.cfw.utils.xmlio.out.XMLEncodeStream;
import com.ceva.eai.bwce.generic.cfw.utils.xmlio.out.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

public class SimpleXPathMerge {
	private boolean silent = false;
    private boolean useQNames = true;
    private boolean prettyPrint = true;

    public void setNamespaceAware(boolean aware) {
        useQNames = aware;
    }

    public boolean getNamespaceAware() {
        return useQNames;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean getSilent() {
        return silent;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    static final public class MergeReferences implements Serializable {

        private final static long serialVersionUID = 1L;
        private List<MergeReference> mergeReference = new ArrayList<>();

        public List<MergeReference> getMergeReference() {
			return mergeReference;
		}
        
        public void setMergeReference(List<MergeReference> mergeReference) {
			this.mergeReference = mergeReference;
		}
       
    }

    static final public class MergeReference implements Serializable {

        private final static long serialVersionUID = 1L;
        private String parentMergePath = "";
        private InputStream payloadStream;
        private String payload = "";
        private String payloadEncoding = "UTF-8";
        private boolean isFile = false;   
        private boolean isStream = false;
        private boolean replaceTag = false;
        private boolean skipRoot = false;
        private boolean isPCData = true;
        private boolean isCData = false;

        static public MergeReference createMergeReference() {
            return new MergeReference();
        }

        public MergeReference() {
        }

        public MergeReference withParentMergePath(String path) {
            this.parentMergePath = path;
            return this;
        }

        public MergeReference withPayload(String payload, boolean isReference) {
            this.payload = payload;
            this.isFile = isReference;
            this.isStream = false;
            return this;
        }
        
        public MergeReference withPayloadStream(InputStream stream, String encoding) {
            this.payloadStream = stream;
            this.payloadEncoding = encoding;
            this.isStream = true;            
            return this;
        }
        
        public MergeReference asPCData() {
            this.isPCData = true;
            this.isCData = false;
            return this;
        }

        public MergeReference asCData() {
            this.isCData = true;
            this.isPCData= false;
            return this;
        }

        public MergeReference addRootTag() {
            this.skipRoot = false;
            return this;
        }

        public MergeReference skipRootTag() {
            this.skipRoot = true;
            return this;
        }

        public MergeReference replaceTag() {
            this.replaceTag = true;
            return this;
        }

        public MergeReference asChildTag() {
            this.replaceTag = false;
            return this;
        }
        
        public String getParentMergePath() {
            return parentMergePath;
        }

        public String getPayload() {
            return payload;
        }

        private Reader getPayloadReader() throws Exception {
            Reader reader;
            if (isStream && this.payloadStream!=null) {
                reader = new BufferedReader(new InputStreamReader(payloadStream));
            } else if (isFile) {
                reader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(getPayload()), false), getPayloadEncoding()));
            } else {
                reader = new BufferedReader(new StringReader(getPayload()));
            }            
            return reader;
        }
                
        public boolean isSkipRoot() {
            return skipRoot;
        }

        public boolean isFile() {
            return isFile;
        }
        
        public boolean isCData() {
            return isCData;
        }
        
        public boolean isPCData() {
            return isPCData;
        }

        public boolean isReplaceTag() {
            return replaceTag;
        }

        public boolean isStream() {
            return isStream;
        }
                
        public String getPayloadEncoding() {
            return payloadEncoding;
        }
    }

    private class OutputHandler implements SimpleImportHandler {

        protected String currentData = "";
        protected XMLWriter writer = null;
        protected boolean tagIsOpen = false;
        protected boolean ignoreNextEnd = false;
        protected boolean ignoreRoot = false;

        public OutputHandler() {
        }

        public boolean isIgnoreRoot() {
            return ignoreRoot;
        }

        public void setIgnoreRoot(boolean ignoreRoot) {
            this.ignoreRoot = ignoreRoot;
        }

        public void setXMLWriter(XMLWriter out) {
            this.writer = out;
        }

        @Override
        public void startDocument() {
        }

        @Override
        public void endDocument() {
        }

        @Override
        public void cData(SimplePath path, String cdata) {
            currentData = cdata;
        }

        private boolean skipRoot(SimplePath path) {
            boolean result = false;
            if (path != null) {
                result = isIgnoreRoot() && SimplePath.stripEndingSlash(path.toString()).lastIndexOf("/") == 0;
            }
            return result;
        }

        @Override
        public void startElement(SimplePath path, String name, AttributesImpl attributes, String leadingCDdata) {
            if (writer != null && !skipRoot(path)) {
                try {
                    if (tagIsOpen) {
                        writer.writeNl();
                    }
                    writer.writeStartTag(XMLWriter.createStartTag(name, attributes, false), false);
                    if (leadingCDdata != null) {
                        writer.writeCData(leadingCDdata);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            tagIsOpen = true;
        }

        @Override
        public void endElement(SimplePath path, String name) {
            if (writer != null && !skipRoot(path) && ignoreNextEnd == false) {
                try {
                    if (tagIsOpen && currentData != null) {
                        writer.writeCData(currentData);
                    }
                    writer.writeEndTag(XMLWriter.createEndTag(name), true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            tagIsOpen = false;
            ignoreNextEnd = false;
            currentData = null;
        }
    }

    final private class MergeHandler extends OutputHandler {

        private Map<String, List<MergeReference>> mergeReferences = new HashMap<String, List<MergeReference>>();
        private Writer data = null;

        public MergeHandler(Writer output) {
            this.data = output;
        }

        public XMLWriter openWriter() throws Exception {
            if (writer == null) {                	
                writer = new XMLWriter(data);
                writer.setPrettyPrintMode(prettyPrint);
            }
            return writer;
        }

        public void closeWriter() throws Exception {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }

        public void setMergeReferences(List<MergeReference> refs) {
            for (Iterator<MergeReference> it = refs.iterator(); it.hasNext();) {
                MergeReference mref = it.next();
                if (mergeReferences.containsKey(mref.parentMergePath)) {
                    mergeReferences.get(mref.parentMergePath).add(mref);
                } else {
                    List<MergeReference> mrefs = new ArrayList<MergeReference>();
                    mrefs.add(mref);
                    mergeReferences.put(mref.parentMergePath, mrefs);
                }
            }
        }

        @SuppressWarnings("unused")
		public void setMergeReference(MergeReferences refs) {
            if (refs != null) {
                for (MergeReference mref : refs.getMergeReference()) {
                    if (mergeReferences.containsKey(mref.parentMergePath)) {
                        mergeReferences.get(mref.parentMergePath).add(mref);
                    } else {
                        List<MergeReference> mrefs = new ArrayList<MergeReference>();
                        mrefs.add(mref);
                        mergeReferences.put(mref.parentMergePath, mrefs);
                    }
                }
            }
        }

        @Override
        public void startDocument() {
            try {
                openWriter().writeXMLDeclaration();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void endDocument() {
            try {
                closeWriter();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void startElement(SimplePath path, String name, AttributesImpl attributes, String leadingCDdata) {
            try {
                String matchString = SimplePath.stripEndingSlash(path.toString());
                if (this.mergeReferences.containsKey(matchString)) {
                    List<MergeReference> refs = this.mergeReferences.get(matchString);
                    for (Iterator<MergeReference> it = refs.iterator(); it.hasNext();) {
                        MergeReference mref = it.next();
                        if (!mref.isReplaceTag()) {
                            super.startElement(path, name, attributes, leadingCDdata);
                        }
                        if (mref.isCData) {
                            insertCDATA(mref);
                        } else if (mref.isPCData) {
                            insertPCDATA(mref);
                        } else {
                            insertXML(mref);
                        }
                        if (!mref.isReplaceTag()) {
                            super.endElement(path, name);
                        }
                    }
                    this.ignoreNextEnd = true;
                } else {
                    super.startElement(path, name, attributes, leadingCDdata);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        
        private void insertPCDATA(MergeReference ref) throws Exception {
            Reader reader = ref.getPayloadReader();
            try {
                XMLEncodeStream.xmlEncodeReaderAsPCDATA(reader, writer, false);
            } finally {
                reader.close();
            }
        }

        private void insertCDATA(MergeReference ref) throws Exception {
            Reader reader = ref.getPayloadReader();
            try {
                writer.write("<![CDATA[");
                IOUtils.copyLarge(reader, writer);
                writer.write("]]>");
            } finally {
                reader.close();
            }
        }

        private void insertXML(MergeReference ref) throws Exception {
            SimpleImporter importer = new SimpleImporter();
            importer.setIncludeLeadingCDataIntoStartElementCallback(false);
            importer.setTrimContent(true);
            importer.setZeroLengthIsNull(true);
            importer.setUseQName(true);
            importer.setMakeCopy(false);
            importer.setFullDebugMode(false);
            importer.setValidating(false);
            importer.setLoadExternal(false);
            importer.setFailOnErr(!silent);
            importer.setFailOnFatal(!silent);
            importer.setFailOnWarn(false);
            importer.setUseQName(useQNames);

            OutputHandler outHandler = new OutputHandler();
            outHandler.setXMLWriter(writer);
            outHandler.setIgnoreRoot(ref.isSkipRoot());
            Reader reader = ref.getPayloadReader();
            try {
                importer.addSimpleImportHandler(outHandler);
                importer.parse(new InputSource(reader));
            } finally {
                reader.close();
            }
        }

        @Override
        public void endElement(SimplePath path, String name) {
            super.endElement(path, name);
        }
    }


    public void merge(Reader reader, Writer writer, List<MergeReference> mergeReferences) throws Exception {
        SimpleImporter importer = new SimpleImporter();        
        MergeHandler mergeHandler = new MergeHandler(writer);
        try {
            mergeHandler.setMergeReferences(mergeReferences);
            importer.addSimpleImportHandler(mergeHandler);
            importer.setIncludeLeadingCDataIntoStartElementCallback(false);
            importer.setTrimContent(true);
            importer.setZeroLengthIsNull(true);
            importer.setUseQName(true);
            importer.setMakeCopy(false);
            importer.setFullDebugMode(false);
            importer.setValidating(false);
            importer.setLoadExternal(false);
            importer.setFailOnErr(!silent);
            importer.setFailOnFatal(!silent);
            importer.setFailOnWarn(false);
            importer.setUseQName(useQNames);
            importer.parse(new InputSource(reader));
        } catch (Exception e) {
            if (!silent) {
                throw e;
            }
        }
    }

}
