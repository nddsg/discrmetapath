package edu.nd.dsg.wiki.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;

public class TFCalculatorTest {
    @Test
    public void testGetTF() throws Exception {
        TFCalculator tfCalculator = TFCalculator.getInstance();
        LinkedList<Integer> x = new LinkedList<Integer>(Arrays.asList(24605831, 22540554, 30625677));
        LinkedList<Integer> y = new LinkedList<Integer>(Arrays.asList(24605831, 30678734, 30625677));
        LinkedList<Integer> z = new LinkedList<Integer>(Arrays.asList(24605831, 26037801, 30625677));

        System.out.println(tfCalculator.getTF(x,y));
        System.out.println(tfCalculator.getTF(z,y));

    }
}
