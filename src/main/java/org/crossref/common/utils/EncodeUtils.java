package org.crossref.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 * Defines encoder related utility calls.
 * 
 * @author joe.aparo
 */
public final class EncodeUtils {
    
    /**
     * URL encode a given string using UTF-8.
     * 
     * @param str The string to encode
     * @return A UTF-encoded string
     */
    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // Just return the given string
            return str;
        }
    }
    
    /**
     * URL decode a given UTF-8 encoded string.
     * 
     * @param str The encoded string
     * @return A decoded string
     */
    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // Just return the given string
            return str;
        }
    }
}
