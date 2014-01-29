package edu.nd.dsg.wiki.util;

import org.junit.Assert;
import org.junit.Test;

public class PageLoaderTest {
    @Test
    public void testGetInstance() throws Exception {
        PageLoader instance = PageLoader.getInstance();
        Assert.assertEquals(PageLoader.getInstance(), instance);
    }

    @Test
    public void testGetPageTextById() throws Exception {
        PageLoader pageLoader = PageLoader.getInstance();
        Assert.assertEquals("#REDIRECT [[Computer accessibility]] {{R from CamelCase}}", pageLoader.getPageTextById(10));
    }
}
