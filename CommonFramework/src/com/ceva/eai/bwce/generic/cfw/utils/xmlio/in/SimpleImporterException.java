package com.ceva.eai.bwce.generic.cfw.utils.xmlio.in;

/**
 * Tells us that there is something wrong with {@link SimpleImporter}. <br>
 * Needs to be a runtime excpetion to get it thrown out of SAX callbacks.
 * In any case if this exception is thrown it is really fatal.
 *
 */
public class SimpleImporterException extends RuntimeException {

	private static final long serialVersionUID = -737546631326685647L;

	public SimpleImporterException(String message) {
        super(message);
    }
}
