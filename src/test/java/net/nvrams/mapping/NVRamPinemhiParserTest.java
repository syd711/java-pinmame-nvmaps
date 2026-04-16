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

  //----------------------------------------------

  @Test
  public void testParsing() {
    NVRamPinemhiParser adapter = new NVRamPinemhiParser();

    doTest(adapter, "#1", "JOE            250.000.000", 1, "JOE", 250000000);
    doTest(adapter, "#3", "IND        30.000.000", 3, "IND", 30000000);
    doTest(adapter, "#3", "I N        30.000.000", 3, "I N", 30000000);
    doTest(adapter, "#10", "MT             110.000.000", 10, "MT ", 110000000);

    doTest(adapter, "1)", "RA        161.000", 1, "RA ", 161000);
    doTest(adapter, "2)", "P G     1.610.000", 2, "P G", 1610000);
    doTest(adapter, "3)", "X      16.100.000", 3, "X  ", 16100000);
    doTest(adapter, "4)", "TEX   161.000.000", 4, "TEX", 161000000);

    doTest(adapter, "1)", "TEX 16", 1, "TEX", 16);
    doTest(adapter, "1#", "DAD   267", 1, "DAD", 267);

    doTest(adapter, "1)", "4.000.000", 1, "   ", 4000000);

    doTest(adapter, "#1", "???   1.000.000", 1, "???", 1000000);

    //doTest(adapter, "2)", "P G", 2, "P G", 0);
    //doTest(adapter, "3#", "TEX", 3, "TEX", 0);
  }

  private void doTest(NVRamPinemhiParser adapter, String posInput, String input, int rank, String initials, long score) {
    String[] seps = { ".", ",", "\ufffd", ""};
    for (String sep : seps) {
      String line = (posInput + " " + input).replace(".", sep);

      assertTrue(adapter.isScoreLine(line));
      NVRamScore s = adapter.createScore("TEST", line);
      assertEquals(initials, s.getPlayerInitials());
      assertEquals(score, s.getScore());
      assertEquals(rank, s.getPosition());
    }

    for (String sep : seps) {
      String line = (input).replace(".", sep);
      assertTrue(adapter.isTitleScoreLine(line));
      NVRamScore s = adapter.createTitledScore("TEST", line);
      assertEquals(initials, s.getPlayerInitials());
      assertEquals(score, s.getScore());
    }
  }
}
