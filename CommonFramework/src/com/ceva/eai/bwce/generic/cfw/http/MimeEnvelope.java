package com.ceva.eai.bwce.generic.cfw.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MimeEnvelope implements Serializable {

	private static final long serialVersionUID = 1436099427099019472L;
	private List<MimePart> mimePart = new ArrayList<>();
	
	public List<MimePart> getMimePart() {
		return mimePart;
	}
	
	public void setMimePart(List<MimePart> mimePart) {
		this.mimePart = mimePart;
	}
}
