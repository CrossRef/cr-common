package org.crossref.common.utils;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joe.aparo
 */
public class ResourceUtilsTest {
    
   @Test
   public void shouldFindResources_whenAnyExist() {
       File[] files = ResourceUtils.getResourceFolderFiles("");
       
       Assert.assertTrue(files != null && files.length > 0);
   }
}
