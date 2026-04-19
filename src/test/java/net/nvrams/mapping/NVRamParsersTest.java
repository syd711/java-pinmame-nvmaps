package net.nvrams.mapping;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.pinemhi.NVRamPinemhiParser;
import net.nvrams.mapping.superhac.NVRamSuperhacParser;

/**
 * A test of NVRamParsers
 * It takes a NVRam, generate a raw and parse it with the DefaultAdapter
 * 
 */
class NVRamParsersTest {

  private RawScoreParser rawParser = RawScoreParserConf.createParser();

  private NVRamParser mapParser = new NVRamMapParser(new File("C:\\Github\\pinmame-nvram-maps"));
  private NVRamParser superhacParser = new NVRamSuperhacParser();
  private NVRamParser pinemhiParser = new NVRamPinemhiParser();


  @Test
  public void compareNVsWithSuperhacParser() throws Exception {
    doCompareNVs(superhacParser);
  }

  @Test
  public void compareNVsWithMapParser() throws Exception {
    doCompareNVs(mapParser);
  }

  @Test
  public void compareNVsWithPinemhiParser() throws Exception {
    doCompareNVs(pinemhiParser);
  }

  private void doCompareNVs(NVRamParser parser) throws Exception {

    Locale loc = Locale.ENGLISH;

    int nbErrors = 0;
    // used in manual testing to skip first roms, should be null normally 
    String firstRom = null;
    boolean parseAll = false;

    File testFolder = new File("nvrams");
    for (File entry : testFolder.listFiles()) {
      String rom = entry.getName().replace(".nv", "").toLowerCase();

      if ((firstRom==null || firstRom.compareTo(rom) <= 0) && parser.isSupportedRom(rom)) {

        List<NVRamScore> scoresMap = parser.parseNvRam(rom, entry, loc, parseAll);

        List<String> raw = parser.getRaw(rom, entry, loc);
        List<NVRamScore> scores = rawParser.getScores(rom, raw, parseAll);

        if (!checkScores(rom, raw, scoresMap, scores, false)) {
          nbErrors++;
        }
      }
    }
    assertEquals(0, nbErrors);
  }

  @Test
  public void compareNV() throws Exception {

    NVRamParser parser =  pinemhiParser;

    String rom = "attila";
    boolean parseAll = false;
    /*
    //----------------------------
    NVRAM PARSER ISSUES

    tagteam, tagteam2 => issue with mode_champion
    tf_180
    monopoly,godzilla

    //----------------------------
    SUPERHAC PARSER ISSUES

    cv_20h => find a way to skip CANNON BALL CHAMPION
    fs_lx5 => find a way to skip TOP BOWLER
    gi_l9 => decalage ?
    gnr_300
    jupk_513
    lw3_208

    jd_17

    sttng_l7 => buyin flag qui desactive les autres scores
    stwr_a14 => ne prend que les 4 premiers scores car des sections différentes, utilise 1 section et des labels ? 
    trek_201 => decalage offset sur names
    trn_174h => decalage sur names + scores
    twd_160h => decalage sur names + scores
    tf_180 => complex, autobot and decepticon conflict
    twst_405 => decalage
    viprsega => decalage
    viper => score sur 9 vs 6 sur pinemhi
    waterwld, wcsoccer, whirl_l3, wipeout => errro when parsing scores, possiblement un decalage de +1 sur name offsets and score offsets

    => group HIGH SCORE as one section :
    acd_170h, avr_200,  avs_170, bbh_170, bdk_294,fs_lx5 
    */

    File testFolder = new File("nvrams");
    File entry = new File(testFolder, rom + ".nv");

    Locale loc = Locale.ENGLISH;

    // Get scores from the map and nv directly (except pinemhi, does not use raw string)
    List<NVRamScore> scoresMap = parser.parseNvRam(rom, entry, loc, parseAll);

    // generate the raw version of scores
    List<String> raw = parser.getRaw(rom, entry, loc);

    // parse raw with generic RawScoreParser
    List<NVRamScore> scores = rawParser.getScores(rom, raw, parseAll);
    checkScores(rom, raw, scoresMap, scores, true);

    // parse raw with the parser using the map (for pinemhi, this is exactly same as above, so bypass)
    if (parser != pinemhiParser) {
      List<NVRamScore> scores2 = parser.parseRaw(rom, raw, loc, parseAll);
      checkScores(rom, raw, scoresMap, scores2, true);
    }
  }

  private boolean checkScores(String rom, List<String> raw, List<NVRamScore> parsedScores, List<NVRamScore> scores, boolean useAssert) {
    System.out.println("Checking "+ rom);

    if (parsedScores.size() == scores.size()) {
      for (int i = 0; i < parsedScores.size(); i++) {
        NVRamScore scorePinemhi = parsedScores.get(i);
        NVRamScore scoreMap = scores.get(i);
        if (!scoreMap.equals(scorePinemhi)) {
          System.out.println(String.join("\n", raw));
          if (useAssert) {
            assertEquals(scorePinemhi, scoreMap);
          }
          else {
            System.out.println("==> " + scorePinemhi + " != " + scoreMap);
            return false;
          }
        }
      }
    }
    else {
      System.out.println("Checking "+ rom);
      System.out.println(String.join("\n", raw));
      if (useAssert) {
        assertEquals(parsedScores.size(), scores.size());
      }
      System.out.println("==> " + parsedScores.size() + " != " + scores.size());
      return false;
    }

    return true;
  }
}
