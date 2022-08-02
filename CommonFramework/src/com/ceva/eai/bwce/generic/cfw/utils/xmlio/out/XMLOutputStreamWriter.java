package com.ceva.eai.bwce.generic.cfw.utils.xmlio.out;

import java.io.*;

/**
 * Adds XML export functionality to the underlying output stream. Formatting and
 * encoding is done as straight forward as possible. <br>
 * Everything you know better than this class must be done by you, e.g. you will
 * have to tell <code>XMLOutputStreamWriter</code> where you wish to have
 * newlines.In effect, no unexpected so called
 * <em>intelligent</em> behavior is to be feared. Another effect is high speed.
 * <br>
 * <br>
 * <em>Caution</em>: Do not forget to call {@link #flush} at the end of your
 * exporting process as otherwise no data might be written.
 *
 * <em>Warning</em>: When using two byte encoding (e.g. UTF-16) underlying
 * OutputStream can
 * not savely be brought to string. Do <em>not</em> use
 * {@link ByteArrayOutputStream} with two byte encoding, as XML declaration
 * will be in single byte encoding (according to XML spec) and the rest will be
 * in double byte totally confusing ByteArrayOutputStream encoding to string.
 * <b>If you want to have string output use {@link XMLWriter} filtering 
 * {@link StringWriter} or for convenience {@link XMLStringWriter}.</b>
 *
 */
public class XMLOutputStreamWriter extends XMLWriter {
    /** Name of UTF-8 encoding  */
    public static String ENCODING_UTF_8 = "UTF-8";
    /** Name of UTF-16 encoding */
    public static String ENCODING_UTF_16 = "UTF-16";
    /** Name of ISO-8859-1 encoding */
    public static String ENCODING_ISO_8859_1 = "ISO-8859-1";

    /** Name of standard encoding */
    public static String ENCODING_STANDARD = ENCODING_UTF_8;
    /** Alias for ISO-8859-1 encoding */
    public static String ENCODING_ISO_LATIN1 = ENCODING_ISO_8859_1;

    protected OutputStream os;
    protected String encodingName;

    /** Creates a new output stream writer for XML export.
     * @param os the underlying output stream the XML is exported to
     * @param encodingName name of the encoding used to write XML as well as
     * for the XML declaration (e.g. UTF-8, ISO-8859-1, ...)
     */
    public XMLOutputStreamWriter(OutputStream os, String encodingName) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(os, encodingName));
        this.encodingName = encodingName;
        this.os = os;
    }

    /** Creates a new output stream writer for XML export. Standard encoding
     * will be used as found in {@link #ENCODING_STANDARD}, which usually is
     * UTF-8.
     * @param os the underlying output stream the XML is exported to
     * @see #XMLOutputStreamWriter(OutputStream, String)
     */
    public XMLOutputStreamWriter(OutputStream os) throws UnsupportedEncodingException {
        this(os, ENCODING_STANDARD);
    }

    /** Gets the name of the encoding as it would be inserted into the
     * XML declaration. {@link OutputStreamWriter#getEncoding} may return something less verbose.
     * @see OutputStreamWriter#getEncoding
     */
    public String getEncodingName() {
        return encodingName;
    }

    /** Writes XML declaration using version 1.0 and encoding specified in
     * constructor.
     * <em>Caution</em>: As XML declaration must be in plain text (no UNICODE)
     * it will not be passed to writer, but directly to stream!
     */
    public void writeXMLDeclaration() throws IOException {
        String xmlDecl = "<?xml version=\"1.0\" encoding=\"" + getEncodingName() + "\"?>\n";
        byte[] xmlDeclBytes = xmlDecl.getBytes("US-ASCII");

        // flush to ensure correct sequence
        flush();
        os.write(xmlDeclBytes);
    }

}
