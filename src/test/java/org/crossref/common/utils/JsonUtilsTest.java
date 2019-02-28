package org.crossref.common.utils;

import org.json.JSONArray;
import org.json.JSONException;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joe.aparo
 */
public class JsonUtilsTest {
    
   @Test
   public void shouldNotBeAJsonArray_ifKindOfLooksLikeOne() {
       String test1 = "[1.]The rest of my string";
       
       try {
           JSONArray arr = JsonUtils.createJSONArray(test1);
           Assert.assertTrue(false);
       } catch (JSONException ex) {
           // Fails as expected
       }
   }
   
    @Test
    public void shouldBeAJsonArray_ifIsOne() {
       String test1 = "[1.]";
       
       try {
           JSONArray arr = JsonUtils.createJSONArray(test1);
       } catch (JSONException ex) {
           Assert.assertTrue(false);
       }
   }
}