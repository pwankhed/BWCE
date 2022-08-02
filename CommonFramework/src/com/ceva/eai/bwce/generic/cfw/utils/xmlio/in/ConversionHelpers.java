package com.ceva.eai.bwce.generic.cfw.utils.xmlio.in;

/**
 * Collection of some simple conversion and fallback methods for convenience.
 *
 */
public class ConversionHelpers {

    /** Returns <code>value</code> if not null, otherwise <code>fallBack</code>.
     */
    public static String getString(String value, String fallBack) {
        if (value == null)
            return fallBack;
        return value;
    }

    /** Gets int value from a string value. 
     * @param value string value to get int from
     * @return int representation of value or <code>-1</code> 
     * if it can not be converted to an int
     */
    public static int getInt(String value) {
        return getInt(value, -1);
    }

    /** Gets int value from a string value. 
     * @param value string value to get int from
     * @param fallBack fall back value
     * @return int representation of value or <code>fallBack</code> 
     * if it can not be converted to an int
     */
    public static int getInt(String value, int fallBack) {
        if (value == null)
            return fallBack;
        try {
            return Integer.valueOf(value).intValue();
        } catch (NumberFormatException nfe) {
            return fallBack;
        }
    }

    /** Gets long value from a string value. 
     * @param value string value to get long from
     * @return long representation of value or <code>-1L</code> 
     * if it can not be converted to a long
     */
    public static long getLong(String value) {
        return getLong(value, -1L);
    }

    /** Gets long value from a string value. 
     * @param value string value to get long from
     * @param fallBack fall back value
     * @return long representation of value or <code>fallBack</code> 
     * if it can not be converted to a long
     */
    public static long getLong(String value, long fallBack) {
        if (value == null)
            return fallBack;
        try {
            return Long.valueOf(value).longValue();
        } catch (NumberFormatException nfe) {
            return fallBack;
        }
    }

    /** Gets boolean value from a string value.
     * @param value string value to get boolean from
     * @param fallBack fall back value
     * @return boolean representation of value <code>fallBack</code> 
     * if it can not <em>properly</em> be converted to a boolean
     */
    public static boolean getBoolean(String value, boolean fallBack) {
        if (value == null)
            return fallBack;
        // do not use "Boolean.valueOf(" as this returns false on everything
        // but "true"
        if ("true".equalsIgnoreCase(value))
            return true;
        if ("false".equalsIgnoreCase(value))
            return false;
        return fallBack;
    }
}
