package com.ceva.eai.bwce.generic.cfw.http;

import com.ceva.eai.bwce.generic.cfw.model.Document;
import com.tibco.bw.palette.shared.java.BWActivityContext;
import com.tibco.bw.runtime.ActivityContext;

public class HttpClientAlt {
	private ActivityContext<?> context;
	
	@BWActivityContext
	public void assignContext(final ActivityContext<?> context) {
		this.context = context;
	}
	
	public void invoke(HttpClientRequest request, Document[] documentArray) {
		
	}
}
