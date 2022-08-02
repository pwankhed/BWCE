package com.ceva.eai.bwce.generic.cfw.utils.xmlio.in;

import org.xml.sax.helpers.AttributesImpl;

/**
 * Empty implementation for callback interface {@link SimpleImportHandler} for {@link SimpleImporter}. 
 * For lazy people - like me - who do not like to write empty method bodies.
 *
 */
public class DefaultSimpleImportHandler implements SimpleImportHandler {
    
    public void startDocument() { }
    public void endDocument() { }
    
    public void cData(SimplePath path, String cdata) { }
    
    public void startElement(SimplePath path, String name, AttributesImpl attributes, String leadingCDdata) { }
    public void endElement(SimplePath path, String name) { }
}
