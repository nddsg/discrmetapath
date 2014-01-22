package edu.nd.dsg.wiki.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class WikiPathTest {

    WikiPath wikiPath;

    private static final int SRC = 21102575;
    private static final int DEST = 47089;

    @Before
    public void setUp() throws Exception {
        wikiPath = new WikiPath(SRC, DEST);
    }

    @Test
    public void testPutPath() throws Exception {
        wikiPath.putPath("21102575->18948365->18589346->47089->");
        Assert.assertEquals(4, wikiPath.getPathLength());
        Assert.assertEquals((long) 18948365, (long) wikiPath.getPath().get(1));
        Assert.assertEquals((long)18589346, (long)wikiPath.getPath().get(2));
        wikiPath.putPath("21102575->18948365->18589346->47089");
        Assert.assertEquals(4, wikiPath.getPathLength());
        Assert.assertEquals((long)18589346, (long)wikiPath.getPath().get(2));
        Assert.assertEquals((long)47089, (long)wikiPath.getPath().get(3));
    }

    @Test
    public void testGetgetOrderedTypeVector() throws Exception {
        wikiPath.putPath("21102575->18948365->18589346->47089->");
        Assert.assertNull(wikiPath.getOrderedTypeVector(4));
    }

    @Test
    public void testGetSrc() throws Exception {
        Assert.assertEquals(SRC, wikiPath.getSrc());
    }

    @Test
    public void testGetDest() throws Exception {
        Assert.assertEquals(DEST, wikiPath.getDest());
    }
}
