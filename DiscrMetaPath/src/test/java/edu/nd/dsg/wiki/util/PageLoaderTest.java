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
        Assert.assertEquals("", pageLoader.getPagePlainTextById(10));
        String line76 = ("From 1986, MAK began to set up a network of recruiting offices in the U.S., the hub of which was the Al Kifah Refugee Center at the Farouq Mosque on Brooklyn's Atlantic Avenue. Among notable figures at the Brooklyn center were \"double agent\" Ali Mohamed, whom FBI special agent Jack Cloonan called \"bin Laden's first trainer,\"Cloonan Frontline interview, PBS, July 13, 2005. and \"Blind Sheikh\" Omar Abdel-Rahman, a leading recruiter of mujahideen for Afghanistan. Al-Qaeda evolved from MAK.Azzam and bin Laden began to establish camps in Afghanistan in 1987.{{Harvnb}}.U.S. government financial support for the Afghan Islamic militants was substantial. Aid to Gulbuddin Hekmatyar, an Afghan mujahideen leader. and founder and leader of the Hezb-e Islami radical Islamic militant faction, alone amounted \"by the most conservative estimates\" to $600&nbsp;million. Later, in the early 1990s, after the U.S. had withdrawn support, Hekmatyar \"worked closely\" with bin Laden.Bergen, Peter L., Holy war, Inc.: Inside the Secret World of Osama bin Laden, New York: Free Press, c2001., p.70-1 In addition to receiving hundreds of millions of dollars in American aid, Hekmatyar was the recipient of the lion's share of Saudi aid.Bergen, Peter L., Holy war, Inc.: inside the secret world of Osama bin Laden, New York: Free Press, c2001., p. 69 There is evidence that the CIA supported Hekmatyar's drug trade activities by giving him immunity for his opium trafficking, which financed the operation of his militant faction.Interview with Alfred McCoy, November 9, 1991 by Paul DeRienzo[1]MAK and foreign mujahideen volunteers, or \"Afghan Arabs,\" did not play a major role in the war. While over 250,000 Afghan mujahideen fought the Soviets and the communist Afghan government, it is estimated that were never more than 2,000 foreign mujahideen in the field at any one time.{{Harvnb}}. Nonetheless, foreign mujahideen volunteers came from 43 countries, and the total number that participated in the Afghan movement between 1982 and 1992 is reported to have been 35,000.{{cite web}} Bin Laden played a central role in organizing training camps for the foreign Muslim volunteers.\"Who Is Osama Bin Laden?\". Forbes. September 14, 2001.\"Frankenstein the CIA created\". January 17, 1999. The Guardian.The Soviet Union finally withdrew from Afghanistan in 1989. To the surprise of many, Mohammad Najibullah's communist Afghan government hung on for three more years, before being overrun by elements of the mujahideen. With mujahideen leaders unable to agree on a structure for governance, chaos ensued, with constantly reorganizing alliances fighting for control of ill-defined territories, leaving the country devastated.");

        Assert.assertEquals(line76, pageLoader.getPagePlainTextById(1921).split("\n")[75]);
    }
}
