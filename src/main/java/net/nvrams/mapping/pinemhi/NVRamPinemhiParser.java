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
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import net.nvrams.mapping.NVRamParser;
import net.nvrams.mapping.Score;

public class NVRamPinemhiParser implements NVRamParser {
  private final static Logger LOG = LoggerFactory.getLogger(NVRamPinemhiParser.class);

  private File vpPathAdjusted = null;


  @Override
  public List<String> getSupportedNVRams() throws IOException {
    File commandFile = new File("tools", "PINemHi.exe");
    List<String> commands = Arrays.asList("cmd.exe", "/c", commandFile.getName(), "-lr");

    return execute(commands, commandFile.getParentFile());
  }

  @Nullable
  @Override
  public List<Score> parseNvRam(@NonNull File nvRam, Locale locale) throws IOException {
    List<String> lines = getLines(nvRam);
    return convertOutputToRaw(lines);
  }

 @Override
  public String getRaw(@NonNull File nvRam, Locale locale) throws IOException {
    List<String> lines = getLines(nvRam);
    StringBuilder bld = new StringBuilder();
    for (String line : lines) {
      bld.append(line).append("\n");
    }
    return bld.toString();
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
    return lines;
  }

  @Nullable
  public List<String> executePINemHi(@NonNull File originalNVRamFile) throws IOException {
      File commandFile = new File("tools", "PINemHi.exe");

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
    catch(InterruptedException ie) {
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
        File pinemhiIni = new File("tools", "pinemhi.ini");
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


  @NonNull
  private List<Score> convertOutputToRaw(List<String> lines) throws IOException {
    try {
      List<Score> scores = new ArrayList<>();

	    String currentTitle = null;
      String currentSuffix = null;
      Score currentScore = null;
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i).trim();
      	if (StringUtils.isEmpty(line)) {
          if (currentSuffix != null && currentScore != null) {
            currentScore.setSuffix(currentSuffix);
          }
        	// restart a possible new sequence
        	currentTitle = null;
          currentSuffix = null;
          currentScore = null;
          continue;
      	}

        if (isScoreLine(line)) {
          currentScore = createScore(currentTitle, line);
          if (currentScore != null) {
            scores.add(currentScore);
          }
        }
        else if (isTitleScoreLine(line)) {
          currentScore = createTitledScore(currentTitle, line);
          if (currentScore != null) {
            scores.add(currentScore);
          }
        }
        else if (StringUtils.isNotEmpty(line)) {
          if (currentScore != null) {
            currentSuffix = " " + line;
          }
          currentTitle = line;
        }
      }
      if (currentSuffix != null && currentScore != null) {
        currentScore.setSuffix(currentSuffix);
      }

      return scores;
    }
    catch (Exception e) {
      LOG.error("Score parsing failed: {}", e.getMessage(), e);
      throw e;
    }
  }

  //-------------------------

  private static final String _patternIndex = "(\\d+\\)|#\\d+|\\d+#|\\d+\\.:) +";
  private static final String _patternScore = "(.{3})?(\\s+-)?(\\s+(\\d\\d?\\d?(?:[., ?\u00a0\u202f\ufffd\u00ff]?\\d\\d\\d)*(\\.\\d)?)((\\s?[a-zA-Z]+)*))+$";

  private static final Pattern patternScoreLine = Pattern.compile(_patternIndex + _patternScore);
  private static final Pattern patternScoreTitle = Pattern.compile(_patternScore);


  public boolean isTitleScoreLine(String line) {
    Matcher m = patternScoreTitle.matcher(line);
    return m.find();
  }

  public boolean isScoreLine(String line) {
    Matcher m = patternScoreLine.matcher(line);
    return m.find();
  }

  /**
   * Parses score that are shown right behind a possible title.
   * These scores do not have a leading position number.
   */
  @Nullable
  protected Score createTitledScore(@Nullable String title, @NonNull String line) {
    Matcher m = patternScoreTitle.matcher(line);
    if (m.find()) {
      String initials = m.group(1);
      if (StringUtils.isEmpty(initials)) {
        initials = "???";
      }

      String scoreString = m.group(4).trim();
      long scoreValue = toNumericScore(scoreString, false);
      if (scoreValue != -1) {
        Score sc = new Score(initials.trim(), scoreString, scoreValue, 1);
        sc.setLabel(title);

        // do not trim and keep spaces at beginning if present
        String suffix = m.group(6);
        if (StringUtils.isNotEmpty(suffix)) {
          sc.setSuffix(suffix);
        }
        return sc;
      }
    }
    return null;
  }

  @Nullable
  public Score createScore(@Nullable String title, @NonNull String line) {
    String idx = StringUtils.substringBefore(line, " ");
    idx = idx.replace(")", "");
    idx = idx.replace("#", "");
    idx = idx.replace(".:", "");
    int index = Integer.parseInt(idx);
    
    line = StringUtils.substringAfter(line, " ");
    Score sc = createTitledScore(title, line);
    sc.setPosition(index);
    return sc;
  }

  protected long toNumericScore(@Nullable String score, boolean log) {
    if (StringUtils.isEmpty(score)) {
      if (log) {
        LOG.warn("Cannot parse empty numeric highscore, ignoring this segment");
      }
      return -1;
    }
    try {
      String cleanScore = cleanScore(score);
      return Long.parseLong(cleanScore);
    }
    catch (NumberFormatException e) {
      if(log) {
        LOG.warn("Failed to parse numeric highscore string '{}', ignoring this segment", score);
      }

      return -1;
    }
  }

  public static String cleanScore(String score) {
    return score
        .replace(".", "")
        .replace(",", "")
        .replace("?", "")
        .replace("\u00ff", "")
        .replace("\u00a0", "")
        .replace("\u202f", "")
        .replace("\ufffd", "")
        .replace(" ", "");
  }
} 