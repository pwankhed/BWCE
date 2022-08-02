package com.ceva.eai.bwce.generic.cfw.utils.xmlio.in;

/**
 * Representation of a path element. 
 *
 */
public final class Item {

    public static final Item ITEM_ANY = null;
    
    private final String namespaceURI;
    private final String name;

    public Item() {
        this(null, null);
    }
    public Item(String name) {
        this(name, null);
    }

    public Item(String name, String namespaceURI) {
        this.namespaceURI = namespaceURI;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }
        Item token = (Item) o;
        return (
            (token.name == null || this.name.equals(token.name))
                && (this.namespaceURI == null
                    || token.namespaceURI == null
                    || this.namespaceURI.equals(token.namespaceURI)));

    }

    public String toString() {
        if (namespaceURI == null || namespaceURI.length() == 0) {
            return name;
        } else {
            return namespaceURI + ":" +name;
        }
    }
}