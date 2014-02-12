package edu.nd.dsg.wiki.util;


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

public class WordCounterTest {
    @Test
    public void testGetWordCount() throws Exception {
        String testStr = " Atwan also noted, regarding the collapse of the U.S., \"If this sounds far-fetched, it is sobering to consider that this virtually describes the downfall of the Soviet Union.\"\n" +
                "NameIn Arabic, al-Qaeda has four syllables ({{transl}}, {{IPA-ar}} or {{IPA-ar}}). However, since two of the Arabic consonants in the name (the voiceless uvular plosive {{IPA}} and the voiced pharyngeal fricative {{IPA}}) are not phones found in the English language, the closest naturalized English pronunciations include {{IPAc-en}}, {{IPAc-en}} and {{IPAc-en}}.{{Citation needed}} al-Qaeda's name can also be transliterated as al-Qaida, al-Qa'ida, el-Qaida, or al-Qaeda.Listen to the U.S. pronunciation (RealPlayer).The name comes from the Arabic noun qā'idah, which means foundation or basis, and can also refer to a military base. The initial al- is the Arabic definite article the, hence the base.Arabic Computer Dictionary: English-Arabic, Arabic-English By Ernest Kay, Multi-lingual International Publishers, 1986.Bin Laden explained the origin of the term in a videotaped interview with Al Jazeera journalist Tayseer Alouni in October 2001:";
        HashMap<String, Integer> wordmap = WordCounter.getWordCount(testStr);
        Assert.assertEquals(3, wordmap.get("to").intValue());
        Assert.assertEquals(false, wordmap.containsKey(""));
    }

    @Ignore
    public void testSaveTFToDB() throws Exception {
        HashMap<String, Integer> termMap = new HashMap<String, Integer>();
        termMap.put("Al\"oha", 998);
        termMap.put("nothing", 1);
        WordCounter wordCounter = WordCounter.getInstance();
        Assert.assertEquals(1, wordCounter.saveTFToDB(0, 998, termMap));
    }
}

