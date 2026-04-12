package net.nvrams.mapping;

import java.io.File;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.pinemhi.NVRamPinemhiParser;

class NVRamParserTest {

  @Test
  public void compareNV() throws Exception {

    String rom = "afm_113b";

    File testFolder = new File("nvrams");
    File entry = new File(testFolder, rom + ".nv");

    Locale loc = Locale.ENGLISH;

    NVRamPinemhiParser pinemhi = new NVRamPinemhiParser();
    //List<Score> scoresPinemhi = pinemhi.parseNvRam(entry, loc);
    String rawPinemhi = pinemhi.getRaw(entry, loc);
    System.out.println(rawPinemhi);

    NVRamMapParser parser = new NVRamMapParser();
    //List<Score> scoresMap = parser.parseNvRam(entry, loc);
    String rawMap = parser.getRaw(entry, loc);
    System.out.println(rawMap);

    //assertEquals(rawPinemhi, rawMap);
  }

}
