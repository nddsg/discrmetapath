package edu.nd.dsg.wiki.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashSet;

public class TypeFinderTest {
    @Test
    public void testGetTypeVector() throws Exception {
        LinkedHashSet<Integer> typeVector = TypeFinder.getTypeVector(12);
        Assert.assertEquals(6177, typeVector.size());
        typeVector = TypeFinder.getTypeVector("1921");
        Assert.assertEquals(6399, typeVector.size());
        Assert.assertEquals(0, TypeFinder.getTypeVector(1921, typeVector).size());
    }
}
