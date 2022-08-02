package com.ceva.eai.bwce.generic.cfw.utils.xmlio.out;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class XMLEncodeStream {
		 
	static final private int lastPrintable = 0x7E;

	public XMLEncodeStream() {
	}

	static public void xmlEncodeReaderAsPCDATA(Reader reader, Writer writer, boolean forAttribute) throws IOException {
		int ch;
		while ((ch = reader.read()) != -1) {
			switch (ch) {
			case '<': {
				writer.append("&lt;");
				break;
			}
			case '>': {
				writer.append("&gt;");
				break;
			}
			case '&': {
				writer.append("&amp;");
				break;
			}
            case '"' :
                if (forAttribute)
                	writer.append("&quot;");
                else
                	writer.write(ch);
                break;
            case '\'' :
                if (forAttribute)
                	writer.append("&apos;");
                else
                	writer.write(ch);
                break;		
            case '\n' :
                if (forAttribute)
                	writer.append("&#xA;");
                else
                	writer.write(ch);
                break;  
            case '\t' :
                if (forAttribute)
                	writer.append("&#x9;");
                else
                	writer.write(ch);
                break;   
            case '\r' :
                if (forAttribute)
                	writer.append("&#xD;");
                else
                	writer.write(ch);
                break;                 
			default: {
				if ((ch >= ' ' && ch <= lastPrintable && ch != 0xF7)) {
					// If the character is not printable, print as character reference.
					// Non printables are below ASCII space but not tab or line
					// terminator, ASCII delete, or above a certain Unicode threshold.
					writer.write(ch);
				} else if ((ch >= 0x0 && ch <= 0x8) || (ch >= 0xB && ch <= 0xC) || (ch >= 0xE && ch <= 0x1F)
						|| (ch >= 0x7F && ch <= 0x84) || (ch >= 0x86 && ch <= 0x9F) || (ch >= 0xFDD0 && ch <= 0xFDEF)
						|| (ch >= 0x1FFFE && ch <= 0x1FFFF) || (ch >= 0x2FFFE && ch <= 0x2FFFF)
						|| (ch >= 0x3FFFE && ch <= 0x3FFFFF) || (ch >= 0x4FFFE && ch <= 0x4FFFF)
						|| (ch >= 0x5FFFE && ch <= 0x5FFFF) || (ch >= 0x6FFFE && ch <= 0x6FFFFF)
						|| (ch >= 0x7FFFE && ch <= 0x7FFFF) || (ch >= 0x8FFFE && ch <= 0x8FFFF)
						|| (ch >= 0x9FFFE && ch <= 0x9FFFFF) || (ch >= 0xAFFFE && ch <= 0xAFFFF)
						|| (ch >= 0xBFFFE && ch <= 0xBFFFF) || (ch >= 0xCFFFE && ch <= 0xCFFFFF)
						|| (ch >= 0xDFFFE && ch <= 0xDFFFF) || (ch >= 0xEFFFE && ch <= 0xEFFFF)
						|| (ch >= 0xFFFFE && ch <= 0xFFFFF) || (ch >= 0x10FFFE && ch <= 0x10FFFF)) {
					// ignore
				} else if ((ch == 0x9) || (ch == 0xA) || (ch == 0xD) || ((ch >= 0x20) && (ch <= 0xD7FF))
						|| ((ch >= 0xE000) && (ch <= 0xFFFD)) || ((ch >= 0x10000) && (ch <= 0x10FFFF))) {
					writer.append("&#");
					writer.append(Integer.toString(ch));
					writer.append(';');
				}
			}
			break;
			}
		}
	}

}
