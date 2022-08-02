package com.ceva.eai.bwce.generic.cfw.model;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author nlou
 */
public class DocumentProperties implements Serializable {

	private static final long serialVersionUID = -1721172999920873955L;
	private Set<DocumentProperty> property = new DocumentPropertySet<>();

	public DocumentProperties() {
	}
	
	public DocumentPropertySet<DocumentProperty> asDocumentPropertySet() {
		return (DocumentPropertySet<DocumentProperty>)property;
	}

	public Set<DocumentProperty> getProperty() {
		return property;
	}

	public void setProperty(Set<DocumentProperty> property) {
		this.property = property;
	}
	
	public String retrievePropertyValue(final String name, final String category, final String defaultValue) {
		String result = defaultValue;
		if (asDocumentPropertySet().containsProperty(name, category)) {
			String value = asDocumentPropertySet().findProperty(name, category).getValue();
			if (!DocumentPropertyUtils.isEmpty(value)) {
				result = value;
			}
		}
		return result;
	}

	public String retrieveString(final String name, final String defaultVal) {
		return retrievePropertyValue(name, Document.CATEGORY, defaultVal);
	}

	public long retrieveLong(final String name, final long defaultVal) {
		long result = Long.parseLong(retrieveString(name, Long.toString(defaultVal)));
		return result;
	}

	public int retrieveInt(final String name, final int defaultVal) {
		int result = Integer.parseInt(retrieveString(name, Integer.toString(defaultVal)));
		return result;
	}

	public boolean retrieveBool(final String name, final boolean defaultVal) {
		boolean result = Boolean.parseBoolean(retrieveString(name, Boolean.toString(defaultVal)));
		return result;
	}
	

	public void addProperty(final String name, final String type, final String value) {
		this.addProperty(Document.CATEGORY, name, type, value);
	}

	public void addProperty(final String category,final String name, final String type, final String value) {
		if (value==null) {
			return;
		}
		// make sure existing is gone
		// retrieveProperties().removeProperty(name, category);
		DocumentProperty p = DocumentProperty.create().setName(name).setType(type).setCategory(category).setValue(value)
				.build();
		asDocumentPropertySet().add(p);
	}	
	
	public void clearProperties() {
		this.property.clear();
	}	
	
}