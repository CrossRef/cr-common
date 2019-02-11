package org.crossref.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joe.aparo
 */


public class EncodeUtilsTest {
    @Test
    public void encodingShouldPass_withSimpleCheck() {
        String testStr = "String to encode";
        
        String encodedStr = EncodeUtils.urlEncode(testStr);
        
        Assert.assertTrue(encodedStr != null && !encodedStr.equals(testStr));
        
        String decodedStr = EncodeUtils.urlDecode(encodedStr);
        
        Assert.assertTrue(decodedStr != null && decodedStr.equals(testStr));
        
    }
}
