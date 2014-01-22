package edu.nd.dsg.wiki.util;

import org.junit.Assert;
import org.junit.Test;

public class WikiPathSetTest {
    @Test
    public void testPutPath() throws Exception {
        WikiPathSet wikiPathSet = new WikiPathSet(24605831, 30625677);

        wikiPathSet.putPath("24605831->30625677->");
        wikiPathSet.putPath("24605831->2776748->30625677->");
        wikiPathSet.putPath("24605831->885435->30625677->");
        wikiPathSet.putPath("24605831->23391454->30625677->");
        wikiPathSet.putPath("24605831->30678734->30625677->");
        wikiPathSet.putPath("24605831->11677564->30625677->");
        wikiPathSet.putPath("24605831->4277750->30625677->");
        wikiPathSet.putPath("24605831->24624498->30625677->");
        wikiPathSet.putPath("24605831->24677558->30625677->");
        wikiPathSet.putPath("24605831->18409568->30625677->");
        wikiPathSet.putPath("24605831->24916163->30625677->");
        wikiPathSet.putPath("24605831->885351->30625677->");
        wikiPathSet.putPath("24605831->26037801->30625677->");
        wikiPathSet.putPath("24605831->28244893->30625677->");
        wikiPathSet.putPath("24605831->13914518->30625677->");
        wikiPathSet.putPath("24605831->23384577->30625677->");
        wikiPathSet.putPath("24605831->38326490->30625677->");
        wikiPathSet.putPath("24605831->7874936->30625677->");
        wikiPathSet.putPath("24605831->15945356->30625677->");
        wikiPathSet.putPath("24605831->24909705->30625677->");
        wikiPathSet.putPath("24605831->22670058->30625677->");
        wikiPathSet.putPath("24605831->8421646->30625677->");
        wikiPathSet.putPath("24605831->21264572->30625677->");
        wikiPathSet.putPath("24605831->23395648->30625677->");
        wikiPathSet.putPath("24605831->22540554->30625677->");

        wikiPathSet.putSibling("828338->31717->9316->192713");
        wikiPathSet.putSibling("828338->1485389->9316->192713");
        wikiPathSet.putSibling("828338->350215->9316->192713");
        wikiPathSet.putSibling("1358651->266910->16459786->34791858");
        wikiPathSet.putSibling("1358651->20206->618066->182881");
        wikiPathSet.putSibling("1358651->266910->9626808->182881");
        wikiPathSet.putSibling("1358651->20206->32131169->182881");
        wikiPathSet.putSibling("1358651->266910->81066->1510480");
        wikiPathSet.putSibling("1358651->9602->670465->1928229");
        wikiPathSet.putSibling("1358651->17867->670465->1928229");
        wikiPathSet.putSibling("1358651->9602->17880->1928229");
        wikiPathSet.putSibling("1358651->20206->17880->1928229");
        wikiPathSet.putSibling("1358651->1627->17880->1928229");
        wikiPathSet.putSibling("1358651->20206->16203347->1270682");
        wikiPathSet.putSibling("1358651->266910->16203347->1270682");
        wikiPathSet.putSibling("1358651->9602->18727355->602831");
        wikiPathSet.putSibling("1358651->1627->18727355->602831");
        wikiPathSet.putSibling("1358651->20206->18727355->602831");
        wikiPathSet.putSibling("1358651->9602->14914->602831");
        wikiPathSet.putSibling("1358651->3010272->14914->602831");
        wikiPathSet.putSibling("1358651->20206->14914->602831");
        wikiPathSet.putSibling("1358651->26994->14914->602831");
        wikiPathSet.putSibling("1358651->266910->532187->857713");
        wikiPathSet.putSibling("1358651->266910->5455080->857713");
        wikiPathSet.putSibling("1358651->25679->44070->857713");
        wikiPathSet.putSibling("1358651->20206->30433662->857713");
        wikiPathSet.putSibling("1358651->9602->320278->857713");

        System.out.println(wikiPathSet.getDiscriminativeRate());
        System.out.println(wikiPathSet.getSimilarRate());
        System.out.println(wikiPathSet.getDiscriminativeRateByOrder());
        System.out.println(wikiPathSet.getSimilarRateByOrder());


        Assert.assertEquals(6461, wikiPathSet.getDiscriminativeRate());
        Assert.assertEquals(6567, wikiPathSet.getSimilarRate());
        Assert.assertEquals(39615180, wikiPathSet.getDiscriminativeRateByOrder());
        Assert.assertEquals(254490715320L, wikiPathSet.getSimilarRateByOrder());
    }

    @Test
    public void testEmptyPath() throws Exception{
        WikiPathSet wikiPathSet = new WikiPathSet(24605831, 30625677);
        Assert.assertNull(wikiPathSet.getDiscriminativePath());

        wikiPathSet = new WikiPathSet(24605831, 30625677);
        wikiPathSet.putSibling("828338->31717->9316->192713");
        Assert.assertNull(wikiPathSet.getDiscriminativePath());

        wikiPathSet = new WikiPathSet(24605831, 30625677);
        wikiPathSet.putSibling("24605831->30625677->");
        wikiPathSet.putPath("24605831->2776748->30625677->");
        Assert.assertNotNull(wikiPathSet.getDiscriminativePathByOrder());
    }
}
