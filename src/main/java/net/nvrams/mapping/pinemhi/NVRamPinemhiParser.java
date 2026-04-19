package net.nvrams.mapping.pinemhi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import net.nvrams.mapping.RawScoreParser;
import net.nvrams.mapping.RawScoreParserConf;
import net.nvrams.mapping.NVRamParser;
import net.nvrams.mapping.NVRamScore;
import net.nvrams.mapping.pinemhi.adapters.AlteringLinesWithoutPosAdapter;
import net.nvrams.mapping.pinemhi.adapters.Anonymous5PlayerScoreAdapter;
import net.nvrams.mapping.pinemhi.adapters.FixTitleScoreAdapter;
import net.nvrams.mapping.pinemhi.adapters.FourColumnScoreAdapter;
import net.nvrams.mapping.pinemhi.adapters.MultiBlockAdapter;
import net.nvrams.mapping.pinemhi.adapters.ScoreNvRamAdapter;
import net.nvrams.mapping.pinemhi.adapters.SkipFirstListScoreAdapter;

public class NVRamPinemhiParser implements NVRamParser {
  private final static Logger LOG = LoggerFactory.getLogger(NVRamPinemhiParser.class);
  
  public String pinemhiFolder = "resources/pinemhi";
  private File vpPathAdjusted = null;
  private Set<String> supportedNvRams = new HashSet<>();

  private final RawScoreParser defaultAdapter;

  private final List<ScoreNvRamAdapter> adapters = new ArrayList<>();

  public NVRamPinemhiParser() {
    this(RawScoreParserConf.createParser());
  }

  public NVRamPinemhiParser(List<String> titles, List<String> romsSkipTitlesCheck) {
    this(new RawScoreParser(titles, romsSkipTitlesCheck));
  }

  private NVRamPinemhiParser(RawScoreParser rawParser) {
    this.defaultAdapter = rawParser;

    //adapters.add(new SinglePlayerScoreAdapter("algar_l1.nv", 1));
    //adapters.add(new SinglePlayerScoreAdapter("alienstr.nv", 1));
    //adapters.add(new SinglePlayerScoreAdapter("alpok_b6.nv", 1));
    adapters.add(new FourColumnScoreAdapter("monopoly.nv"));
    adapters.add(new SkipFirstListScoreAdapter("godzilla.nv"));
    //adapters.add(new NewLineAfterFirstScoreAdapter("kiko_a10.nv"));
    adapters.add(new Anonymous5PlayerScoreAdapter("punchy.nv"));
    adapters.add(new FixTitleScoreAdapter("rs_l6.nv", "TODAY'S HIGHEST SCORES", "ALL TIME HIGHEST SCORES"));
    //adapters.add(new SinglePlayerScoreAdapter());
    adapters.add(new MultiBlockAdapter("pool_l7.nv", 8));
    adapters.add(new AlteringLinesWithoutPosAdapter("wrldtou2.nv", 5));

    //force the same folder structure as for the Studio Server
    File commandFile = new File(pinemhiFolder, "PINemHi.exe");
    List<String> commands = Arrays.asList("cmd.exe", "/c", commandFile.getName(), "-lr");

    try {
      List<String> roms = execute(commands, commandFile.getParentFile());
      this.supportedNvRams.addAll(roms);
    }
    catch (IOException ioe) {
      throw new RuntimeException("Cannot load pinemhi supported roms");
    }
  }

  //@Override
  public Set<String> getSupportedRoms() {
    return supportedNvRams;
  }

  @Override
  public boolean isSupportedRom(String rom) {
    return supportedNvRams.contains(rom);
  }

  @Nullable
  @Override
  public List<NVRamScore> parseNvRam(String rom, @NonNull File nvRam, Locale locale, boolean parseAll) throws IOException {
    List<String> lines = getLines(nvRam);
    return defaultAdapter.getScores(rom, lines, parseAll);
  }

  @Override
  public List<String> getRaw(String rom, @NonNull File nvRam, Locale locale) throws IOException {
    return getLines(nvRam);
  }

  @Override
  public List<NVRamScore> parseRaw(String rom, List<String> lines, Locale locale, boolean parseAll) throws IOException {
    return defaultAdapter.getScores(rom, lines, parseAll);
  }


  private List<String> getLines(File nvRam) throws IOException {
    File originalNVRamFile = nvRam;
    String nvRamFileName = nvRam.getCanonicalFile().getName().toLowerCase();
    String nvRamName = FilenameUtils.getBaseName(nvRamFileName).toLowerCase();
    if (nvRamFileName.contains(" ")) {
      LOG.info("Stripping NV offset from nvram file \"{}\" to check if supported.", nvRamFileName);
      nvRamName = nvRamFileName.substring(0, nvRamFileName.indexOf(" "));

      //rename the original nvram file so that we can parse with the original name
      originalNVRamFile = new File(nvRam.getParentFile(), nvRamName + ".nv");
    }

    List<String> lines = executePINemHi(originalNVRamFile);

    for (ScoreNvRamAdapter adapter : adapters) {
      if (adapter.isApplicable(nvRamFileName, lines)) {
        LOG.info("Converted score using {}", adapter.getClass().getSimpleName());
        return adapter.convert(nvRamFileName, lines);
      }
    }
    return lines;
  }

  @Nullable
  public List<String> executePINemHi(@NonNull File originalNVRamFile) throws IOException {
    File commandFile = new File(pinemhiFolder, "PINemHi.exe");

    // make sure nvram can be found
    adjustVPPathForEmulator(originalNVRamFile.getParentFile(), true);

    String nvRamName = originalNVRamFile.getName().toLowerCase();
    List<String> commands = Arrays.asList("cmd.exe", "/c", commandFile.getName(), nvRamName);

    return execute(commands, commandFile.getParentFile());
  }

  private List<String> execute(List<String> commands, File dir) throws IOException {
    List<String> lines = new ArrayList<>();
    try {
      ProcessBuilder pb = new ProcessBuilder(commands)
          .directory(dir)
          .redirectErrorStream(true);
      Process process = pb.start();

      InputStream stdOut = process.getInputStream();

      try (BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(stdOut))) {
        String line;
        while ((line = stdOutReader.readLine()) != null) {
          lines.add(line);
        }
      }
      process.waitFor();
    }
    catch (InterruptedException ie) {
      LOG.error("Process interrupted", ie);
    }
    return lines;
  }

  /**
   * Set the path to the nvRamFolder so that nv files can be found
   * Load pinhemi.ini, update the VP path with the provided folder
   * For optimization, do it only if the cached folder is different
   */
  private void adjustVPPathForEmulator(File nvRamFolder, boolean forcePath) {
    if (vpPathAdjusted != null && vpPathAdjusted.equals(nvRamFolder)) {
      return;
    }
    if (nvRamFolder.exists()) {
      try {
        File pinemhiIni = new File(pinemhiFolder, "pinemhi.ini");
        INIConfiguration iniConfiguration = loadIni(pinemhiIni);
        String vpPath = (String) iniConfiguration.getSection("paths").getProperty("VP");
        File vp = new File(vpPath);

        if (forcePath || !vp.exists() || !vpPath.endsWith("/")) {
          vp = new File(nvRamFolder.getAbsolutePath());
          iniConfiguration.getSection("paths").setProperty("VP", vp.getAbsolutePath().replaceAll("\\\\", "/") + "/");

          saveIni(pinemhiIni, iniConfiguration);
          LOG.info("Changed VP path to {}", vp.getAbsolutePath());
        }

        // cache latest adjusted path for optimisation
        vpPathAdjusted = nvRamFolder;
      }
      catch (Exception e) {
        LOG.error("Failed to update VP path in pinemhi.ini: {}", e.getMessage(), e);
      }
    }
  }

  private static void saveIni(File ini, INIConfiguration iniConfiguration) throws IOException, ConfigurationException {
    try (FileWriter fileWriter = new FileWriter(ini)) {
      iniConfiguration.write(fileWriter);
    }
  }

  private static INIConfiguration loadIni(File ini) throws IOException, ConfigurationException {
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput("=");
    iniConfiguration.setSeparatorUsedInInput("=");

    try (FileReader fileReader = new FileReader(ini)) {
      iniConfiguration.read(fileReader);
    }
    return iniConfiguration;
  }
} 