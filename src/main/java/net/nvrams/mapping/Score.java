package net.nvrams.mapping;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Score {
  private final static Logger LOG = LoggerFactory.getLogger(Score.class);

  private String label;       // The label/title of the group

  private String playerInitials;
  private Long score;
  private String scoreText;

  private int position;
  private String suffix;      // optional suffix for non high scores: 100M, 100Gb, 100 combos, 100 martians,... Include initial space if this is a unit
  private String rawScore;

  private final static Map<Locale, DecimalFormat> formats = new HashMap<>();


  public Score(String playerInitials, String scoreText, int position, String title) {
    this.label = title;
    this.scoreText = scoreText;
    this.position = position;
    if (!StringUtils.isEmpty(playerInitials)) {
      this.playerInitials = playerInitials.trim();
    }
  }

  public Score(String playerInitials, Long score, int position, String title) {
    this.label = title;
    this.score = score;
    this.position = position;
    if (!StringUtils.isEmpty(playerInitials)) {
      this.playerInitials = playerInitials.trim();
    }
  }

  public String getPlayerInitials() {
    String initials = StringUtils.defaultString(this.playerInitials);
    while (initials.length() < 3) {
      initials += " ";
    }
    return initials;
  }

  public void setPlayerInitials(String playerInitials) {
    this.playerInitials = playerInitials;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public Long getScore() {
    return score;
  }

  public void setScore(Long score) {
    this.score = score;
  }

  public String getScoreText() {
    return scoreText;
  }

  public void setScoreText(String scoreText) {
    this.scoreText = scoreText;
  }

  public String getRawScore() {
    return rawScore;
  }

  public void setRawScore(String rawScore) {
    this.rawScore = rawScore;
  }
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String title) {
    this.label = title;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  //---------------------------------------------

  public boolean hasInitials() {
    return playerInitials != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Score)) {
      return false;
    }

    Score score = (Score) obj;
    return score.getPlayerInitials().equalsIgnoreCase(this.getPlayerInitials())
        && (score.getPosition() == this.getPosition())
        && (score.getScore() != null ? score.getScore().equals(this.getScore()) : true)
        && (score.getScoreText() != null ? score.getScoreText().equals(this.getScoreText()) : true);
  }

  public String toRaw(Locale loc) {
    return rawScore != null ? rawScore : toString(loc);
  }

  @Override
  public String toString() {
    return toString(Locale.getDefault());
  }

  public String toString(Locale loc) {
    String disp = (position > 0 ? position + ") " : "") + getPlayerInitials() + "   " + getFormattedScore(loc) + (suffix != null? " " + suffix : "");
    return disp.trim();
  }

  @JsonIgnore
  public String getFormattedScore() {
    return getFormattedScore(Locale.getDefault());
  }

  @JsonIgnore
  public String getFormattedScore(Locale loc) {
    if (score != null) {
      return getFormattedScore(score, loc);
    }
    else if (scoreText != null) {
      return scoreText;
    }
    return "";
  }

  public static String getFormattedScore(long score, Locale loc) {
    try {
      DecimalFormat decimalFormat = formats.get(loc);
      if (decimalFormat == null) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(loc);
        decimalFormat = new DecimalFormat("#.##", symbols);
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        formats.put(loc, decimalFormat);
      }

      String formattedScore = decimalFormat.format(score);
      // for french locale replace non-breaking spaces with normal spaces
      // see https://bugs.openjdk.org/browse/JDK-8274768, french whitespace separators changed in Java17
      formattedScore = formattedScore.replace('\u00A0', ' ');
      formattedScore = formattedScore.replace('\u202F', ' ');
      formattedScore = formattedScore.replaceAll(" {2}", " ");
      return formattedScore;
    }
    catch (NumberFormatException e) {
      LOG.error("Failed to read number from '" + score + "': " + e.getMessage());
      return "0";
    }
  }

}