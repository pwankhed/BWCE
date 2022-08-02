package com.ceva.eai.bwce.generic.cfw.utils.xmlio.in;

import java.io.*;
import java.util.*;
import java.net.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

/**
 * <b>Simple</b> and <b>fast</b> importer for XML configuration or import files. <br>
 * <br>
 * It is based on SAX and can be considered an extension to it. This means it is
 * callback oriented and does not build an internal data structure like the DOM.
 * While SAX is simple, fast, and memory friendly it might be a bit too 
 * rudimentary for most tasks. <code>SimpleImporter</code> adds more high level
 * means for importing XML while preserving the SAX's benefits. <br>
 * <br>
 * As with SAX you register a callback handler ({@link SimpleImportHandler})
 * that is called upon events. Consider the following example implementation
 * of a {@link SimpleImportHandler}:<br><br>
 * <code><pre>
 * public class DemoHandler implements SimpleImportHandler { 
 * public void startDocument() { }
 * public void endDocument() { }
 * 
 * public void cData(SimplePath path, String cdata) { }
 * 
 * public void startElement(SimplePath path, String name, AttributesImpl attributes, String leadingCDdata) {
 * &nbsp;&nbsp;if (path.matches("/root/interesting-element")) {
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println(leadingCDdata);
 * &nbsp;&nbsp;}
 * }
 * public void endElement(SimplePath path, String name) { }
 * 
 * }
 * </pre></code>
 * 
 * Registering this class with {@link #addSimpleImportHandler} and call
 * {@link #parse} on an input stream or {@link #parseUrlOrFile} will dump 
 * the leading text of the element matching the path ({@link SimplePath}) 
 * "/root/interesting-element".<br>
 * <br>
 * <em>Note</em>: This class is thread safe.
 *
 */
public class SimpleImporter {

    // properties
    private boolean trimContent = true;
    private boolean makeCopy = false;
    private boolean zeroLengthIsNull = true;
    private boolean includeLeadingCDataIntoStartElementCallback = true;
    private boolean fullDebug = false;
    private boolean useQName = true;
    private boolean validating = true;
    private boolean loadExternal = true;
    private boolean failOnError = true;
    private boolean failOnWarn = false;
    private boolean failOnFatal = true;
    private boolean buildComplexPath = false;

    protected SAXParserFactory factory;

    protected List<SimpleImportHandler> callbackHandlerList = new ArrayList<>();

    // internal state
    protected StringBuffer currentMixedPCData = null;
    protected boolean foundMixedPCData = false;
    // the first (leading) CDATA is exacly the part between a start tag
    // and any other tagging
    protected StringBuffer firstPCData = null;
    protected boolean isFirstPCData = true;

    // remember start element for later flushing
    protected ParseElement currentElement = null;

    protected PathStack parseStack = new PathStack();

    protected String debugBuffer = null;

    /** Creates a new SimpleImporter object having default property settings. It is recommended
     * to set all properties explicitly for clearity.
     */
    public SimpleImporter() {
        factory = SAXParserFactory.newInstance();
    }

    /** Determines if we have found any mixed content while parsing. */
    public boolean getFoundMixedPCData() {
        return foundMixedPCData;
    }

    /**
     * Determines if the path shall be assembled of the full qualified names. <code>true</code> is the default.
     */
    public boolean getUseQName() {
        return useQName;
    }

    /**
     * Sets if the path shall be assembled of the full qualified names. <code>true</code> is the default.
     */
    public void setUseQName(boolean useQName) {
        this.useQName = useQName;
    }
    
        /**
     * Determines parser will be validating. <code>true</code> is the default.
     */
    public boolean getValidating() {
        return validating;
    }

    /**
     * Sets if the will be validating. <code>true</code> is the default.
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

      /**
     * Determines is external DTD will be loaded. <code>true</code> is the default.
     */
    public boolean getLoadExternal() {
        return loadExternal;
    }

    /**
     * Sets if the will be validating. <code>true</code> is the default.
     */
    public void setLoadExternal(boolean loadexternal) {
        this.loadExternal = loadexternal;
    }    
    
      /**
     * Determines parsing should fail on warning. <code>true</code> is the default.
     */
    public boolean getFailOnWarn() {
        return failOnWarn;
    }

    /**
     * Sets parsing will fail on warning. <code>true</code> is the default.
     */
    public void setFailOnWarn (boolean failonwarn) {
        this.failOnWarn = failonwarn;
    }     
     /**
     * Determines parsing should fail on error. <code>true</code> is the default.
     */
    public boolean getFailOnErr() {
        return failOnError;
    }

    /**
     * Sets parsing will fail on error. <code>true</code> is the default.
     */
    public void setFailOnErr(boolean failonerr) {
        this.failOnError = failonerr;
    }     
    
     /**
     * Determines parsing should fail on fatal. <code>true</code> is the default.
     */
    public boolean getFailOnFatal() {
        return failOnFatal;
    }

    /**
     * Sets parsing will fail on fatal. <code>true</code> is the default.
     */
    public void setFailOnFatal(boolean failonfatal) {
        this.failOnFatal = failonfatal;
    }     
    
    /**
     * Determines if the simple path created will have complex additional info.  
     */
    public boolean getBuildComplexPath() {
        return buildComplexPath;
    }

    /**
     * Sets if the simple path created will have complex additional info.  
     */
    public void setBuildComplexPath(boolean buildComplexPath) {
        this.buildComplexPath = buildComplexPath;
    }

    /** Sets the full debug mode which enables us to get the parsed stream
     * as string via the {@link #getParsedStreamForDebug()}
     * method even if an error occured.
     */
    public void setFullDebugMode(boolean fullDebug) {
        this.fullDebug = fullDebug;
    }

    /** Gets the property described in
     * {@link #setFullDebugMode}.
     */
    public boolean getFullDebugMode() {
        return fullDebug;
    }

    /** Gets the whole stream parsed in the {@link #parse} method. As this requires some actions 
     * significantly slowing down the whole parse, this only works if it has been enabled 
     * by the the {@link #setFullDebugMode} method. 
     */
    public String getParsedStreamForDebug() {
        if (!getFullDebugMode()) {
            return null;
        } else {
            return debugBuffer;
        }
    }

    /** Gets property telling importer to return any leading CDATA, i.e.
     * CDATA directly following a start tag before any other tagging,
     * along with the start element
     * method. If set to false leading CDATA will be returned using method
     * {@link SimpleImportHandler#cData} just like any CDATA in a mixed
     * content. <br>
     *
     * @see SimpleImportHandler#startElement
     * @see #setIncludeLeadingCDataIntoStartElementCallback
     */
    public boolean getIncludeLeadingCDataIntoStartElementCallback() {
        return includeLeadingCDataIntoStartElementCallback;
    }

    /** Sets the property described in
     * {@link #getIncludeLeadingCDataIntoStartElementCallback}.
     */
    public void setIncludeLeadingCDataIntoStartElementCallback(boolean includeLeadingCDataIntoStartElementCallback) {
        this.includeLeadingCDataIntoStartElementCallback = includeLeadingCDataIntoStartElementCallback;
    }

    /** Sets the property described in
     * {@link #setTrimContent}.
     */
    public boolean getTrimContent() {
        return trimContent;
    }

    /** Sets when all content shall be trimed. 
     * If set in conjunction with {@link #setZeroLengthIsNull} all whitespace data will not be
     * reported to callback handlers. 
     */
    public void setTrimContent(boolean trimContent) {
        this.trimContent = trimContent;
    }

    /** Gets property: When findind zero length content should it be treated as null data? 
     * If it is treated as null data nothing is reported to handlers when finding zero length data. 
     */
    public boolean getZeroLengthIsNull() {
        return zeroLengthIsNull;
    }

    /** Sets the property described in
     * {@link #getZeroLengthIsNull}.
     */
    public void setZeroLengthIsNull(boolean zeroLengthIsNull) {
        this.zeroLengthIsNull = zeroLengthIsNull;
    }

    /** Gets the property describing if every callback handler gets a fresh copy of the parsed data. 
     * This is only important when there is more than one callback handler. If so and it is not set,
     * all handlers will get <em>identical</em> objects. This is bad if you expect them to change any
     * of that data.
     */
    public boolean getMakeCopy() {
        return makeCopy;
    }

    /** Sets the property described in {@link #getMakeCopy}. */
    public void setMakeCopy(boolean makeCopy) {
        this.makeCopy = makeCopy;
    }

    /** Adds a new callback handler if it is not in the callback list, yet. 
     * This can be dynamically done while parsing. 
     * @see #removeSimpleImportHandler
     */
    public void addSimpleImportHandler(SimpleImportHandler callbackHandler) {
        synchronized (callbackHandlerList) {
            if (!callbackHandlerList.contains(callbackHandler)) {
                callbackHandlerList.add(callbackHandler);
            }
        }
    }

    /** Removes a callback handler if it is in the callback list. 
     * This can be dynamically done while parsing. 
     * @see #addSimpleImportHandler
     */
    public void removeSimpleImportHandler(SimpleImportHandler callbackHandler) {
        synchronized (callbackHandlerList) {
            callbackHandlerList.remove(callbackHandler);
        }
    }

    /** Tries to parse the file or URL named by parameter <code>urlOrFileName</code>. 
     * First it tries to parse it as URL, if this does not work, it tries to parse it as file. 
     * If one option works, an input stream will be opened and {@link #parse} will be called with it.
     * If both does not work, an exception is thrown.
     * 
     * @see #parse
     */
    public synchronized void parseUrlOrFile(String urlOrFileName)
        throws ParserConfigurationException, SAXException, IOException, SimpleImporterException {
        Throwable urlException = null;
        Throwable fileException = null;
        InputStream in = null;
        try {
            URL url = new URL(urlOrFileName);
            URLConnection urlConnection = url.openConnection();
            in = urlConnection.getInputStream();
        } catch (MalformedURLException mue) {
            urlException = mue;
        } catch (IOException ioe) {
            urlException = ioe;
        }

        try {
            in = new FileInputStream(urlOrFileName);
        } catch (IOException ioe) {
            fileException = ioe;
        }

        if (in != null) {
            parse(new InputSource(new BufferedInputStream(in)));
        } else {
            throw new SimpleImporterException(
                "Could not parse "
                    + urlOrFileName
                    + ", is neither URL ("
                    + urlException.getMessage()
                    + ") nor file ("
                    + fileException.getMessage()
                    + ").");
        }
    }

    /** Parses the input source using the standard SAX parser and calls back the callback handlers.
     * If enabled with {@link #setFullDebugMode} the source will be verbosely copied first.<br>
     *<br>
     * <em>Note</em>: This method is synchronized, so you can not have two concurrent parses.
     */
    public synchronized void parse(InputSource is) throws ParserConfigurationException, SAXException, IOException {
        firstPCData = null;
        currentElement = null;
        factory.setNamespaceAware(!useQName || buildComplexPath);
        factory.setValidating(validating);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",loadExternal);
        SAXParser parser = factory.newSAXParser();
        if (getFullDebugMode()) {
            InputSource preReadIn = bufferParserStream(is);
            parser.parse(preReadIn, new SAXHandler(failOnWarn,failOnError, failOnFatal));
        } else {
            parser.parse(is, new SAXHandler(failOnWarn,failOnError, failOnFatal));
        }
    }

    private InputSource bufferParserStream(InputSource is) throws IOException {
        StringBuffer buf = new StringBuffer();
        @SuppressWarnings("resource")
		Reader reader;
        BufferedReader bufferedReader;
        if (is.getCharacterStream() != null) {
            reader = is.getCharacterStream();
        } else {
            String encoding = is.getEncoding();
            if (encoding != null) {
                reader = new InputStreamReader(is.getByteStream(), encoding);
            } else {
                reader = new InputStreamReader(is.getByteStream());
            }
        }
        if (reader instanceof BufferedReader) {
            bufferedReader = (BufferedReader) reader;
        } else {
            bufferedReader = new BufferedReader(reader);
        }

        while (true) {
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            } else {
                buf.append(line).append('\n');
            }
        }
        debugBuffer = buf.toString();
        return new InputSource(new StringReader(debugBuffer));
    }

    // callback handlers with start element method when there is data
    private void callBackStartElementWhenReady() {
        if (currentElement != null) {
            String content = getFirstPCData();
            SimplePath path;
            if (buildComplexPath) {
                path =
                    new SimplePath(
                        currentElement.path,
                        (Item[]) currentElement.pathList.toArray(new Item[currentElement.pathList.size()]));
            } else {
                path = new SimplePath(currentElement.path);

            }

            synchronized (callbackHandlerList) {
                for (Iterator<SimpleImportHandler> it = callbackHandlerList.iterator(); it.hasNext();) {
                    SimpleImportHandler callbackHandler = (SimpleImportHandler) it.next();
                    if (getMakeCopy()) {
                        // string is constant any way, no need to make a copy
                        callbackHandler.startElement(
                            new SimplePath(path),
                            currentElement.name,
                            new AttributesImpl(currentElement.attributes),
                            content);
                    } else {
                        callbackHandler.startElement(path, currentElement.name, currentElement.attributes, content);
                    }
                }
            }

            firstPCData = null;
            currentElement = null;
        }
    }

    private void sendCharacters(String text) {
        if (text == null)
            return;

        if (isFirstPCData) {
            if (includeLeadingCDataIntoStartElementCallback) {
                addToFirstPCData(text);
            } else {
                sendCData(text);
            }
        } else {
            foundMixedPCData = true;
            sendCData(text);
        }
    }

    private void callBackCDATAWhenReady() {
        callBackStartElementWhenReady();
        if (currentMixedPCData == null) {
            return;
        }
        String text = currentMixedPCData.toString();
        text = trimPCData(text);
        if (text == null) {
            return;
        }

        SimplePath path;
        if (buildComplexPath) {
            path =
                new SimplePath(
                    parseStack.getPath(),
                    (Item[]) parseStack.getPathList().toArray(new Item[parseStack.getPathList().size()]));
        } else {
            path = new SimplePath(parseStack.getPath());

        }
        
        synchronized (callbackHandlerList) {
            for (Iterator<SimpleImportHandler> it = callbackHandlerList.iterator(); it.hasNext();) {
                SimpleImportHandler callbackHandler = (SimpleImportHandler) it.next();
                if (getMakeCopy()) {
                    // string is constant any way, no need to make a copy
                    callbackHandler.cData(new SimplePath(path), text);
                } else {
                    callbackHandler.cData(path, text);
                }
            }
        }
        currentMixedPCData = null;
    }

    // send normal (not leading) CDATA to handlers
    private void sendCData(String text) {
        // defer sending it until we have a maximum chunck, i.e. until
        // next tagging occurs
        if (currentMixedPCData == null) {
            currentMixedPCData = new StringBuffer(text.length());
        }
        currentMixedPCData.append(text);
    }

    private void addToFirstPCData(String text) {
        if (firstPCData == null) {
            firstPCData = new StringBuffer(text.length());
        }
        firstPCData.append(text);
    }

    private String getFirstPCData() {
        if (firstPCData == null) {
            return null;
        } else {
            String text = firstPCData.toString();
            return trimPCData(text);
        }
    }

    // trim text depending on settings of properties
    private String trimPCData(String pcData) {
        if (pcData == null) {
            return null;
        } else {
            if (getTrimContent()) {
                pcData = pcData.trim();
            }
            if (pcData.length() == 0 && getZeroLengthIsNull()) {
                return null;
            } else {
                return pcData;
            }
        }
    }

    // use to temporarily save a an element
    private final static class ParseElement {
        public String name, path;
        public List<Object> pathList; 
        public AttributesImpl attributes;

        public ParseElement(String name, String path, List<Object> pathList, AttributesImpl attributes) {
            this.name = name;
            this.path = path;
            this.attributes = attributes;
            this.pathList = pathList;
        }
    }

    
    private final class SAXHandler extends DefaultHandler {
        private boolean failOnWarn = false;
        private boolean failOnErr = false;
        private boolean failOnFatal= true;
        
        public SAXHandler (boolean failOnWarn, boolean failOnErr, boolean failOnFatal) {
            this.failOnWarn = failOnWarn;
            this.failOnErr = failOnErr;
            this.failOnFatal = failOnFatal;
            
        }
        public void warning(SAXParseException exception) throws SAXException {
            if (failOnWarn) {
                throw exception;
            }
            
        }

        public void error(SAXParseException exception) throws SAXException {
            if (failOnErr) {
                throw exception;
            }
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            if (failOnFatal) {
                throw exception;
            }
        }

        public void startDocument() {
            synchronized (callbackHandlerList) {
                for (Iterator<SimpleImportHandler> it = callbackHandlerList.iterator(); it.hasNext();) {
                    SimpleImportHandler callbackHandler = (SimpleImportHandler) it.next();
                    callbackHandler.startDocument();
                }
            }
        }

        public void endDocument() {
            // flush any pending start elements and character data, as now the show is over
            callBackStartElementWhenReady();
            callBackCDATAWhenReady();
            synchronized (callbackHandlerList) {
                for (Iterator<SimpleImportHandler> it = callbackHandlerList.iterator(); it.hasNext();) {
                    SimpleImportHandler callbackHandler = (SimpleImportHandler) it.next();
                    callbackHandler.endDocument();
                }
            }
        }

        public void characters(char ch[], int start, int length) {
            if (length < 1)
                return;
            String text = new String(ch, start, length);
            sendCharacters(text);
        }

        public void endElement(String namespaceURI, String localName, String qName) {
            // be sure to have any pending start elements and character data flushed before
            // sending end tag to keep right sequence of callbacks
            callBackStartElementWhenReady();
            callBackCDATAWhenReady();
            String name;
            if (!useQName || qName == null || qName.length() == 0) {
                name = localName;
            } else {
                name = qName;
            }

            SimplePath path;
            if (buildComplexPath) {
                path =
                    new SimplePath(
                        parseStack.getPath(),
                        (Item[]) parseStack.getPathList().toArray(new Item[parseStack.getPathList().size()]));
            } else {
                path = new SimplePath(parseStack.getPath());

            }

            synchronized (callbackHandlerList) {
                for (Iterator<SimpleImportHandler> it = callbackHandlerList.iterator(); it.hasNext();) {
                    SimpleImportHandler callbackHandler = (SimpleImportHandler) it.next();
                    if (getMakeCopy()) {
                        // string is constant any way, no need to make a copy
                        callbackHandler.endElement(new SimplePath(path), name);
                    } else {
                        callbackHandler.endElement(path, name);
                    }
                }
            }

            // this must never be
            if (parseStack.empty()) {
                throw new SimpleImporterException("Umatchted end tag: " + name);
            } else {
                Object top = parseStack.peek();
                String topName;
                if (buildComplexPath) {
                    topName = ((Item)top).getName();
                } else {
                    topName = (String)top;
                }
                if (!name.equals(topName)) {
                    throw new SimpleImporterException(
                        "End tag " + name + " does not match start tag " + top);
                } else {
                    parseStack.pop();
                }
            }
            // any CDATA following can't be leading
            isFirstPCData = false;
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            // be sure to have any pending start elements and character data flushed before
            // opening a new one to keep right sequence of callbacks
            callBackStartElementWhenReady();
            callBackCDATAWhenReady();
            String name;
            if (!useQName || qName == null || qName.length() == 0) {
                name = localName;
            } else {
                name = qName;
            }
            parseStack.push(namespaceURI, name);
            // Defer callback to handlers as it is not clear now how
            // much (if any) CDATA has to be passed over with start element method.
            AttributesImpl attributesCopy = new AttributesImpl(atts);
            currentElement = new ParseElement(name, parseStack.getPath(), parseStack.getPathList(), attributesCopy);
            // Any CDATA (can be more the one SAX event) following is leading
            // until next tag. Actually it is sufficient to switch this off
            // in end tag not in start tag, as it would be turned on again
            // immediately.
            isFirstPCData = true;
        }
    }

    // Notion of a stack representing a path.
    private final class PathStack {

        private List<Object> pathStack;

        @SuppressWarnings("unused")
		public PathStack(int initialCapacity) {
            pathStack = new ArrayList<>(initialCapacity);
        }

        public PathStack() {
            pathStack = new ArrayList<>();
        }

        public String getPath() {
            StringBuilder path = new StringBuilder(100);
            // this is always there as root
            path.append('/');
            for (Iterator<Object> it = pathStack.iterator(); it.hasNext();) {
                Object element = it.next();
                String pathElement;
                if (buildComplexPath) {
                    pathElement = ((Item) element).getName();
                } else {
                    pathElement = (String) element;
                }
                path.append(pathElement).append('/');
            }
            return path.toString();
        }

        public List<Object> getPathList() {
            return pathStack;
        }

        public String toString() {
            return getPath();
        }

        public void push(String namespaceURI, String name) {
            if (buildComplexPath) {
                pathStack.add(new Item(name, namespaceURI));
            } else {
                pathStack.add(name);
            }
        }

        @SuppressWarnings("unused")
		public int size() {
            return pathStack.size();
        }

        public boolean empty() {
            return (pathStack.size() <= 0);
        }

        public Object peek() {
            int size = pathStack.size();
            if (size > 0) {
                return pathStack.get(size - 1);
            } else {
                return null;
            }
        }

        public Object pop() {
            int size = pathStack.size();
            if (size > 0) {
                Object o = pathStack.get(size - 1);
                pathStack.remove(size - 1);
                return o;
            } else {
                return null;
            }
        }

    }
}
