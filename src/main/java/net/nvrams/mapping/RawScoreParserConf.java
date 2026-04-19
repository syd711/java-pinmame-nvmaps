package net.nvrams.mapping;

import java.util.List;

/**
 * Copied from VpinStudio to test 
 */
public class RawScoreParserConf {

  public static final List<String> TITLES = List.of(
    "MASTER MAGICIAN", "CHAMPION", "GRAND CHAMPION", "WORLD RECORD", "GREATEST VAMPIRE HUNTER", "GREATEST TIME LORD", 
    "RIVER MASTER", "CLUB CHAMPION", "HIGHEST SCORES", "THE BEST DUDE", "ACE WINGER",
    // to be added in ScoringDB.json....
    //               system-rom-20    che_cho            bop_17                     punchy            punchy             ripleys
    "HIGHEST SCORE", "HIGH SCORE" ,"ROAD-TRIP KING", "BILLIONAIRE CLUB MEMBERS", "MY BEST FRIEND", "MY OTHER FRIENDS", "GRAND CHAMP"
  );

  public static final List<String> SKIP_TITLE_CHECK = List.of(
    "gnr_300", "jupk_513", "jupk_600", "pool_l7", "trek_201"
  );

  public static RawScoreParser createParser() {
    return new RawScoreParser(TITLES, SKIP_TITLE_CHECK);
  }

}