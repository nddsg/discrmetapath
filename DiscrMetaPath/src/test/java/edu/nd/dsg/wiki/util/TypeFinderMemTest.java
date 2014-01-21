package edu.nd.dsg.wiki.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.LinkedHashSet;

public class TypeFinderMemTest {

    private TypeFinderMem typeFinderMem;
    private TypeFinderSQL typeFinderSQL;

    @Before
    public void setUp() throws Exception {
        typeFinderMem = TypeFinderMem.getInstance();
        typeFinderSQL = TypeFinderSQL.getInstance();
    }

    @Test
    public void testGetInstance() throws Exception {
        Assert.assertEquals(70680147, typeFinderMem.size());
    }

    @Test
    public void testgetTypeVector() throws Exception {
        LinkedHashSet<Integer> typeVector = typeFinderMem.getTypeVector(12);
        LinkedHashSet<Integer> typeVectorSQL = typeFinderSQL.getTypeVector(12);
        Assert.assertArrayEquals(typeVectorSQL.toArray(), typeVector.toArray());
        Assert.assertEquals(6177, typeVector.size());
        typeVector = typeFinderMem.getTypeVector("1921");
        Assert.assertEquals(6399, typeVector.size());
        Assert.assertEquals(0, typeFinderMem.getTypeVector(1921, typeVector).size());
        Assert.assertEquals(typeFinderMem, TypeFinderSQL.getInstance());
    }

}
