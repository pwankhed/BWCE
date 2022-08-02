package com.ceva.eai.bwce.generic.cfw.model;

import java.util.regex.Pattern;

public class DocumentPropertyUtils {

	static public boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	static public DocumentProperty getPropFromString(final String entry) {
		// split key from value
		String[] keyValPair = entry.split(DocumentProperty.VALUE_SEP, 2);
		String key = keyValPair[0];

		// split type from key
		String pType = "string";
		if (key.contains(DocumentProperty.TYPE_SEP)) {
			String[] nameTypePair = key.split(DocumentProperty.TYPE_SEP, 2);
			key = nameTypePair[0];
			if (!isEmpty(nameTypePair[1])) {
				pType = nameTypePair[1];
			}
		}

		// get name and category
		String name = key;
		String cat = Document.CATEGORY;

		String cat_sep = null;
		if (key.contains(DocumentProperty.CATEGORY_SEP) && !key.startsWith(DocumentProperty.CATEGORY_SEP)) {
			cat_sep = Pattern.quote(DocumentProperty.CATEGORY_SEP);
		} else if (key.contains(DocumentProperty.CATEGORY_ALT_SEP)
				&& !key.startsWith(DocumentProperty.CATEGORY_ALT_SEP)) {
			cat_sep = Pattern.quote(DocumentProperty.CATEGORY_ALT_SEP);
		} else if (key.contains(DocumentProperty.CATEGORY_ALT2_SEP)
				&& !key.startsWith(DocumentProperty.CATEGORY_ALT2_SEP)) {
			cat_sep = Pattern.quote(DocumentProperty.CATEGORY_ALT2_SEP);
		}
		if (cat_sep != null) {
			String[] nameCatPair = key.split(cat_sep, 2);
			if (!isEmpty(nameCatPair[0])) {
				cat = nameCatPair[0];
			}
			if (!isEmpty(nameCatPair[1])) {
				name = nameCatPair[1];
			}
		}

		String value = "";
		if (keyValPair.length == 2) {
			value = keyValPair[1];
		}

		DocumentProperty p = DocumentProperty.create().setCategory(cat).setName(name).setType(pType).setValue(value)
				.build();
		return p;
	}

	static public DocumentPropertySet<DocumentProperty> parsePropertyArray(final String[] properties) {
		DocumentPropertySet<DocumentProperty> propMap = new DocumentPropertySet<>();
		if (properties != null && properties.length > 0) {
			for (String entry : properties) {
				DocumentProperty p = getPropFromString(entry);
				propMap.add(p);
			}
		}
		return propMap;
	}

	static public DocumentPropertySet<DocumentProperty> parsePropertyString(final String properties) {
		DocumentPropertySet<DocumentProperty> propMap = new DocumentPropertySet<>();
		if (!isEmpty(properties)) {
			String[] entries = properties.split("\\|\\|");
			for (String entry : entries) {
				DocumentProperty p = getPropFromString(entry);
				propMap.add(p);
			}
		}
		return propMap;
	}
}
