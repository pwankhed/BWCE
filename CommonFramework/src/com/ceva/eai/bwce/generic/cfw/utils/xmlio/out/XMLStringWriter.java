package com.ceva.eai.bwce.generic.cfw.utils.xmlio.out;

import java.io.IOException;
import java.io.StringWriter;

/**
 * This is a convenience class for writing XML to a string. As
 * no IOExceptions will occur this class catches them for you
 * doing nothing. Call {@link #toString} to finally get your string.
 * As constructor for {@link XMLWriter} already needs writer call
 * {@link #create} to get your objects instead of consructor.
 *
 */
public class XMLStringWriter extends XMLWriter {

    /** Creates a new <code>XMLStringWriter</code> objects. */
    public static XMLStringWriter create() {
        return new XMLStringWriter(new StringWriter());
    }

    private StringWriter sw;

    private XMLStringWriter(StringWriter sw) {
        super(sw);
        this.sw = sw;
    }

    /** Gets the string representation of your written XML. */
    public String toString() {
        try {
            flush();
        } catch (IOException ioe) {
            // won't happen...
        }
        sw.flush();
        return sw.toString();
    }

    public void writeXMLDeclaration() {
        try {
            super.writeXMLDeclaration();
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeProlog(String prolog) {
        try {
            super.writeProlog(prolog);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeNl() {
        try {
            super.writeNl();
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeComment(String comment) {
        try {
            super.writeComment(comment);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writePI(String target, String data) {
        try {
            super.writePI(target, data);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeStartTag(String startTag, boolean nl) {
        try {
            super.writeStartTag(startTag, nl);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeStartTag(String startTag) {
        try {
            super.writeStartTag(startTag);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeEndTag(String endTag, boolean nl) {
        try {
            super.writeEndTag(endTag, nl);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeEndTag(String endTag) {
        try {
            super.writeEndTag(endTag);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeEmptyElement(String emptyTag, boolean nl) {
        try {
            super.writeEmptyElement(emptyTag, nl);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeEmptyElement(String emptyTag) {
        try {
            super.writeEmptyElement(emptyTag);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeCData(String cData) {
        try {
            super.writeCData(cData);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writePCData(String pcData) {
        try {
            super.writePCData(pcData);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeElementWithCData(String startTag, String cData, String endTag) {
        try {
            super.writeElementWithCData(startTag, cData, endTag);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

    public void writeElementWithPCData(String startTag, String pcData, String endTag) {
        try {
            super.writeElementWithPCData(startTag, pcData, endTag);
        } catch (IOException ioe) {
            // won't happen...
        }
    }

}
