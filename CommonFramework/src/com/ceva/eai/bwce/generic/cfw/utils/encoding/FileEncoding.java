package com.ceva.eai.bwce.generic.cfw.utils.encoding;

import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File encoding has a good understanding of how files are put together and is
 * therefore in the pound seat to answer the often asked question, namely, is
 * this file encoded in binary or in plain ASCII text.
 */
public enum FileEncoding {

	/**
	 * Use this handle to grab the sole instance of this FileEncoding class.
	 */
	SINGLE_INSTANCE;

	/**
	 * Test for files that contain text. This text is by far and away more
	 * complicated than it initially sounds. However, with a liberal sprinkling of
	 * common sense we can arrive at a reasonable implementation that will work nine
	 * times out of ten.
	 *
	 * @param fis
	 * @param acceptUTF8encoding if <b>true</b>, a file in UTF-8 encoding will be
	 *                           accepted if it contains only characters existing in
	 *                           the Latin-1 table.
	 * @return true if the file contents are textual in nature, false otherwise.
	 * @throws IOException if problems reading the file are encountered.
	 */
	public boolean contentIsText(final InputStream fis, boolean acceptUTF8encoding) throws IOException {
		final int BUFFER_SIZE = 10 * 1024;
		boolean isText = true;
		byte[] buffer = new byte[BUFFER_SIZE];
		final int read = fis.read(buffer);
		int lastByteTranslated = 0;
		for (int i = 0; i < read && isText; i++) {
			final byte singleByte = buffer[i];
			int unsignedByte = singleByte & (0xff); // unsigned
			int utf8value = lastByteTranslated + unsignedByte;
			lastByteTranslated = (unsignedByte) << 8;
			isText = isCharacterTextOrBinary(acceptUTF8encoding, unsignedByte, utf8value);
		}
		return isText;
	}

	public boolean contentIsTextLoose(final InputStream fis, boolean acceptUTF8encoding, long nonTextAllowedPerc,
			long maxReadBytes) throws IOException {
		final int BUFFER_SIZE = 10 * 1024;
		boolean isText;
		double totalBytes = 0;
		double nonTextBytes = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		final int read = fis.read(buffer);
		int lastByteTranslated = 0;
		for (int i = 0; i < read; i++) {
			totalBytes++;
			final byte singleByte = buffer[i];
			int unsignedByte = singleByte & (0xff); // unsigned
			int utf8value = lastByteTranslated + unsignedByte;
			lastByteTranslated = (unsignedByte) << 8;
			isText = isCharacterTextOrBinary(acceptUTF8encoding, unsignedByte, utf8value);
			if (!isText) {
				nonTextBytes++;
			}
			if (maxReadBytes > 0 && totalBytes > maxReadBytes) {
				break;
			}
		}
		long percNonText = 0;
		if (totalBytes > 0) {
			percNonText = Math.round((nonTextBytes / totalBytes) * 100.0);
		}
		final Logger logger = LoggerFactory.getLogger("bw.logger");

		isText = percNonText <= nonTextAllowedPerc;
		logger.debug("Perc non text:" + Double.toString(percNonText) + " " + nonTextBytes + " of total:" + totalBytes
				+ " isText:" + isText);
		return isText;
	}

	public boolean contentIsTextMime(final InputStream fis) throws IOException {
		return mimeTypeIsText(detectMimeType(fis));
	}

	private boolean isCharacterTextOrBinary(boolean acceptUTF8encoding, int unsignedByte, int utf8value) {

		final int ASCII_TEXT_SYMBOLS_LOWER_BOUND = 0x20;
		final int ASCII_TEXT_SYMBOLS_UPPER_BOUND = 0x7E;

		final int LATIN_CHARSET_LOWER_BOUND = 0xA0;
		final int LATIN_CHARSET_UPPER_BOUND = 0xEE;

		final int LATIN_IN_UTF_8_LOWER_BOUND = 0x2E2E;
		final int LATIN_IN_UTF_8_UPPER_BOUND = 0xC3BF;

		final int CARRIAGE_RETURN_CHARACTER = 0x0D;
		final int TAB_CHARACTER = 0x09;
		final int LINE_FEED_CHARACTER = 0x0A;
		final int FORM_FEED_CHARACTER = 0x0C;

		return unsignedByte == TAB_CHARACTER || unsignedByte == LINE_FEED_CHARACTER
				|| unsignedByte == FORM_FEED_CHARACTER || unsignedByte == CARRIAGE_RETURN_CHARACTER
				|| (unsignedByte >= ASCII_TEXT_SYMBOLS_LOWER_BOUND && unsignedByte <= ASCII_TEXT_SYMBOLS_UPPER_BOUND)
				|| (unsignedByte >= LATIN_CHARSET_LOWER_BOUND && unsignedByte <= LATIN_CHARSET_UPPER_BOUND)
				|| (acceptUTF8encoding
						&& (utf8value >= LATIN_IN_UTF_8_LOWER_BOUND && utf8value <= LATIN_IN_UTF_8_UPPER_BOUND));
	}

	public String detectMimeType(final InputStream fis) throws IOException {
		Tika tika = new Tika();
		return tika.detect(fis);
	}

	public boolean mimeTypeIsText(String mimeType) {
		boolean isText = false;
		if (mimeType != null) {
			isText = mimeType.matches("text|json|xml|html|yaml");
		}
		return isText;
	}

}
