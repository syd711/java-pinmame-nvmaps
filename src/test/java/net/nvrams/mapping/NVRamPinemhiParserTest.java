package net.nvrams.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import net.nvrams.mapping.pinemhi.NVRamPinemhiParser;

/**
 */
public class NVRamPinemhiParserTest {

  @Test
  public void testPinemhiExtract() throws IOException {
    NVRamPinemhiParser parser = new NVRamPinemhiParser();

    String rom = "bcats_l5";
    File nvram = new File("nvrams", rom + ".nv");

    List<String> lines = parser.executePINemHi(nvram);
    assertEquals(5, lines.size());
  }


  @Test
  public void testPinemhiScores() throws IOException {
    NVRamPinemhiParser parser = new NVRamPinemhiParser();

    String rom = "bcats_l5";
    File nvram = new File("nvrams", rom + ".nv");

    List<NVRamScore> scores = parser.parseNvRam(rom, nvram, Locale.ENGLISH, false);
    assertEquals(4, scores.size());
    for (NVRamScore sc : scores) {
      System.out.println(sc.toString());
    }
  }
}
