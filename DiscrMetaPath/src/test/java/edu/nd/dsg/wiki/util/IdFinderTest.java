package edu.nd.dsg.wiki.util;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import junit.framework.Assert;
import org.junit.Test;

public class IdFinderTest {
    @Test
    public void testGetInstance() throws Exception {
        IdFinder idFinder = IdFinder.getInstance();
        IdFinder idFinder1 = IdFinder.getInstance();
        Assert.assertEquals(idFinder, idFinder1);
    }

    @Test
    public void testGetIdByTitle() throws Exception {
        IdFinder idFinder = IdFinder.getInstance();
        Assert.assertEquals(629572, idFinder.getIdByTitle("Population_transfer_in_the_Soviet_Union"));
        Assert.assertEquals(57843, idFinder.getIdByTitle("Trenton,_New_Jersey"));
        Assert.assertEquals(0, idFinder.getIdByTitle("Trenton, New_Jersey"));
    }

    @Test
    public void testGetIdByTitle1() throws Exception {
        IdFinder idFinder = IdFinder.getInstance();
        int[] arry = {629572,57843};
        String[] titleArry = {"Population_transfer_in_the_Soviet_Union","Trenton,_New_Jersey"};
        Assert.assertEquals(arry[0], idFinder.getIdByTitle(titleArry)[0]);
        Assert.assertEquals(arry[1], idFinder.getIdByTitle(titleArry)[1]);
        String[] titleNullArry = {"Population_transfer_in_the_Soviet_Union","Trenton,_New_Jersey", "ddd"};
        Assert.assertEquals(null, idFinder.getIdByTitle(titleNullArry));
    }

    @Test
    public void testGetIdStrByTitle() throws Exception {
        IdFinder idFinder = IdFinder.getInstance();
        int[] arry = {629572,57843};
        String[] titleArry = {"","Population_transfer_in_the_Soviet_Union","","Trenton,_New_Jersey"};
        Assert.assertEquals("629572,57843", idFinder.getIdStrByTitle(titleArry));
    }

}
