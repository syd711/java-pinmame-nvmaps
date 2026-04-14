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

  private List<String> titles = List.of("MASTER MAGICIAN", "CHAMPION", "GRAND CHAMPION", "WORLD RECORD", "GREATEST VAMPIRE HUNTER", "GREATEST TIME LORD", "RIVER MASTER", "CLUB CHAMPION", "HIGHEST SCORES", "HIGHEST SCORE", "THE BEST DUDE", "ACE WINGER",
    // to be added in ScoringDB.json....
    // che_cho            bop_17                     punchy            punchy
       "ROAD-TRIP KING", "BILLIONAIRE CLUB MEMBERS", "MY BEST FRIEND", "MY OTHER FRIENDS"
  );


  @Override
  public List<String> getSupportedNVRams() throws IOException {
    File commandFile = new File("tools", "PINemHi.exe");
    List<String> commands = Arrays.asList("cmd.exe", "/c", commandFile.getName(), "-lr");

    return execute(commands, commandFile.getParentFile());
  }

  @Nullable
  @Override
  public List<Score> parseNvRam(@NonNull File nvRam, Locale locale, boolean parseAll) throws IOException {
    List<String> lines = getLines(nvRam);
    return convertOutputToRaw(lines, parseAll);
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
  private List<Score> convertOutputToRaw(List<String> lines, boolean parseAll) throws IOException {
    try {
      List<Score> scores = new ArrayList<>();

	    String currentTitle = null;
      Score currentScore = null;
      String currentBuffer = null;
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i).trim();
      	if (StringUtils.isEmpty(line)) {
          if (currentBuffer != null) {
            currentScore = createTitledScore(currentTitle, currentBuffer);
          	if (currentScore != null) {
              scores.add(currentScore);
            }
            currentBuffer = null;
          }
          // restart a new sequence
          currentTitle = null;
          currentScore = null;
          if (scores.size() > 3 && !parseAll) {
              break;
          }
          continue;
      	}

        if (currentTitle != null && isScoreLine(line)) {
          currentScore = createScore(currentTitle, line);
          if (currentScore != null) {
            if (currentBuffer != null) {
              // this happens rarely, ex rom jd_l7  REGULAR GAME > HIGH SCORES > scores  ou tf_180
              currentBuffer = null;   
            }
            scores.add(currentScore);
          }
        }
        else if (currentTitle != null && isTitleScoreLine(line)) {
          currentScore = createTitledScore(currentTitle, line);
          if (currentScore != null) {
            if (currentBuffer != null) {
              setScoreOrSuffix(currentScore, currentBuffer);
              currentBuffer = null;
            }
            if (parseAll || titles.contains(currentTitle)) {
              scores.add(currentScore);
            }
          }
        }
        else if (currentTitle != null && StringUtils.isNotEmpty(line)) {
          if (currentScore != null) {
            setScoreOrSuffix(currentScore, line);
          }
          else if (currentBuffer == null) {
            currentBuffer = line;
          } else {
            currentBuffer += " " + line;
          }
        }
        else if (StringUtils.isNotEmpty(line)) {
          currentTitle = line;
        }
      }
      if (currentBuffer != null) {
        currentScore = createTitledScore(currentTitle, currentBuffer);
        if (currentScore != null) {
          scores.add(currentScore);
        }
      }

      return scores;
    }
    catch (Exception e) {
      LOG.error("Score parsing failed: {}", e.getMessage(), e);
      throw e;
    }
  }
  private void setScoreOrSuffix(Score currentScore, String line) {
    if (!currentScore.hasInitials()) {
      currentScore.setPlayerInitials(line);
    }
    else if (currentScore.getScore() == null && currentScore.getScoreText() == null) {
      currentScore.setScoreText(line);
    }
    else if (currentScore.getScore() == null) {
      currentScore.setScoreText(currentScore.getScoreText() + " | " + line);
    }
    else {
      currentScore.setSuffix(line);
    }
  }

  //-------------------------

  private static final String _patternIndex = "(\\d+\\)|\\d+,|#\\d+|\\d+#|\\d+\\.:)";
  private static final String _patternScore = "([ ?a-zA-Z0-9\u0000]{3,}\\s+)?(?:[-|]?\\s+)?(\\d\\d?\\d?(?:[.,?\u00a0\u202f\ufffd\u00ff]?\\d\\d\\d)*(?:\\.\\d)?)((?:\\s\\d+)?[\\-\\sa-zA-Z]*)$";

  private static final Pattern patternScoreLine = Pattern.compile("^" + _patternIndex + _patternScore);
  private static final Pattern patternScoreTitle = Pattern.compile("^" + _patternScore);


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
  public Score createTitledScore(@Nullable String title, @NonNull String line) {
    Matcher m = patternScoreTitle.matcher(line);
    if (m.find()) {
      String initials = StringUtils.trim(m.group(1));
      String scoreString = StringUtils.trim(m.group(2));
      long scoreValue = toNumericScore(scoreString, false);
      if (scoreValue != -1) {
        Score sc = new Score(initials, scoreValue, -1, title);
        sc.setRawScore(line);

        // do not trim and keep spaces at beginning if present
        String suffix = StringUtils.trim(m.group(3));
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
    idx = idx.replace(",", "");
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