package net.nvrams.mapping.tools;

import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.pinemhi.NVRamPinemhiParser;
import net.nvrams.mapping.superhac.NVRamMapSuperhacParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * A Tool that compares NVRam support between pinemHi, superhac and the Nvram-Map project
 */
public class NVRamParserCompareTool {

  public static void main(String[] args) throws IOException {

    File mameFolder = new File("C:/Visual Pinball/VPinMAME");

    Map<String, String> roms = VPXUtil.getRomnames(mameFolder);
    Map<String, String> clones = VPXUtil.getClones(mameFolder);

    NVRamMapParser parser = new NVRamMapParser();
    List<String> supportedNVRams = parser.getSupportedNVRams();

    NVRamPinemhiParser pinemhi = new NVRamPinemhiParser();
    List<String> supportedByPinemhi = pinemhi.getSupportedNVRams();
    
    NVRamMapSuperhacParser superhac = new NVRamMapSuperhacParser();
    List<String> supportedbySuperhac = superhac.getSupportedNVRams();

    File[] testFolders = new File[] { 
      new File("./testsystem/vPinball/VisualPinball/VPinMAME/nvram/"),
      new File("C:/Github/py-pinmame-nvmaps/test/nvram"),
      new File("C:/temp/_NVRAMS/Matt"),
      new File("C:/temp/_NVRAMS/ed209"),
      new File("C:/temp/_NVRAMS/YabbaDabbaDoo"),
      new File("C:/temp/_NVRAMS/gonzonia"),
      new File("C:/temp/_NVRAMS/GerhardK"),
      new File("C:/temp/_NVRAMS/Buffdriver"),
      new File("C:/temp/_NVRAMS/BostonBuckeye"),
      new File("C:/temp/_NVRAMS/FuFu"),
      new File("C:/temp/_NVRAMS/Blap"),
      new File("C:/Visual Pinball/VPinMAME/nvram"),         // OLE
      new File("./resources/nvrams")    // resetted nvrams
    };

    try (PrintWriter w = new PrintWriter("allroms.csv")) {
      w.println("\"rom\",\"Name\",\"clone of\",\"pinemHi\",\"tomslogic\",\"superhac\",\"nvs\"");
      w.println("-------------------------------------");
      for (String s : roms.keySet()) {

        w.print(s + ",");
        w.print(roms.get(s) + ",");

        String cloneOf = clones.get(s);
        w.print((cloneOf != null? cloneOf: "") + ",");

        w.print((supportedByPinemhi.contains(s) ? "x": "") + ",");
        w.print((supportedNVRams.contains(s) ? "x": "") + ",");
        w.print((supportedbySuperhac.contains(s) ? "x": "") + ",");

        String paths = null;
        for (File folder : testFolders) {
          File entry = new File(folder, s + ".nv");
          if (entry.exists()) {
            paths = (paths != null? paths + ", ": "") + entry.getAbsolutePath();
          }
        }
        w.println(paths != null ? paths: "");
      }
    }
  }
}
