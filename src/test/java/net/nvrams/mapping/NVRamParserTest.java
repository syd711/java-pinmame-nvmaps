package net.nvrams.mapping;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.pinemhi.NVRamPinemhiParser;

class NVRamParserTest {

  @Test
  public void compareNVs() throws Exception {

    Locale loc = Locale.ENGLISH;

    NVRamParser pinemhi = new NVRamPinemhiParser();
    List<String> pinemhiSupported = pinemhi.getSupportedNVRams();
    NVRamParser parser = new NVRamMapParser();
    List<String> mapSupported = parser.getSupportedNVRams();

    int nbErrors = 0;

    // used in manual testing to skip first roms, should be null normally 
    String firstRom = "taf_l7";

    File testFolder = new File("nvrams");
    for (File entry : testFolder.listFiles()) {
      String rom = entry.getName().replace(".nv", "").toLowerCase();

      if ((firstRom==null || firstRom.compareTo(rom) <= 0) && pinemhiSupported.contains(rom) && mapSupported.contains(rom)) {
        System.out.println("Checking " + rom);

        List<Score> scoresPinemhi = pinemhi.parseNvRam(entry, loc, false);
        String rawPinemhi = pinemhi.getRaw(entry, loc);

        List<Score> scoresMap = parser.parseNvRam(entry, loc, false);
        String rawMap = parser.getRaw(entry, loc);

        if (!checkScores(scoresPinemhi, rawPinemhi, scoresMap, rawMap, false)) {
          System.out.println(" => Error");
          System.out.println(" ---------------------------");
          nbErrors++;
        }
      }
    }
    assertEquals(0, nbErrors);
  }

  @Test
  public void compareNV() throws Exception {

    String rom = "afm_113b";
    /*
    FIXME These roms need to be fixed
    rom = "cueball3";
    rom = "drac_l1";
    rom = "godzilla";
    rom = "kissc";
    rom = "lostspc";
    rom = "monopoly";
    rom = "mtl_180h";
    rom = "pool_l7";
    rom = "sc_091";
    rom = "strlt_l1";
    rom = "sttng_l7";
    rom = "tf_180";
    rom = "tmnt_104";
    rom = "trek_201";
    */

    File testFolder = new File("nvrams");
    File entry = new File(testFolder, rom + ".nv");

    Locale loc = Locale.ENGLISH;

    NVRamParser pinemhi = new NVRamPinemhiParser();
    List<Score> scoresPinemhi = pinemhi.parseNvRam(entry, loc, false);
    String rawPinemhi = pinemhi.getRaw(entry, loc);

    NVRamParser parser = new NVRamMapParser();
    List<Score> scoresMap = parser.parseNvRam(entry, loc, false);
    String rawMap = parser.getRaw(entry, loc);

    checkScores(scoresPinemhi, rawPinemhi, scoresMap, rawMap, true);
  }

  private boolean checkScores(List<Score> scoresPinemhi, String rawPinemhi, List<Score> scoresMap, String rawMap, boolean useAssert) {
    if (scoresPinemhi.size() == scoresMap.size()) {
      for (int i = 0; i < scoresPinemhi.size(); i++) {
        Score scorePinemhi = scoresPinemhi.get(i);
        Score scoreMap = scoresMap.get(i);
        String sc1 = scorePinemhi.getPlayerInitials() + " " + scorePinemhi.getScore();
        String sc2 = scoreMap.getPlayerInitials() + " " + scoreMap.getScore();
        if (useAssert) {
          assertEquals(sc1, sc2);
        }
        else if (!StringUtils.equals(sc1, sc2)) {
          System.out.println(sc1 + " != " + sc2);
          return false;
        }

      }
    }
    else {
      System.out.println(rawPinemhi);
      System.out.println(rawMap);
      if (useAssert) {
        assertEquals(scoresPinemhi.size(), scoresMap.size());
      }
      return false;
    }
    return true;
  }
}
