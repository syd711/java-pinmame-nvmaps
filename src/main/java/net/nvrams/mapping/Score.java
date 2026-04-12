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

  private String playerInitials = "???";
  private long score;
  private int position;
  private String label;       // optional label for titled scores, high-scores, buy-in scores....
  private String suffix;      // optional suffix for non high scores: 100M, 100Gb, 100 combos, 100 martians,... Include initial space if this is a unit
  private String rawScore;

  private final static Map<Locale, DecimalFormat> formats = new HashMap<>();


  public Score(String playerInitials, String rawScore, long score, int position) {
    this.score = score;
    this.position = position;
    this.rawScore = rawScore;
    if (!StringUtils.isEmpty(playerInitials)) {
      this.playerInitials = playerInitials;
    }
  }

  public String getPlayerInitials() {
    while (playerInitials.length() < 3) {
      playerInitials += " ";
    }
    return playerInitials;
  }

  public void setPlayerInitials(String playerInitials) {
    if (!StringUtils.isEmpty(playerInitials.trim())) {
      this.playerInitials = playerInitials;
    }
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public long getScore() {
    return score;
  }

  public void setScore(long score) {
    this.score = score;
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

  public void setLabel(String label) {
    this.label = label;
  }
  
  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Score)) {
      return false;
    }

    Score score = (Score) obj;
    return score.getPlayerInitials().equalsIgnoreCase(this.getPlayerInitials())
        && score.getPosition() == this.getPosition()
        && score.getScore() == this.getScore();
  }

  public boolean matches(Score newScore) {
    return this.playerInitials != null && this.playerInitials.equals(newScore.getPlayerInitials())
        && this.score == newScore.getScore();

  }

  @Override
  public String toString() {
    return toString(Locale.getDefault());
  }

  public String toString(Locale loc) {
    return "#" + this.getPosition() + " " + this.getPlayerInitials() + "   " + getFormattedScore(loc);
  }

  @JsonIgnore
  public String getFormattedScore() {
    return getFormattedScore(Locale.getDefault());
  }

  @JsonIgnore
  public String getFormattedScore(Locale loc) {
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