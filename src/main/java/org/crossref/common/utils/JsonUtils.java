package org.crossref.common.utils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Miscellaneous JSON helper functions.
 * 
 * @author joe.aparo
 */
public class JsonUtils {
    /**
     * Convert a string to a JSON array. Unfortunately, org.json will 
     * convert any string to an array if it starts with a '[' but might look
     * something like this: "[1.]The rest of my string". This will result
     * in an array containing a single double value of 1.0, and the rest
     * of the string is ignored, which can be problematic. This method
     * simply takes a peek at the string before handing to JSONArray and
     * throws a JSONException ex if it does not both begin and end with
     * '[' and ']'.
     * 
     * @param str A string to attempt to convert to an array.
     * @return A JSON Array
     */
    public static JSONArray createJSONArray(String str) {
        
        // Ignore leading/trailing whitespace
        String tmp = str.trim();
        
        // Check for brackets
        if ((tmp.charAt(0) != '[') || (tmp.charAt(tmp.length() -1) != ']')) {
            throw new JSONException("Array string does not begin and end with [ and ]");
        }
        
        // Let JSON do the hard work if we pass the simple test
        return new JSONArray(str);
    }
}
