package com.ceva.eai.bwce.generic.cfw.utils.io;

import java.io.CharArrayWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.BitSet;

public class URLEncodingFilterWriter extends FilterWriter {
	private BitSet dontNeedEncoding;
	private final int caseDiff = ('a' - 'A');
	private Charset charset;
	private final int DEFAULT_BUFFER_SIZE = 1024;
	private CharArrayWriter charArrayWriter = new CharArrayWriter(DEFAULT_BUFFER_SIZE);
	private int delayedChar = 0;

	public URLEncodingFilterWriter(Writer out, Charset charset) {
		super(out);
		this.charset = charset;
		dontNeedEncoding = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++) {
			dontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++) {
			dontNeedEncoding.set(i);
		}
		for (i = '0'; i <= '9'; i++) {
			dontNeedEncoding.set(i);
		}
		dontNeedEncoding.set(' '); /*
									 * encoding a space to a + is done in the encode() method
									 */
		dontNeedEncoding.set('-');
		dontNeedEncoding.set('_');
		dontNeedEncoding.set('.');
		dontNeedEncoding.set('*');
	}

	public URLEncodingFilterWriter(Writer out) {
		this(out, Charset.defaultCharset());
	}

	@Override
	public void write(char cbuf[], int off, int len) throws IOException {

		for (int i = off; i < len;) {
			int c = (int) cbuf[i];
			if (dontNeedEncoding.get(c)) {
				if (delayedChar > 0) {
					charArrayWriter.write(delayedChar);
					delayedChar = 0;
					flush();
				}
				if (c == ' ') {
					c = '+';
				}
				out.append((char) c);
				i++;
			} else {
				// convert to external encoding before hex conversion
				do {
					if (delayedChar > 0) {
						charArrayWriter.write(delayedChar);
						delayedChar = 0;
					}
					/*
					 * If this character represents the start of a Unicode surrogate pair, then pass
					 * in two characters. It's not clear what should be done if a byte reserved in
					 * the surrogate pairs range occurs outside of a legal surrogate pair. For now,
					 * just treat it as if it were any other character.
					 */
					if (c >= 0xD800 && c <= 0xDBFF) {
						if ((i + 1) < len) {
							if (delayedChar != 0) {
								charArrayWriter.write(delayedChar);
								charArrayWriter.write(c);
								delayedChar = 0;
							} else {
								charArrayWriter.write(c);
								int d = (int) cbuf[i + 1];
								if (d >= 0xDC00 && d <= 0xDFFF) {
									charArrayWriter.write(d);
									i++;
								}
							}
						} else {
							if (delayedChar > 0) {
								charArrayWriter.write(delayedChar);
								charArrayWriter.write(c);
								delayedChar = 0;
							} else {
								delayedChar = c;
							}
						}
					} else {
						charArrayWriter.write(c);
					}
					i++;
				} while (i < len && !dontNeedEncoding.get((c = (int) cbuf[i])));
				flush();
			}
		}
	}

	@Override
	public void write(int c) throws IOException {
		char[] cbuf = { (char) c };
		this.write(cbuf, 0, cbuf.length);
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		this.write(cbuf, 0, cbuf.length);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
	}

	@Override
	public void flush() throws IOException {
		charArrayWriter.flush();
		String str = new String(charArrayWriter.toCharArray());
		byte[] ba = str.getBytes(charset);
		for (int j = 0; j < ba.length; j++) {
			out.append('%');
			char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
			// converting to use uppercase letter as part of
			// the hex value if ch is a letter.
			if (Character.isLetter(ch)) {
				ch -= caseDiff;
			}
			out.append(ch);
			ch = Character.forDigit(ba[j] & 0xF, 16);
			if (Character.isLetter(ch)) {
				ch -= caseDiff;
			}
			out.append(ch);
		}
		charArrayWriter.reset();

	}

	public static int copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[2];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/* static public void main(String[] args) throws Exception {
		String toBeTranslated = new String(Character.toChars(0x1f604)) + "$#$$234 To be encoded message, ë..."
				+ new String(Character.toChars(0x1f604)) + "!";
		System.out.println(toBeTranslated);
		StringWriter writer = new StringWriter();
		StringReader reader = new StringReader(toBeTranslated);
		try {
			URLEncodingFilterWriter urlwriter = new URLEncodingFilterWriter(writer);
			try {
				copy(reader, urlwriter);
			} finally {
				urlwriter.flush();
				urlwriter.close();
			}

		} finally {
			reader.close();
		}
		System.out.println(writer.toString());
		System.out.println(URLEncoder.encode(toBeTranslated, "UTF-8"));
	}*/

}
