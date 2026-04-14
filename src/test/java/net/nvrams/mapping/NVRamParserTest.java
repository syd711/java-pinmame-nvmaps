package net.nvrams.mapping;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.pinemhi.NVRamPinemhiParser;
import net.nvrams.mapping.superhac.NVRamSuperhacParser;

class NVRamParserTest {

  private boolean useSuperhac = true;


  @Test
  public void compareNVs() throws Exception {

    Locale loc = Locale.ENGLISH;

    NVRamParser pinemhi = new NVRamPinemhiParser();
    List<String> pinemhiSupported = pinemhi.getSupportedNVRams();
    NVRamParser parser = useSuperhac ? new NVRamSuperhacParser() : new NVRamMapParser();
    List<String> mapSupported = parser.getSupportedNVRams();

    int nbErrors = 0;
    // used in manual testing to skip first roms, should be null normally 
    String firstRom = "wipeouu";
    boolean parseAll = false;

    File testFolder = new File("nvrams");
    for (File entry : testFolder.listFiles()) {
      String rom = entry.getName().replace(".nv", "").toLowerCase();

      if ((firstRom==null || firstRom.compareTo(rom) <= 0) && pinemhiSupported.contains(rom) && mapSupported.contains(rom)) {
        System.out.println("Checking " + rom);

        List<Score> scoresPinemhi = pinemhi.parseNvRam(entry, loc, parseAll);
        String rawPinemhi = pinemhi.getRaw(entry, loc);

        List<Score> scoresMap = parser.parseNvRam(entry, loc, parseAll);
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

    String rom = "waterwld";
    boolean parseAll = false;

    /*

waterwld => exception

    FIXME NVRamMapParser These roms need to be fixed
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

    FIXME NVRamSUperhacParser These roms need to be fixed  
    strlt_l1
    ts_lx5
    viprsega
    waterwld !!!!

    FIXME NVRamSUperhacParser fix map
    dollyptb  => "decoder": "single_high_nibble_score_x10"
    jd_l7 => improve pinhemi, problem with REGULAR GAME / HIGH SCORE ON 2 lines
    mm_10    => decoder: bdc  pour TROLL CHAMPION et les autres
    monopoly => specific name_decoder so suffix is in initials when it should be separated, review design + map
    nbaf_31 => very complex ^^
    stingray => decalage de 1 dans offsets
    phnix_l1, scrpn_l1 => à revoir
    cv_20h => find a way to skip CANNON BALL CHAMPION
    fs_lx5 => find a way to skip TOP BOWLER
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
    NVRamParser pinemhi = new NVRamPinemhiParser();
    List<Score> scoresPinemhi = pinemhi.parseNvRam(entry, loc, parseAll);
    String rawPinemhi = pinemhi.getRaw(entry, loc);

    NVRamParser parser = useSuperhac ? new NVRamSuperhacParser() : new NVRamMapParser();
    List<Score> scoresMap = parser.parseNvRam(entry, loc, parseAll);
    String rawMap = parser.getRaw(entry, loc);

    checkScores(scoresPinemhi, rawPinemhi, scoresMap, rawMap, true);
  }

  private boolean checkScores(List<Score> scoresPinemhi, String rawPinemhi, List<Score> scoresMap, String rawMap, boolean useAssert) {
    if (scoresPinemhi.size() == scoresMap.size()) {
      for (int i = 0; i < scoresPinemhi.size(); i++) {
        Score scorePinemhi = scoresPinemhi.get(i);
        Score scoreMap = scoresMap.get(i);
        if (!scoreMap.equals(scorePinemhi)) {
          System.out.println(rawPinemhi);
          System.out.println(rawMap);
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
