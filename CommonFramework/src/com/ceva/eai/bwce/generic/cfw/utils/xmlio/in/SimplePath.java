package com.ceva.eai.bwce.generic.cfw.utils.xmlio.in;

import java.util.Collection;
import java.util.Iterator;

/**
 * <b>Rudimentary</b> representation of a path to an XML element. 
 * <br>
 * Two paths match in two cases:
 * <ol><li>If they are really equal in terms of the {@link #equals} method.
 * <li>If the path to match to is relative, i.e. it has no leading '/' and it is the suffix of the matching path.
 * </ol>
 * <br>
 * For example<br>
 * <code>/root/tag</code> matches <code>/root/tag</code> and<br>
 * <code>/root/tag</code> matches <code>tag</code>.
 *
 */
public class SimplePath {

    protected final String path;
    protected final Item[] pathList;

    /** Strips off ending slash from a string if there is one. */
    public final static String stripEndingSlash(String path) {
        if (path != null && path.length() > 0 && path.charAt(path.length() - 1) == '/') {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    /** Creates a path object from a string describing it. The describing
     * string uses '/' characters to seperate the paths parts.
     */
    public SimplePath(String path) {
        this(path, null);
    }

    /** Creates a path object from a string describing it. The describing
     * string uses '/' characters to seperate the paths parts.
     */
    public SimplePath(String path, Item[] pathList) {
        this.path = stripEndingSlash(path);
        this.pathList = pathList;
    }

    /** Copy ctor. */
    public SimplePath(SimplePath path) {
        this.path = stripEndingSlash(path.toString());
        this.pathList = new Item[path.pathList.length];
        System.arraycopy(path.pathList, 0, this.pathList, 0, path.pathList.length);
    }

    /**
     * Checks if an item matches the last segment of this path.
     */
    public boolean matches(Item name) {
        return (pathList != null && pathList.length > 0 && pathList[pathList.length - 1].equals(name));
    }

    /**
     * Checks if the given array of items matches this path.
     */
    public boolean matches(Item[] path, boolean isRelative) {
        if (pathList == null
            || path == null
            || path.length > pathList.length
            || (!isRelative && path.length != pathList.length)) {
            return false;
        } else {
            for (int i = path.length - 1; i >= 0; i--) {
                if (!pathList[i].equals(path[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Checks if the given array of items matches this path from the root. The given path is to be considered relative.
     * Useful to distinguish between something like /rootPath/valid/*\/valid and /rootPath/invalid/*\/valid. You will need two
     * matches for this:
     * <pre>
     * matchesFromRoot(new Item[] { new Item("rootPath"), new Item("valid")}) 
     * &&
     * matches(new Item("valid"))
     * </pre>
     */
    public boolean matchesFromRoot(Item[] path) {
        if (pathList == null || path == null || path.length > pathList.length) {
            return false;
        } else {
            for (int i = 0; i < path.length; i++) {
                if (!pathList[i].equals(path[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Checks if the given array of items matches this path. The given path is to be considered relative.
     */
    public boolean matches(Item[] path) {
        return matches(path, true);
    }

    /** Finds out if the the given path matches this one. 
     */
    public boolean matches(SimplePath matchPath) {
        return matches(matchPath.toString());
    }

    /** Finds out if the path represented by the given string matches this one. 
     * @see #matches(SimplePath)
    */
    public boolean matches(String matchPath) {
        String matchString = stripEndingSlash(matchPath);

        if (matchString != null && matchString.length() > 0 && matchString.charAt(0) != '/') {
            // relative
            return path.endsWith("/" + matchString);
        } else {
            // absolute
            return path.equals(matchString);
        }
    }

    /** Checks if this path matches any of the paths stored in
     * <code>paths</code> collection. This means we iterate through 
     * <code>paths</code> and match every entry to this path.
     */
    public boolean matchsAny(Collection<SimplePath> paths) {
        for (Iterator<SimplePath> it = paths.iterator(); it.hasNext();) {
            SimplePath matchPath = it.next();
            if (matches(matchPath))
                return true;
        }
        return false;
    }

    /** Checks if this path matches any of the paths stored in
     * <code>paths</code> collection. This means we iterate through 
     * <code>paths</code> and match every entry to this path.
     */
    public boolean matchsAny(String[] paths) {
        for (int i = 0; i < paths.length; i++) {
            if (matches(paths[i]))
                return true;
        }
        return false;
    }

    public String toString() {
        return path;
    }

    public boolean equals(Object o) {
        if (o instanceof String) {
            return path.equals(o);
        } else if (o instanceof SimplePath) {
            return path.equals(((SimplePath) o).toString());
        } else {
            return false;
        }
    }

}
