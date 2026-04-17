package net.nvrams.mapping.superhac;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.nvrams.mapping.NVRamScore;

public class EntryDecoders {

  private static final Map<Integer, String> MONTH_NAMES = new java.util.HashMap<>() {{
    put(1, "JAN"); put(2, "FEB"); put(3, "MAR"); put(4, "APR");
    put(5, "MAY"); put(6, "JUN"); put(7, "JUL"); put(8, "AUG");
    put(9, "SEP"); put(10, "OCT"); put(11, "NOV"); put(12, "DEC");
  }};

  private static String monthName(int m) {
    return MONTH_NAMES.getOrDefault(m, String.valueOf(m));
  }

  public static NVRamScore dispatchEntryDecoder(byte[] f, NVRamEntry entry, String initials, int rank, String title, 
    boolean oneBased, Integer zeroByte, Integer zeroIfGte) {
  switch (entry.getEntryDecoder()) {
    case "afm_ruler_of_the_universe":
      return decodeAfmRulerOfTheUniverseEntry(f, entry, initials, rank, title, oneBased);
    case "andrett4_lap_time":
      return decodeAndrett4LapTimeEntry(f, entry, initials, rank, title, oneBased);
    case "apollo13_multiball":
      return decodeApollo13MultiballEntry(f, entry, initials, rank, title);
    case "labeled_single_value":
      return decodeLabeledSingleValueEntry(f, entry, initials, rank, title, oneBased);
    case "since_date":
      return decodeSinceDateEntry(f, entry, initials, rank, title, oneBased);
    case "x_y_seconds":
      return decodeXYSecondsEntry(f, entry, initials, rank, title, oneBased);
    case "mm_ss_cc":
      return decodeMmSsCcEntry(f, entry, initials, rank, title, oneBased);
    case "crowned_datetime":
      return decodeCrownedDatetimeEntry(f, entry, initials, rank, title, oneBased);
    case "datetime":
      return decodeDatetimeEntry(f, entry, initials, rank, title, oneBased);
    case "team_wins_rings":
      return decodeTeamWinsRingsEntry(f, entry, initials, rank, title, oneBased);
    case "static_text":
      return decodeStaticTextEntry(f, entry, initials, rank, title);
    case "name_text":
      return decodeNameTextEntry(f, entry, initials, rank, title);
    case "labeled_score":
      return decodeLabeledScoreEntry(f, entry, initials, rank, title, oneBased, zeroByte, zeroIfGte);
    case "got_to_year":
      return decodeGotToYearEntry(f, entry, initials, rank, title, oneBased);
    case "label_name_value":
      return decodeLabelNameValueEntry(f, entry, initials, rank, title, oneBased, zeroByte, zeroIfGte);
    case "name_value":
      return decodeNameValueEntry(f, entry, initials, rank, title, oneBased, zeroByte, zeroIfGte);
    case "label_value_name":
      return decodeLabelValueNameEntry(f, entry, initials, rank, title, oneBased, zeroByte, zeroIfGte);
    default:
      throw new IllegalArgumentException("Unknown entry decoder: " + entry.getEntryDecoder());
    }
  }

  private static NVRamScore decodeAfmRulerOfTheUniverseEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    int termOffset = entry.getExtraOffset("term");
    int termByte = ByteDecoders.readOffset(f, termOffset, oneBased);

    String month = monthName(dataBytes.get(2));
    int hour = dataBytes.get(4);
    String suffix = " AM";
    if (hour > 12) { hour -= 12; suffix = " PM"; }
    String minute = String.format("%02d", dataBytes.get(5));
    int year = dataBytes.get(0) * 256 + dataBytes.get(1);
    String termText = termByte == 0
        ? "INAUGURATED"
        : String.format("RE-ELECTION #%X", termByte);

    String valueText = termText + " | " + dataBytes.get(3) + " " + month + ", " + year + " " + hour + ":" + minute + suffix;
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeAndrett4LapTimeEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    int lapByte = ByteDecoders.readOffsets(f, offsets, oneBased).get(0);
    int high = lapByte >> 4;
    int low = lapByte & 0x0F;
    String lowText = lapByte != 0 ? Integer.toHexString(low).toUpperCase() : "0";

    String valueText = high + "." + lowText + "0";
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeApollo13MultiballEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title) {
     String valueText = "PLAYED 13-BALL MULTIBALL";
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeLabeledSingleValueEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    int valueByte = ByteDecoders.readOffsets(f, offsets, oneBased).get(0);
    String valueText;
    if ("hex".equals(entry.getValueFormat())) {
      valueText = Integer.toHexString(valueByte).toUpperCase();
    } else {
      valueText = String.valueOf(valueByte);
    }
    
    if (StringUtils.isNotEmpty(entry.getLabel())) {
      valueText = entry.getLabel() + " = " + valueText;
    }
    NVRamScore sc = new NVRamScore(initials, valueText, rank, title);
		if (StringUtils.isNotEmpty(entry.getValueSuffix())) {
      sc.setSuffix(entry.getValueSuffix());
    }
    return sc;
  }

  private static NVRamScore decodeSinceDateEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    String month = monthName(dataBytes.get(0));
    int day = dataBytes.get(1);
    int year = dataBytes.get(2) * 256 + dataBytes.get(3);
    String valueText = "SINCE " + month + ". " + day + ", " + year;
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeXYSecondsEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    String valueText = dataBytes.get(0) + "." + dataBytes.get(1) + " SECONDS";
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeMmSsCcEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    int total = dataBytes.get(0) * 256 + dataBytes.get(1);
    int minutes = Math.round(total / 6000.0f);
    int seconds = Math.round((total - minutes * 6000) / 100.0f);
    int centiseconds = total - minutes * 6000 - seconds * 100;
    String valueText = String.format("%02d:%02d.%02d", minutes, seconds, centiseconds);
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeCrownedDatetimeEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    int crownCountOffset = entry.getExtraOffset("crown_count");
    int crownCount = ByteDecoders.readOffsets(f, List.of(crownCountOffset), oneBased).get(0);

    if (crownCount == 0) {
      return null;
    }

    Map<Integer, String> ordinalSuffix = Map.of(1, "st", 2, "nd", 3, "rd");
    String suffix = ordinalSuffix.getOrDefault(crownCount, "th");
    String month = monthName(dataBytes.get(2));
    int hour = dataBytes.get(4);
    String amPm = " AM";
    if (hour > 12) { hour -= 12; amPm = " PM"; }
    int year = dataBytes.get(0) * 256 + dataBytes.get(1);

    String valueText = "CROWNED FOR THE " + crownCount + suffix + " TIME" + " | " + 
      dataBytes.get(3) + " " + month + ", " + year + " " + hour + ":" + String.format("%02d", dataBytes.get(5)) + amPm;

    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeDatetimeEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    String month = monthName(dataBytes.get(2));
    int hour = dataBytes.get(4);
    String amPm = " AM";
    if (hour > 12) { hour -= 12; amPm = " PM"; }
    int year = dataBytes.get(0) * 256 + dataBytes.get(1);

    String valueText = dataBytes.get(3) + " " + month + ", " + year
        + " " + hour + ":" + String.format("%02d", dataBytes.get(5)) + amPm;

    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeTeamWinsRingsEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    long rings = ByteDecoders.bcdToInt(List.of(dataBytes.get(0)));
    long wins = ByteDecoders.bcdToInt(List.of(dataBytes.get(1))) * 100
        + ByteDecoders.bcdToInt(List.of(dataBytes.get(2)));

    String valueText;
    if (rings == 0) {
      valueText = String.valueOf(wins);
    } else if (rings == 1) {
      valueText = wins + " 1-RING";
    } else {
      valueText = wins + " " + rings + "-RINGS";
    }

    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeStaticTextEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title) {
    String valueText = entry.getText();
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeNameTextEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title) {
    String valueText = entry.getText();
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeLabeledScoreEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased, Integer zeroByte, Integer zeroIfGte) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> scoreBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    long score = ScoreDecoders.decodeScoreBytes(scoreBytes, entry.getScoreDecoder(), entry, zeroByte, zeroIfGte);
    String valueText =  entry.getLabel() + "   " + String.format("%,d", score);
    if (entry.getValueSuffix() != null) {
      valueText += " " + entry.getValueSuffix();
    }
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeGotToYearEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased) {
    List<Integer> offsets = entry.getDataOffsets();
    int dataByte = ByteDecoders.readOffsets(f, offsets, oneBased).get(0);
    int baseYear = entry.getBaseYear() != null ? (Integer) entry.getBaseYear() : 1993;
    long year = baseYear - ByteDecoders.bcdToInt(List.of(dataByte)) * 100;
    String valueText = " GOT TO " + year;
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeLabelNameValueEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased, Integer zeroByte, Integer zeroIfGte) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    long value = ScoreDecoders.decodeScoreBytes(dataBytes, entry.getScoreDecoder(), entry, zeroByte, zeroIfGte);
    String valueText = entry.getLabel() + " | " + formatValue(value, entry);
    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeNameValueEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased, Integer zeroByte, Integer zeroIfGte) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    long value = ScoreDecoders.decodeScoreBytes(dataBytes, entry.getScoreDecoder(), entry, zeroByte, zeroIfGte);
    String valueText = formatValue(value, entry);

    return new NVRamScore(initials, valueText, rank, title);
  }

  private static NVRamScore decodeLabelValueNameEntry(byte[] f, NVRamEntry entry, String initials, int rank, String title, boolean oneBased, Integer zeroByte, Integer zeroIfGte) {
    List<Integer> offsets = entry.getDataOffsets();
    List<Integer> dataBytes = ByteDecoders.readOffsets(f, offsets, oneBased);
    long value = ScoreDecoders.decodeScoreBytes(dataBytes, entry.getScoreDecoder(), entry, zeroByte, zeroIfGte);
    String valueText = entry.getLabel() + formatValue(value, entry);

    return new NVRamScore(initials, valueText, rank, title);
  }

  private static String formatValue(long value, NVRamEntry entry) {
    String valueText;
    if ("hex".equals(entry.getValueFormat())) {
      valueText = Long.toHexString(value).toUpperCase();
    } else {
      valueText = String.valueOf(value);
    }
    if (entry.getValuePrefix() != null) {
      valueText = entry.getValuePrefix() + valueText;
    }
    if (entry.getValueSuffix() != null) {
      valueText = valueText + " " + entry.getValueSuffix();
    }
    return valueText;
  }
}
