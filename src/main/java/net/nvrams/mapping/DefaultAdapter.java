package net.nvrams.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Copied from VpinStudio to test 
 */
public class DefaultAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(DefaultAdapter.class);

  private List<String> titles = List.of("MASTER MAGICIAN", "CHAMPION", "GRAND CHAMPION", "WORLD RECORD", "GREATEST VAMPIRE HUNTER", "GREATEST TIME LORD", "RIVER MASTER", "CLUB CHAMPION", "HIGHEST SCORES", "HIGHEST SCORE", "THE BEST DUDE", "ACE WINGER"
    // to be added in ScoringDB.json....
    // system-rom-20    che_cho            bop_17                     punchy            punchy             ripleys
      ,"HIGH SCORE" ,"ROAD-TRIP KING", "BILLIONAIRE CLUB MEMBERS", "MY BEST FRIEND", "MY OTHER FRIENDS", "GRAND CHAMP"
  );

  private List<String> skipTitlesCheckFor = List.of("gnr_300", "jupk_513", "jupk_600", "pool_l7", "trek_201"
  );


  public List<NVRamScore> getScores(String raw, boolean parseAll) {
    return getScores(raw, titles, parseAll);
  }

  public List<NVRamScore> getScores(String rom, List<String> lines, boolean parseAll) {
    if (lines == null || lines.isEmpty()) {
      return Collections.emptyList();
    }

    try {
      List<NVRamScore> scores = new ArrayList<>();

	    String currentTitle = null;
      String currentSuffix = null;
      NVRamScore currentScore = null;
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
        	if (skipTitlesCheckFor.contains(rom) || scores.size() <= 3 || parseAll) {
            continue;
          }
          // else stop the parsing
          break;
      	}

        if (currentTitle != null && isScoreLine(line)) {
          currentScore = createScore(currentTitle, line);
          if (currentScore != null) {
            scores.add(currentScore);
          }
        }
        else if (currentTitle != null && isTitleScoreLine(line)) {
            currentScore = createTitledScore(currentTitle, line);
            if (currentScore != null) {
            if (parseAll || titles.contains(currentTitle)) {
              scores.add(currentScore);
            }
          }
        }
        else if (currentTitle != null && StringUtils.isNotEmpty(line)) {
          if (currentScore != null) {
            currentSuffix = " " + line;
          }
        }
        else if (StringUtils.isNotEmpty(line)) {
          currentTitle = line;
        }
      }
      if (currentSuffix != null && currentScore != null) {
        currentScore.setSuffix(currentSuffix);
      }

      //OLE removed
      //return filterDuplicates(scores);
      return scores;
    }
    catch (Exception e) {
      LOG.error("Score parsing failed: {}", e.getMessage(), e);
      throw e;
    }
  }

  //-------------------------

  private static final String _patternIndex = "(\\d+\\)|#\\d+|\\d+#|\\d+,|\\d+\\.:) +";
  private static final String _patternScore = "([ ?/+\\-a-zA-Z0-9\u0000]{3,}\\s+)?(?:[-|]?\\s+)?(\\d\\d?\\d?(?:[.,?\u00a0\u202f\ufffd\u00ff]?\\d\\d\\d)*(?:\\.\\d)?)((?:\\s\\d+)?[\\-\\sa-zA-Z]*)$";

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
  protected NVRamScore createTitledScore(@Nullable String title, @NonNull String line) {
    Matcher m = patternScoreTitle.matcher(line);
    if (m.find()) {
      String initials = StringUtils.trim(m.group(1));
      if (StringUtils.isEmpty(initials)) {
        initials = "";
      }

      String scoreString = StringUtils.trim(m.group(2));
      long scoreValue = toNumericScore(scoreString, false);
      if (scoreValue != -1) {
        NVRamScore sc = new NVRamScore(initials, scoreValue, -1, title);
        sc.setLabel(title);

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
  public NVRamScore createScore(@Nullable String title, @NonNull String line) {
    String idx = StringUtils.substringBefore(line, " ");
    idx = idx.replace(")", "");
    idx = idx.replace(",", "");
    idx = idx.replace("#", "");
    idx = idx.replace(".:", "");
    int index = Integer.parseInt(idx);
    
    line = StringUtils.substringAfter(line, " ");
    NVRamScore sc = createTitledScore(title, line);
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

  protected String cleanScore(String score) {
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

  protected List<NVRamScore> filterDuplicates(List<NVRamScore> scores) {
    List<NVRamScore> scoreList = new ArrayList<>();
    int pos = 1;
    for (NVRamScore s : scores) {
      Optional<NVRamScore> match = scoreList.stream().filter(score -> score.getFormattedScore().equals(s.getFormattedScore()) && String.valueOf(score.getPlayerInitials()).equals(s.getPlayerInitials())).findFirst();
      if (match.isPresent()) {
        continue;
      }
      s.setPosition(pos);
      scoreList.add(s);
      pos++;
    }
    return scoreList;
  }
}