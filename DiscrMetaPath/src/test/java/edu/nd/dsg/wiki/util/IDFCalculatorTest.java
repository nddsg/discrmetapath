package edu.nd.dsg.wiki.util;

import org.junit.Assert;
import org.junit.Test;

public class IDFCalculatorTest {

    @Test
    public void testGetIDF() throws Exception {
        IDFSimpleCalculator idfCalculator = IDFSimpleCalculator.getInstance();
        Assert.assertEquals(0.9766404573902129,idfCalculator.getIDF("a"), 0.0000000000000001);
        Assert.assertEquals(1.5941641948617213,idfCalculator.getIDF("an"), 0.0000000000000001);
        Assert.assertEquals(0.947361304243521, idfCalculator.getIDF("the"), 0.0000000000000001);
    }
}
