package com.ceva.eai.bwce.generic.cfw.model;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class DocumentPropertySet<E extends DocumentProperty> extends AbstractSet<E>
		implements Set<E>, Cloneable, java.io.Serializable {
	private static final long serialVersionUID = -7980667361421005757L;
	private transient HashMap<String, E> map;

	public DocumentPropertySet() {
		map = new HashMap<>();
	}

	@Override
	public Iterator<E> iterator() {
		return map.values().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof DocumentProperty)) {
			return false;
		}
		DocumentProperty op = (DocumentProperty) o;
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			if (op.getKey().equals(it.next().getKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean add(E e) {
		map.put(e.getKey(), e);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		if (!(o instanceof DocumentProperty)) {
			return false;
		}
		DocumentProperty dp = (DocumentProperty) o;
		return map.remove(dp.getKey()) != null;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		try {
			DocumentPropertySet<E> newSet = (DocumentPropertySet<E>) super.clone();
			newSet.map = (HashMap<String, E>) map.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	public boolean removeProperty(String name, String category) {
		String key;
		if (category != null && category.length() > 0) {
			key = category + DocumentProperty.CATEGORY_SEP + name;
		} else {
			key = Document.CATEGORY + DocumentProperty.CATEGORY_SEP + name;
		}
		return map.remove(key) != null;
	}

	public boolean removeProperty(String name) {
		String key = Document.CATEGORY + DocumentProperty.CATEGORY_SEP + name;
		return map.remove(key) != null;
	}

	public DocumentProperty findProperty(String name, String category) {
		String key;
		if (category != null && category.length() > 0) {
			key = category + DocumentProperty.CATEGORY_SEP + name;
		} else {
			key = Document.CATEGORY + DocumentProperty.CATEGORY_SEP + name;
		}
		return map.get(key);
	}

	public DocumentProperty findProperty(String name) {
		String key = Document.CATEGORY + DocumentProperty.CATEGORY_SEP + name;
		return map.get(key);
	}

	public boolean containsProperty(String name, String category) {
		String key;
		if (category != null && category.length() > 0) {
			key = category + DocumentProperty.CATEGORY_SEP + name;
		} else {
			key = Document.CATEGORY + DocumentProperty.CATEGORY_SEP + name;
		}
		return map.containsKey(key);
	}

	public boolean containsProperty(String name) {
		String key = Document.CATEGORY + DocumentProperty.CATEGORY_SEP + name;
		return map.containsKey(key);
	}

	public void deleteDocumentProperties(String categoryMask, String nameMask) {
		Pattern catPattern = null;
		if (!DocumentPropertyUtils.isEmpty(categoryMask)) {
			catPattern = Pattern.compile(categoryMask);
		}
		Pattern namePattern = null;
		if (!DocumentPropertyUtils.isEmpty(nameMask)) {
			namePattern = Pattern.compile(nameMask);
		}
		if (namePattern != null || catPattern != null) {
			for (Iterator<E> it = iterator(); it.hasNext();) {
				DocumentProperty docProperty = it.next();
				if (docProperty == null) {
					continue;
				}
				if (Document.CATEGORY.equals(docProperty.getCategory())) {
					continue;
				}
				boolean cat_matches = false;
				boolean name_matches = false;
				if (catPattern != null && docProperty.getCategory() != null) {
					cat_matches = catPattern.matcher(docProperty.getCategory()).matches();
				}
				if (namePattern != null && docProperty.getName() != null) {
					name_matches = namePattern.matcher(docProperty.getName()).matches();
				}

				if (name_matches || cat_matches) {
					it.remove();
				}
			}
		}
	}
	

}
