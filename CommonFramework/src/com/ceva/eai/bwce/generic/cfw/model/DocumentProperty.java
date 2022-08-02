package com.ceva.eai.bwce.generic.cfw.model;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author nlou
 */
public class DocumentProperty implements Serializable {

    protected static final String STRING_TYPE = "string";
    protected static final String BOOLEAN_TYPE = "boolean";
    protected static final String LONG_TYPE = "long";
    protected static final String INT_TYPE = "integer";
    protected static final String DOUBLE_TYPE = "double";
    protected static final String BYTE_TYPE = "byte";
    protected static final String SHORT_TYPE = "short";
    protected static final String FLOAT_TYPE = "float";
    protected static final String CATEGORY_SEP = "__";
    protected static final String CATEGORY_ALT_SEP = "::";
    protected static final String CATEGORY_ALT2_SEP = "$$";
    protected static final String TYPE_SEP = "@@";
    protected static final String VALUE_SEP = "==";
    private static final long serialVersionUID = 8017842335655242137L;    
    private String name;
    private String value;
    private String category = Document.CATEGORY;
    private String type = STRING_TYPE;
    private String key;
    

    static public DocumentProperty create() {
		DocumentProperty p = new DocumentProperty();
		return p;
	}
    
    public DocumentProperty build() {
    	key = getKey();
    	return this;
    }
            
	private DocumentProperty() {
    }

    public String getCategory() {
        return category;
    }

    public DocumentProperty setCategory(String category) {
        if (category != null && !category.isEmpty()) {
            this.category = category;
        }
		return this;
    }

    public String getValue() {
        return value;
    }

    public DocumentProperty setValue(String value) {
        this.value = value;
		return this;        
    }

    public String getName() {
        return name;
    }

    public DocumentProperty setName(String name) {
        this.name = name;
		return this;        
    }

    public DocumentProperty setType(String type) {
        if (type != null && !type.isEmpty()) {
            this.type = type;
        }    	
		return this;        
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        if (category != null && category.length() > 0) {
            key = category + CATEGORY_SEP + name;
        } else {
            key = Document.CATEGORY + CATEGORY_SEP + name;
        }
        return key;
    }    

	@Override
	public int hashCode() {
		return Objects.hash(getCategory(), getName(), getKey());
	}

	@Override
	public boolean equals(Object obj) {	
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DocumentProperty)) {
			return false;
		}
		DocumentProperty other = (DocumentProperty) obj;
		return Objects.equals(getCategory(), other.getCategory())
				&& Objects.equals(getName(), other.getName())
				&& Objects.equals(getKey(), other.getKey());
	}
	
	@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			sb.append("\"category\":\"").append(getCategory()).append("\",");
			sb.append("\"name\":\"").append(getName()).append("\",");
			sb.append("\"type\":\"").append(getType()).append("\",");
			sb.append("\"value\":\"").append(getValue()).append("\",");
			sb.append("\"key\":\"").append(getKey()).append("\"");
            sb.append("}");
			return sb.toString();
		}
    

}