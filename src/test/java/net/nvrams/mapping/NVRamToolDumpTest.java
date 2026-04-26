package net.nvrams.mapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.nvrams.mapping.map.NVRamMap;
import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.map.SparseMemory;
import net.nvrams.mapping.tools.NVRamToolDump;

/**
 */
public class NVRamToolDumpTest {

  @Test
  public void testDumpAlpok() throws IOException {
    doTestDump("alpok_l6", "1.000.000\n");
  }

  /** @fixme check how to limit nb of scores as buy-in scores are added at the moment */
  @Test
  public void testDumpAfm() throws IOException {
    doTestDump("afm_113b", "SLL   7.500.000.000\n" +
            "BRE   7.000.000.000\n" +
            "LFS   6.500.000.000\n" +
            "RCF   6.000.000.000\n" +
            "DTW   5.500.000.000\n" +
            "DWF   5.000.000.000\n" +
            "ASR   4.500.000.000\n" +
            "BCM   4.000.000.000\n" +
            "MOO   3.500.000.000\n");
  }


  private void doTestDump(String rom, String expected) throws IOException {
    File mainFolder = new File("nvrams");
    
    File entry = new File(mainFolder, rom + ".nv");
    byte[] bytes = Files.readAllBytes(entry.toPath());

    NVRamMapParser parser = new NVRamMapParser("resources/maps");
    NVRamMap mapJson = parser.getMap(rom);
    SparseMemory memory = parser.getMemory(mapJson, bytes);

    NVRamToolDump dump = new NVRamToolDump();
    //String txt = dump.dump(mapJson, memory, Locale.ENGLISH, true);

    String rawscores = dump.dumpScores(mapJson, memory, Locale.GERMANY, false);

    Assertions.assertEquals(expected.replace("\n", System.lineSeparator()), rawscores);
  }
}
