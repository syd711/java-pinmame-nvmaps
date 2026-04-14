package net.nvrams.mapping.superhac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.nvrams.mapping.Score;

public class NVRamMap {

  @JsonProperty("decoder")
  private String decoder;

  @JsonProperty("one_based")
  private boolean oneBased = false;

  @JsonProperty("reverse_digits")
  private boolean reverseDigits = false;

  @JsonProperty("scoretype")
  private String scoretype;

  @JsonProperty("zero_byte")
  private Integer zeroByte;

  @JsonProperty("offsets")
  private List<Integer> offsets;

  @JsonProperty("zero_if_gte")
  private Integer zeroIfGte;

  @JsonProperty("digit_offset")
  private Integer digitOffset = 0;

  @JsonProperty("entries")
  private List<NVRamEntry> entries;

  @JsonProperty("sections")
  private List<NVRamSection> sections;

  @JsonProperty("settings")
  private Map<String, ?> settings;

  //--------------------------------------------------

  public List<Score> parseScores(byte[] data, boolean parseAll) {
    List<Score> scores = new ArrayList<>();
    iterate(data, parseAll, score -> {
      scores.add(score);
    });
    return scores;
  }

  public String getRaw(byte[] data, Locale locale) {
    StringBuilder raw = new StringBuilder();
    String[] currentTitle = { null };
    iterate(data, true, score -> {
      if (!StringUtils.equals(currentTitle[0], score.getLabel())) {
        if (raw.length() > 0) {
          raw.append("\n");
        }
        if (StringUtils.isNotEmpty(score.getLabel())) {
          raw.append(score.getLabel()).append("\n");
        }
      }
      raw.append(score.toRaw(locale)).append("\n");
      currentTitle[0] = score.getLabel();
    });
    return raw.toString();
  }

  private void iterate(byte[] data, boolean parseAll, Consumer<Score> scores) {

    Map<String, Object> _settings = new HashMap<>();
    if (settings != null) {
      _settings.putAll(settings);
    }
    _settings.put("buyins", parseAll);

    int nbScore = 0;
    switch (decoder) {
      case "single_bcd_score":
        nbScore += addScore(scores, ScoreDecoders.decodeSingleBcdScore(ByteDecoders.readOffsets(data, offsets, oneBased, reverseDigits)));
        break;
      case "single_bcd_score_x10":
        nbScore += addScore(scores, 10 * ScoreDecoders.decodeSingleBcdScore(ByteDecoders.readOffsets(data, offsets, oneBased, reverseDigits)));
        break;
      case "single_digit_score":
        nbScore += addScore(scores, ScoreDecoders.decodeSingleDigitScore(ByteDecoders.readOffsets(data, offsets, oneBased, reverseDigits), digitOffset, zeroByte));
        break;
      case "single_digit_score_x10":
        nbScore += addScore(scores, 10 * ScoreDecoders.decodeSingleDigitScore(ByteDecoders.readOffsets(data, offsets, oneBased, reverseDigits), digitOffset, zeroByte));
        break;
      case "single_high_nibble_score":
        nbScore += addScore(scores, ScoreDecoders.decodeSingleHighNibbleScore(ByteDecoders.readOffsets(data, offsets, oneBased, reverseDigits), zeroByte, zeroIfGte));
        break;
      case "single_high_nibble_score_x10":
        nbScore += addScore(scores, 10 * ScoreDecoders.decodeSingleHighNibbleScore(ByteDecoders.readOffsets(data, offsets, oneBased, reverseDigits), zeroByte, zeroIfGte));
        break;
      case "leaderboard_bcd":
        nbScore += addScores(scores, data, entries, "HIGH SCORES", parseAll);
        break;
      case "mixed_leaderboard":
        // FIXME should not be needed but today some sections are redundant
        mergeSections(sections);
        for (NVRamSection section : sections) {
          // stop when a first section is filtered
          if (!section.isEnabled(_settings)) {
            break;
          }
          if (nbScore > 3 && !parseAll) {
            break;
          }
          nbScore += addScores(scores, data, section.getEntries(), section.getTitle(), parseAll);
        }
        break;
      default:
        throw new IllegalArgumentException("No decoder registered or decoder unknown: " + decoder);
    }
  }

  /**
   * FIXME should not be needed but today some section are redundant
   */
  private void mergeSections(List<NVRamSection> sections) {
    NVRamSection currentSection = null;
    for (Iterator<NVRamSection> iter = sections.iterator(); iter.hasNext();) {
      NVRamSection section = iter.next();
      if (currentSection == null || !StringUtils.equals(currentSection.getTitle(), section.getTitle())) {
        currentSection = section;
      }
      else {
        iter.remove();
        currentSection.getEntries().addAll(section.getEntries());
        System.out.println("!!! CONSIDER MERGING SECTION " + section.getTitle());
      }
    }
  }

  private int addScore(Consumer<Score> scores, long decodedScore) {
    Score sc = new Score(null,  decodedScore, -1, "HIGHEST SCORE");
    scores.accept(sc);
    return 1;
  }

  private int addScores(Consumer<Score> scores, byte[] data, List<NVRamEntry> entries, String title, boolean parseAll) {
    int nbScores = 0;
    for (NVRamEntry entry : entries) {
      Score sc = entry.getScore(data, title, oneBased, zeroByte, zeroByte);
      if (sc != null && (parseAll || sc.getScore() != null)) {
        scores.accept(sc);
        nbScores++;
      }
    }
    return nbScores;
  }

  //--------------------------------------------------

  public String getDecoder() {
    return decoder;
  }

  public void setDecoder(String decoder) {
    this.decoder = decoder;
  }

  public boolean isOneBased() {
    return oneBased;
  }

  public void setOneBased(boolean oneBased) {
    this.oneBased = oneBased;
  }

  public boolean isReverseDigits() {
    return reverseDigits;
  }

  public void setReverseDigits(boolean reverseDigits) {
    this.reverseDigits = reverseDigits;
  }

  public String getScoretype() {
    return scoretype;
  }

  public void setScoretype(String scoretype) {
    this.scoretype = scoretype;
  }

  public List<NVRamEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<NVRamEntry> entries) {
    this.entries = entries;
  }

  public List<NVRamSection> getSections() {
    return sections;
  }

  public void setSections(List<NVRamSection> sections) {
    this.sections = sections;
  }

  public Integer getZeroByte() {
    return zeroByte;
  }

  public void setZeroByte(Integer zeroByte) {
    this.zeroByte = zeroByte;
  }

  public List<Integer> getOffsets() {
    return offsets;
  }

  public void setOffsets(List<Integer> offsets) {
    this.offsets = offsets;
  }

  public Integer getDigitOffset() {
    return digitOffset;
  }

  public void setDigitOffset(Integer digitOffset) {
    this.digitOffset = digitOffset;
  }
}
