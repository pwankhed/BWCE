package com.ceva.eai.bwce.generic.cfw.utils.xmlio.out;

import java.io.*;

import org.xml.sax.Attributes;

/**
 * {@link FilterWriter} adding formatted and encoded XML export 
 * functionality to the underlying writer. Formatting and
 * encoding is done as straight forward as possible. <br>
 * Everything you know better than this class must be done by you, e.g. you will
 * have to tell <code>XMLWriter</code> where you wish to have
 * newlines.In effect, no unexpected so called
 * <em>intelligent</em> behaviour is to be feared. Another effect is high speed.
 * <br>
 * <br>
 * A simple example: Suppose your <code>XMLWriter</code> object is xmlWriter.
 * The following sequence of code <br><br>
 * <code>
 * &nbsp;&nbsp;xmlWriter.writeStartTag("&lt;root>");<br>
 * &nbsp;&nbsp;xmlWriter.writeStartTag("&lt;next1>", false);<br>
 * &nbsp;&nbsp;xmlWriter.writeEmptyTag("&lt;emptyTag/>", false);<br>
 * &nbsp;&nbsp;xmlWriter.writeEndTag("&lt;/next1>");<br>
 * &nbsp;&nbsp;xmlWriter.writeStartTag("&lt;/root>");<br>
 * </code>
 * <br>
 * will write this to the underlying writer<br><br>
 * <code>
 * &lt;root><br>
 * &nbsp;&nbsp;&lt;next1>&lt;emptyTag/>&lt;/next1><br>
 * &lt;/root><br>
 *</code>
 * <br>
 * <br>
 * <em>Caution</em>: Do not forget to call {@link #flush} at the end of your
 * exporting process as otherwise no data might be written.
 *
 */
public class XMLWriter extends FilterWriter {

    public final static boolean NEWLINE = true;
    public final static boolean NO_NEWLINE = false;

    protected int tabWidth = 2;

    /** Current depth of the tree. Do not know what this is good for, but
     * who knows...
     */
    protected int depth = 0;

    /** Current indentation. Depth does not contain sufficient information as 
     * tabWidth may change during output (should not).
     */
    protected int indent = 0;

    protected boolean prettyPrintMode = true;

    protected boolean nlAfterEmptyTag = true;
    protected boolean nlAfterStartTag = true;
    protected boolean nlAfterEndTag = true;

    /** Flag indicating if the XML declaration has already been writter.
     * Check this using {@link #isXMLDeclarationWritten()}. 
     * It might be useful to 
     * avoid writing twice or more times in different contexts writing
     * to same writer. 
     * <br>
     * <em>Caution</em>: If you subclass, be sure to set this in
     * {@link #writeXMLDeclaration()}.
     */
    protected boolean xmlDeclWritten = false;

    private boolean needsIndent = false;
    private boolean indentStringCacheValid = true;
    private String indentStringCache = "";

    /** Convenience method for creating an end tag.
     * @param tagName name of the end tag
     */
    public final static String createEndTag(String tagName) {
        return "</" + tagName + ">";
    }

    /** Convenience method for creating a start tag having no attributes.
     * @param tagName name of the start tag
     */
    public final static String createStartTag(String tagName) {
        return "<" + tagName + ">";
    }

    /** Convenience method for creating an <em>empty</em> tag 
     * having no attributes. E.g. <code>&lt;tagName/></code>. 
     * @param tagName name of the tag
     */
    public final static String createEmptyTag(String tagName) {
        return "<" + tagName + "/>";
    }

    /** Convenience method for creating a start tag.
     * @param tagName name of the start tag
     * @param attrNames names of attributes to be included into start tag
     * @param attrValues values of attributes to be included into start tag -
     * there should be just as many entries as in <code>attrNames</code>,
     * if a value is <code>null</code> corresponding attribute will not be included
     * @param isEmpty decides wheter this is start tag is for an empty element
     */
    public final static String createStartTag(
        String tagName,
        String[] attrNames,
        String[] attrValues,
        boolean isEmpty) {
        return createStartTag(tagName, attrNames, attrValues, isEmpty, true, '"');
    }

    /** Convenience method for creating a <em>non empty</em> start tag.
     * @param tagName name of the start tag
     * @param attrNames names of attributes to be included into start tag
     * @param attrValues values of attributes to be included into start tag -
     * there should be just as many entries as in <code>attrNames</code>,
     * if a value is <code>null</code> corresponding attribute will not be included
     */
    public final static String createStartTag(String tagName, String[] attrNames, String[] attrValues) {
        return createStartTag(tagName, attrNames, attrValues, false);
    }

    /** Convenience method for creating an <em>empty</em> tag.
     * @param tagName name of the tag
     * @param attrNames names of attributes to be included into tag
     * @param attrValues values of attributes to be included into tag -
     * there should be just as many entries as in <code>attrNames</code>,
     * if a value is <code>null</code> corresponding attribute will not be included
     * @see #createEmptyTag(String)
     */
    public final static String createEmptyTag(String tagName, String[] attrNames, String[] attrValues) {
        return createStartTag(tagName, attrNames, attrValues, true);
    }

    /** Convenience method for creating a start tag.
     * @param tagName name of the start tag
     * @param attrName name of attribute to be included into start tag
     * @param attrValue value of attribute to be included into start tag,
     * if attrValue is <code>null</code> attribute will not be included
     * @param isEmpty decides whether this is start tag is for an empty element
     */
    public final static String createStartTag(String tagName, String attrName, String attrValue, boolean isEmpty) {
        return createStartTag(tagName, new String[] { attrName }, new String[] { attrValue }, isEmpty);
    }

    /** Convenience method for creating a <em>non empty</em> start tag.
     * @param tagName name of the start tag
     * @param attrName name of attribute to be included into start tag
     * @param attrValue value of attribute to be included into start tag,
     * if attrValue is <code>null</code> attribute will not be included
     */
    public final static String createStartTag(String tagName, String attrName, String attrValue) {
        return createStartTag(tagName, attrName, attrValue, false);
    }

    /** Convenience method for creating an <em>empty</em> tag.
     * @param tagName name of the tag
     * @param attrName name of attribute to be included into tag
     * @param attrValue value of attribute to be included into tag,
     * if attrValue is <code>null</code> attribute will not be included
     * @see #createEmptyTag(String)
     */
    public final static String createEmptyTag(String tagName, String attrName, String attrValue) {
        return createStartTag(tagName, attrName, attrValue, true);
    }

    /** Convenience method for creating a start tag.
     * @param tagName name of the start tag
     * @param attrNames names of attributes to be included into start tag
     * @param attrValues values of attributes to be included into start tag -
     * there should be just as many entries as in <code>attrNames</code>,
     * if a value is <code>null</code> corresponding attribute will not be included
     * @param isEmpty decides whether this is start tag is for an empty element
     * @param encodeAttrs set this to have your attribute values encoded for XML
     * @param quoteChar if you choose encoding this is the char that quotes
     * your attributes
     */
    public final static String createStartTag(
        String tagName,
        String[] attrNames,
        String[] attrValues,
        boolean isEmpty,
        boolean encodeAttrs,
        char quoteChar) {
        // estimate buffer size
        StringBuffer buf = new StringBuffer((attrNames.length + 1) * 15);
        buf.append('<').append(tagName);

        if (attrNames.length != 0 && (attrNames.length <= attrValues.length)) {
            for (int i = 0; i < attrNames.length; i++) {
                String name = attrNames[i];
                String value = attrValues[i];
                if (value == null)
                    continue;
                if (encodeAttrs)
                    value = XMLEncode.xmlEncodeTextForAttribute(value, quoteChar);
                buf.append(' ').append(name).append('=').append(value);
            }
        }

        if (isEmpty) {
            buf.append("/>");
        } else {
            buf.append('>');
        }
        return buf.toString();
    }

    /** Convenience method for creating a start tag.
     * @param tagName name of the start tag
     * @param attrPairs name/value pairs of attributes to be included into start tag -
     * if a value is <code>null</code> corresponding attribute will not be included
     * @param isEmpty decides whether this is start tag is for an empty element
     */
    public final static String createStartTag(String tagName, String[][] attrPairs, boolean isEmpty) {
        return createStartTag(tagName, attrPairs, isEmpty, true, '"');
    }

    /** Convenience method for creating a <em>non empty</em> start tag.
     * @param tagName name of the start tag
     * @param attrPairs name/value pairs of attributes to be included into start tag -
     * if a value is <code>null</code> corresponding attribute will not be included
     */
    public final static String createStartTag(String tagName, String[][] attrPairs) {
        return createStartTag(tagName, attrPairs, false);
    }

    /** Convenience method for creating an <em>empty</em> tag.
     * @param tagName name of the tag
     * @param attrPairs name/value pairs of attributes to be included into tag -
     * if a value is <code>null</code> corresponding attribute will not be included
     * @see #createEmptyTag(String)
     */
    public final static String createEmptyTag(String tagName, String[][] attrPairs) {
        return createStartTag(tagName, attrPairs, true);
    }

    /** Convenience method for creating a start tag.
     * @param tagName name of the start tag
     * @param attrPairs name/value pairs of attributes to be included into start tag -
     * if a value is <code>null</code> corresponding attribute will not be included
     * @param isEmpty decides whether this is start tag is for an empty element
     * @param encodeAttrs set this to have your attribute values encoded for XML
     * @param quoteChar if you choose encoding this is the char that quotes
     * your attributes
     */
    public final static String createStartTag(
        String tagName,
        String[][] attrPairs,
        boolean isEmpty,
        boolean encodeAttrs,
        char quoteChar) {
        // estimate buffer size
        StringBuffer buf = new StringBuffer((attrPairs.length + 1) * 15);
        buf.append('<').append(tagName);

        for (int i = 0; i < attrPairs.length; i++) {
            String name = attrPairs[i][0];
            String value = attrPairs[i][1];
            if (value == null)
                continue;
            if (encodeAttrs)
                value = XMLEncode.xmlEncodeTextForAttribute(value, quoteChar);
            buf.append(' ').append(name).append('=').append(value);
        }

        if (isEmpty) {
            buf.append("/>");
        } else {
            buf.append('>');
        }
        return buf.toString();
    }

    /** Convenience method for creating an <em>empty</em> tag.
     * @param tagName name of the tag
     * @param attributes SAX attributes to be included into start tag
     * @see #createEmptyTag(String)
     */
    public final static String createEmptyTag(String tagName, Attributes attributes) {
        return createStartTag(tagName, attributes, true);
    }

    /** Convenience method for creating a start tag.
     * @param tagName name of the start tag
     * @param attributes SAX attributes to be included into start tag
     */
    public final static String createStartTag(String tagName, Attributes attributes) {
        return createStartTag(tagName, attributes, false);
    }

    /** Convenience method for creating a start tag.
     * @param tagName name of the start tag
     * @param attributes SAX attributes to be included into start tag
     * @param isEmpty decides whether this is start tag is for an empty element
     */
    public final static String createStartTag(String tagName, Attributes attributes, boolean isEmpty) {
        return createStartTag(tagName, attributes, isEmpty, true, '"');
    }

    /** Convenience method for creating a start tag.
     * @param tagName name of the start tag
     * @param attributes SAX attributes to be included into start tag
     * @param isEmpty decides whether this is start tag is for an empty element
     * @param encodeAttrs set this to have your attribute values encoded for XML
     * @param quoteChar if you choose encoding this is the char that quotes
     * your attributes
     */
    public final static String createStartTag(
        String tagName,
        Attributes attributes,
        boolean isEmpty,
        boolean encodeAttrs,
        char quoteChar) {
        // estimate buffer size
        StringBuffer buf = new StringBuffer((attributes.getLength() + 1) * 15);
        buf.append('<').append(tagName);

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(i);
            if (encodeAttrs)
                value = XMLEncode.xmlEncodeTextForAttribute(value, quoteChar);
            buf.append(' ').append(name).append('=').append(value);
        }

        if (isEmpty) {
            buf.append("/>");
        } else {
            buf.append('>');
        }
        return buf.toString();
    }

    /** Convenience method for creating <em>and writing</em> a whole element. 
     * Added to normal non-static write methods purely for my own laziness.<br>
     * It is non-static as it differs from all other write methods as it
     * combines generating and writing. This is normally avoided to keep every 
     * everything simple, clear and fast.<br>
     * <br>
     * You can write<br>
     * <code>XMLOutputStreamWriter.generateAndWriteElementWithCData(writer, "tag", "cdata");
     * </code><br>
     * <br>
     * to generate<br>
     * <code>&lt;tag>cdata&lt;/tag>
     * </code><br>
     * 
     * @param xmlWriter writer to write generated stuff to
     * @param tagName name of the element
     * @param attrPairs name/value pairs of attributes to be included into start tag -
     * if a value is <code>null</code> corresponding attribute will not be included
     * @param cData the character data of the element
     * @see #writeElementWithCData(String, String, String)
     * @see #createStartTag(String, String[][])
     * @see #createEndTag(String)
     */
    public final static void generateAndWriteElementWithCData(
        XMLWriter xmlWriter,
        String tagName,
        String[][] attrPairs,
        String cData)
        throws IOException {
        String startTag = createStartTag(tagName, attrPairs);
        String endTag = createEndTag(tagName);
        xmlWriter.writeElementWithCData(startTag, cData, endTag);
    }

    /** Convenience method for creating <em>and writing</em> a whole element. 
     * @param xmlWriter writer to write generated stuff to
     * @param tagName name of the element
     * @param attrNames names of attributes to be included into start tag
     * @param attrValues values of attributes to be included into start tag -
     * there should be just as many entries as in <code>attrNames</code>,
     * if a value is <code>null</code> corresponding attribute will not be included
     * @param cData the character data of the element
     * @see #generateAndWriteElementWithCData(XMLWriter, String, String[][], String)
     * @see #writeElementWithCData(String, String, String)
     * @see #createStartTag(String, String[], String[])
     * @see #createEndTag(String)
     */
    public final static void generateAndWriteElementWithCData(
        XMLWriter xmlWriter,
        String tagName,
        String[] attrNames,
        String[] attrValues,
        String cData)
        throws IOException {
        String startTag = createStartTag(tagName, attrNames, attrValues);
        String endTag = createEndTag(tagName);
        xmlWriter.writeElementWithCData(startTag, cData, endTag);
    }

    /** Creates a new filter writer for XML export.
     * @param writer the underlying writer the formatted XML is exported to
     */
    public XMLWriter(Writer writer) {
        super(writer);
    }

    /** Switches on/off pretty print mode.
     * <br>
     * Having it switched on (which is the default) makes output
     * pretty as newlines after tags and indentataion is done. Unfortunately,
     * if your application is sensible to whitespace in CDATA this might lead
     * to unwanted additional spaces and newlines.
     * <br>
     * If it is switched off the output is guaranteed to be correct, but looks
     * pretty funny. After before markup close (> or />) a newline is inserted
     * as otherwise you may get extremely long output lines.
     */
    public void setPrettyPrintMode(boolean prettyPrintMode) {
        this.prettyPrintMode = prettyPrintMode;
    }

    /** Gets property described in {@link #setPrettyPrintMode}. */
    public boolean getPrettyPrintMode() {
        return prettyPrintMode;
    }

    /** Sets the amount of spaces to increase indentation with element level.
     * <br>
     * This only takes effect when {@link #setPrettyPrintMode} is set to true.
     * <br>
     * <em>Caution</em>: You should better avoid to change this property while
     * exporting as this may result in unexpected output.
     */
    public void setTabWidth(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    /** Gets property described in {@link #setTabWidth}. */
    public int getTabWidth() {
        return tabWidth;
    }

    /** Sets if a newline is inserted after an empty start element 
     * by default. 
     */
    public void setNlAfterEmptyTag(boolean nlAfterEmptyTag) {
        this.nlAfterEmptyTag = nlAfterEmptyTag;
    }

    /** Gets property described in {@link #setNlAfterEmptyTag}. */
    public boolean getNlAfterEmptyTag() {
        return nlAfterEmptyTag;
    }

    /** Sets if a newline is inserted after an end tag 
     * by default. */
    public void setNlAfterEndTag(boolean nlAfterEndTag) {
        this.nlAfterEndTag = nlAfterEndTag;
    }

    /** Gets property described in {@link #setNlAfterEndTag}. */
    public boolean getNlAfterEndTag() {
        return nlAfterEndTag;
    }

    /** Sets if a newline is inserted after a non empty start tag 
     * by default. */
    public void setNlAfterStartTag(boolean nlAfterStartTag) {
        this.nlAfterStartTag = nlAfterStartTag;
    }

    /** Gets property described in {@link #setNlAfterStartTag}. */
    public boolean getNlAfterStartTag() {
        return nlAfterStartTag;
    }

    /** Writes XML declaration. 
     * XML declaration will be written 
     * using version 1.0 and no encoding defaulting
     * to standard encoding (supports UTF-8 and UTF-16):<br>
     * <code>&lt;?xml version="1.0"?></code>
     * <br>
     * If you want to have a different encoding or the standalone declaration
     * use {@link #writeProlog(String)}.<br>
     * This sets {@link #setXMLDeclarationWritten xmlDeclWritten} to 
     * <code>true</code>.
     * 
     */
    public void writeXMLDeclaration() throws IOException {
        xmlDeclWritten = true;
        needsIndent = false;
        write("<?xml version=\"1.0\"?>\n");
    }

    /** Indicates whether the XML declaration has been written, yet.
     * As it may only be written once, you can check this when writing 
     * in different contexts to same writer.
     */
    public boolean isXMLDeclarationWritten() {
        return xmlDeclWritten;
    }

    /** Manually sets or resets whether XML declaration has been written. 
     * This is done implicly by {@link #writeXMLDeclaration}, but to give you
     * the full freedom, this can be done here as well. 
     * Use {@link #isXMLDeclarationWritten} to check it.
     */
    public void setXMLDeclarationWritten(boolean xmlDeclWritten) {
        this.xmlDeclWritten = xmlDeclWritten;
    }

    /** Writes prolog data like doctype delcaration and 
     * DTD parts followed by a newline.
     * <br>
     * Do not misuse this to write plain text, but rather - if you really
     * have to - use the standard {@link #write} methods.
     */
    public void writeProlog(String prolog) throws IOException {
        needsIndent = false;
        write(prolog);
        writeNl();
    }

    /** Writes a single newline. */
    public void writeNl() throws IOException {
        needsIndent = true;
        write('\n');
    }

    /** Writes <code>comment</code> encoded as comment. */
    public void writeComment(String comment) throws IOException {
        needsIndent = false;
        write("<!-- ");
        write(comment);
        write(" -->");
    }

    /** Writes a processing instruction. */
    public void writePI(String target, String data) throws IOException {
        needsIndent = false;
        write("<?" + target + " " + data + "?>");
    }

    /** Writes a start tag.
     * @param startTag the complete start tag, e.g. <code>&lt;start></code>
     * @param nl decides whether there should be a newline after the tag
     */
    public void writeStartTag(String startTag, boolean nl) throws IOException {
        writeTag(startTag, nl);
        depthPlus();
    }

    /** Writes a start tag.
     * @param startTag the complete start tag, e.g. <code>&lt;start></code>
     * @see #setNlAfterStartTag
     */
    public void writeStartTag(String startTag) throws IOException {
        writeStartTag(startTag, nlAfterStartTag);
    }

    /** Writes an end tag.
     * @param endTag the complete end tag, e.g. <code>&lt;/end></code>
     * @param nl decides whether there should be a newline after the tag
     */
    public void writeEndTag(String endTag, boolean nl) throws IOException {
        depthMinus();
        writeTag(endTag, nl);
    }

    /** Writes an end tag.
     * @param endTag the complete end tag, e.g. <code>&lt;/end></code>
     * @see #setNlAfterEndTag
     */
    public void writeEndTag(String endTag) throws IOException {
        writeEndTag(endTag, nlAfterEndTag);
    }

    /** Writes an empty element.
     * @param emptyTag the complete tag for an empty element, e.g. <code>&lt;empty/></code>
     * @param nl decides whether there should be a newline after the tag
     */
    public void writeEmptyElement(String emptyTag, boolean nl) throws IOException {
        writeTag(emptyTag, nl);
    }

    /** Writes an empty element.
     * @param emptyTag the complete tag for an empty element, e.g. <code>&lt;start/></code>
     * @see #setNlAfterEmptyTag
     */
    public void writeEmptyElement(String emptyTag) throws IOException {
        writeEmptyElement(emptyTag, nlAfterEmptyTag);
    }

    /** Writes character data with encoding.
     * @param cData the character data to write
     */
    public void writeCData(String cData) throws IOException {
        String encoded = XMLEncode.xmlEncodeText(cData);
        writePCData(encoded);
    }

    /** Writes character data <em>without</em> encoding.
     * @param pcData the <em>parseable</em> character data to write
     */
    public void writePCData(String pcData) throws IOException {
        needsIndent = false;
        write(pcData);
    }

    /** Writes a full element consisting of a start tag, character data and
     * an end tag. There will be no newline after start tag, so character data
     * is literally preserved.
     * <br>
     * The character data will be encoded.
     *
     * @param startTag the complete start tag, e.g. <code>&lt;element></code>
     * @param cData the character data to write
     * @param endTag the complete end tag, e.g. <code>&lt;/element></code>
     */
    public void writeElementWithCData(String startTag, String cData, String endTag) throws IOException {
        writeStartTag(startTag, false);
        writeCData(cData);
        writeEndTag(endTag);
    }

    /** Writes a full element consisting of a start tag, character data and
     * an end tag. There will be no newline after start tag, so character data
     * is literally preserved.
     * <br>
     * The character data will <em>not</em> be encoded.
     *
     * @param startTag the complete start tag, e.g. <code>&lt;element></code>
     * @param pcData the <em>parseable</em> character data to write
     * @param endTag the complete end tag, e.g. <code>&lt;/element></code>
     */
    public void writeElementWithPCData(String startTag, String pcData, String endTag) throws IOException {
        writeStartTag(startTag, false);
        writePCData(pcData);
        writeEndTag(endTag);
    }

    private void writeTag(String tag, boolean nl) throws IOException {
        writeIndent();
        needsIndent = false;
        if (nl) {
            if (getPrettyPrintMode()) {
                write(tag);
                writeNl();
            } else {
                // in correct mode we need to break tag before closing > resp. />
                int length = tag.length();
                int pos;
                if ((pos = tag.indexOf("/>")) != -1) {
                    write(tag, 0, pos);
                    write('\n');
                    write(tag, pos, length - pos);
                } else if ((pos = tag.indexOf(">")) != -1) {
                    write(tag, 0, pos);
                    write('\n');
                    write(tag, pos, length - pos);
                } else {
                    write(tag);
                    write('\n');
                }
            }
        } else {
            write(tag);
        }
    }

    private void writeIndent() throws IOException {
        // indentation is only needed after a newline in pretty print mode
        if (!needsIndent)
            return;

        // every indentation destroys literal write
        if (!getPrettyPrintMode())
            return;

        // shortcut
        if (indent == 0)
            return;

        // save some computation time when indent does not change
        if (!indentStringCacheValid) {
            StringBuffer buf = new StringBuffer(indent);
            for (int i = 0; i < indent; i++) {
                buf.append(' ');
            }
            indentStringCache = buf.toString();
            indentStringCacheValid = true;
        }

        write(indentStringCache);
    }

    private void depthPlus() {
        indent += tabWidth;
        depth++;
        indentStringCacheValid = false;
    }

    private void depthMinus() {
        indent -= tabWidth;
        if (indent < 0)
            indent = 0;
        depth--;
        indentStringCacheValid = false;
    }
}
