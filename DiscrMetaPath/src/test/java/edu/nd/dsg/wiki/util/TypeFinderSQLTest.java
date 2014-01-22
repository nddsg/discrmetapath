package edu.nd.dsg.wiki.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashSet;

public class TypeFinderSQLTest {
    @Test
    public void testGetTypeVector() throws Exception {
        TypeFinderSQL typeFinderSQL = TypeFinderSQL.getInstance();
        LinkedHashSet<Integer> typeVector = typeFinderSQL.getTypeVector(12);
        Assert.assertEquals(6177, typeVector.size());
        typeVector = typeFinderSQL.getTypeVector("1921");
        Assert.assertEquals(6399, typeVector.size());
        Assert.assertEquals(0, typeFinderSQL.getTypeVector(1921, typeVector).size());
        Assert.assertEquals(typeFinderSQL, TypeFinderSQL.getInstance());
    }
}
