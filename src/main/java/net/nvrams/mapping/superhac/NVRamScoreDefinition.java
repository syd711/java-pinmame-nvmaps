package net.nvrams.mapping.superhac;

import java.util.Iterator;
import java.util.Locale;

import net.nvrams.mapping.NVRamScore;

public interface NVRamScoreDefinition {

  NVRamScore getScore(Iterator<String> lines, String title, Locale locale);

  NVRamScore getScore(byte[] data, String title, Locale locale, boolean oneBased, Integer zeroByte, Integer zeroIfGte);

}
