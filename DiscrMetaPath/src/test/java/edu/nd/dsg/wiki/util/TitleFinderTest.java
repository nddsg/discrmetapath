package edu.nd.dsg.wiki.util;

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;

public class TitleFinderTest {
    @Test
    public void testGetInstance() throws Exception {
        TitleFinder titleFinder = TitleFinder.getInstance();
        TitleFinder titleFinder1 = TitleFinder.getInstance();
        Assert.assertEquals(titleFinder, titleFinder1);
    }

    @Test
    public void testGetTitle() throws Exception {
        TitleFinder titleFinder = TitleFinder.getInstance();
        LinkedList<Integer> integers = new LinkedList<Integer>();
        integers.add(10);
        integers.add(12);
        HashMap<Integer, String> titleMap = titleFinder.getTitle(integers);

        Assert.assertTrue(titleMap.containsKey(10));
        Assert.assertTrue(titleMap.containsKey(12));
        Assert.assertEquals("AccessibleComputing", titleMap.get(10));
        Assert.assertEquals("Anarchism", titleMap.get(12));
    }
}
