package net.nvrams.mapping.superhac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nvrams.mapping.DefaultAdapter;

public class ByteDecoders {

  private final static Logger LOG = LoggerFactory.getLogger(DefaultAdapter.class);

  /**
   * Read bytes at given offsets
   */
  public static List<Integer> readOffsets(byte[] data, List<Integer> offsets, boolean oneBased) {
    return readOffsets(data, offsets, oneBased, false);
  }

  /**
   * Read bytes at given offsets
   */
  public static List<Integer> readOffsets(byte[] data, List<Integer> offsets, boolean oneBased, boolean reverseDigits) {
    List<Integer> result = new ArrayList<>();
    for (int offset : offsets) {
      int b = readOffset(data, offset, oneBased);
      if (reverseDigits) {
        result.add(0, b);
      } else {
        result.add(b);
      }
    }
    return result;
  }

  public static int readOffset(byte[] data, int offset, boolean oneBased) {
    int seekOffset = oneBased ? offset - 1 : offset;
    if (seekOffset < 0 || seekOffset >= data.length) {
      throw new IllegalArgumentException("Could not read byte at offset " + seekOffset);
    }
    return data[seekOffset] & 0xFF;
  }

  //----------------------------------------------------

  /**
   * Decode initials from byte values using the specified decoder.
   */
  public static String decodeInitials(List<Integer> byteVals, String nameDecoder) {
    if (nameDecoder == null) {
      return cleanText(bytesToText(byteVals));
    }
    switch (nameDecoder) {
      case "ascii_upper":
        return cleanText(bytesToText(byteVals)).toUpperCase();
      case "low_nibble_pairs_ascii":
        return cleanText(lowNibblePairsToText(byteVals));
      case "high_nibble_pairs_ascii":
        return cleanText(highNibblePairsToText(byteVals));
      case "atlantis_initials":
        return cleanText(atlantisInitialsToText(byteVals));
      case "hvymetal_initials":
        return cleanText(hvymetalInitialsToText(byteVals));
      case "dd_l2_initials":
        return cleanText(ddL2InitialsToText(byteVals));
      case "grand_l4_initials":
        return cleanText(grandL4InitialsToText(byteVals));
      case "austin_name":
        return cleanText(austinNameToText(byteVals));
      case "monopoly_name":
        return cleanText(monopolyNameToText(byteVals));
      case "ff_blank_ascii":
        return cleanText(ffBlankInitialsToText(byteVals));
      default:
        return cleanText(bytesToText(byteVals));
    }
  }

  /**
   * Convert a sequence of byte values into an ASCII string.
   */
  public static String bytesToText(List<Integer> byteVals) {
    StringBuilder sb = new StringBuilder();
    for (int b : byteVals) {
      sb.append((char) b);
    }
    return sb.toString();
  }

  /**
   * Decode packed initials from pairs of low nibbles.
   */
  public static String lowNibblePairsToText(List<Integer> byteVals) {
    if (byteVals.size() % 2 != 0) {
      throw new IllegalArgumentException("Packed nibble text requires an even number of bytes");
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < byteVals.size(); i += 2) {
      int charCode = ((byteVals.get(i) & 0x0F) << 4) | (byteVals.get(i + 1) & 0x0F);
      sb.append((char) charCode);
    }
    return sb.toString();
  }

  /**
   * Decode packed initials from pairs of high nibbles, with FF meaning space.
   */
  public static String highNibblePairsToText(List<Integer> byteVals) {
    if (byteVals.size() % 2 != 0) {
      throw new IllegalArgumentException("Packed nibble text requires an even number of bytes");
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < byteVals.size(); i += 2) {
      int high = (byteVals.get(i) >> 4) & 0x0F;
      int low = (byteVals.get(i + 1) >> 4) & 0x0F;
      if (high == 0x0F && low == 0x0F) {
        sb.append(' ');
        continue;
      }
      sb.append((char) ((high << 4) | low));
    }
    return sb.toString();
  }

  /**
   * Decode Atlantis initials where 79 is space and other values map via +48.
   */
  public static String atlantisInitialsToText(List<Integer> byteVals) {
    StringBuilder sb = new StringBuilder();
    for (int byteVal : byteVals) {
      sb.append(byteVal == 79 ? ' ' : (char) (byteVal + 48));
    }
    return sb.toString();
  }

  /**
   * Decode Heavy Metal initials where 79/255 are spaces, others map via +48, then reverse.
   */
  public static String hvymetalInitialsToText(List<Integer> byteVals) {
    List<Character> chars = new ArrayList<>();
    for (int byteVal : byteVals) {
      chars.add((byteVal == 79 || byteVal == 255) ? ' ' : (char) (byteVal + 48));
    }
    Collections.reverse(chars);
    StringBuilder sb = new StringBuilder();
    for (char c : chars) {
      sb.append(c);
    }
    return sb.toString();
  }

  /**
   * Decode Dr Dude Team initials where 0 is space and other values map via +54.
   */
  public static String ddL2InitialsToText(List<Integer> byteVals) {
    StringBuilder sb = new StringBuilder();
    for (int byteVal : byteVals) {
      sb.append(byteVal == 0 ? ' ' : (char) (byteVal + 54));
    }
    return sb.toString();
  }

  /**
   * Decode Grand Lizard initials where values above 96 are stored with +128.
   */
  public static String grandL4InitialsToText(List<Integer> byteVals) {
    StringBuilder sb = new StringBuilder();
    for (int byteVal : byteVals) {
      sb.append(byteVal > 96 ? (char) (byteVal - 128) : (char) byteVal);
    }
    return sb.toString();
  }

  /**
   * Decode Austin Powers names, handling bracket-as-space padding and short names.
   */
  public static String austinNameToText(List<Integer> byteVals) {
    List<Character> chars = new ArrayList<>();
    for (int byteVal : byteVals) {
      chars.add(byteVal == 91 ? ' ' : (char) byteVal);
    }

    // Check if chars[4:] are all spaces and byteVals[3] == byteVals[2]
    boolean allSpacesFrom4 = true;
    for (int i = 4; i < chars.size(); i++) {
      if (chars.get(i) != ' ') {
        allSpacesFrom4 = false;
        break;
      }
    }
    if (allSpacesFrom4 && byteVals.size() >= 4 && byteVals.get(3).equals(byteVals.get(2))) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 3; i++) {
        sb.append(chars.get(i));
      }
      return sb.toString().stripTrailing();
    }

    StringBuilder sb = new StringBuilder();
    for (char c : chars) {
      sb.append(c);
    }
    return sb.toString().stripTrailing();
  }

  /**
   * Decode Monopoly names, where some entries are 3 initials plus a token id.
   */
  public static String monopolyNameToText(List<Integer> byteVals) {
    java.util.Map<Integer, String> tokenNames = new java.util.HashMap<>();
    tokenNames.put(0, "BATTLESHIP");
    tokenNames.put(1, "THIMBLE");
    tokenNames.put(2, "SACK OF MONEY");
    tokenNames.put(3, "IRON");
    tokenNames.put(4, "CANNON");
    tokenNames.put(5, "DOG");
    tokenNames.put(6, "HORSE AND RIDER");
    tokenNames.put(7, "SHOE");
    tokenNames.put(8, "TOP HAT");
    tokenNames.put(9, "WHEELBARROW");
    tokenNames.put(10, "RACECAR");
    tokenNames.put(11, "PLD LOGO");

    int tokenId = byteVals.get(3);
    if (tokenNames.containsKey(tokenId)) {
      StringBuilder initials = new StringBuilder();
      for (int i = 0; i < 3; i++) {
        initials.append((char) (int) byteVals.get(i));
      }
      String initialsStr = initials.toString().stripTrailing();
      return (initialsStr + " " + tokenNames.get(tokenId)).stripTrailing();
    }

    return cleanText(bytesToText(byteVals));
  }

  /**
   * Decode ASCII initials where 255 bytes should be treated as blanks.
   */
  public static String ffBlankInitialsToText(List<Integer> byteVals) {
    StringBuilder sb = new StringBuilder();
    for (int byteVal : byteVals) {
      sb.append(byteVal == 255 ? '\0' : (char) byteVal);
    }
    return sb.toString();
  }

  /**
   * Strip common NVRAM padding characters from decoded text.
   */
  public static String cleanText(String text) {
    int nullIdx = text.indexOf('\0');
    if (nullIdx >= 0) {
      text = text.substring(0, nullIdx);
    }
    // Strip trailing \xFF and spaces
    int end = text.length();
    while (end > 0) {
      char c = text.charAt(end - 1);
      if (c == '\uffff' || c == '\u00ff' || c == ' ') {
        end--;
      } else {
        break;
      }
    }
    return text.substring(0, end);
  }

  //----------------------------------------------------

  /**
   * Convert a sequence of BCD bytes into one integer score.
   */
  public static long bcdToInt(List<Integer> byteVals) {
    long score = 0;
    for (int byteVal : byteVals) {
      int high = (byteVal >> 4) & 0xF;
      int low = byteVal & 0xF;
      if (high > 9 || low > 9) {
        LOG.error(String.format("Invalid BCD byte: 0x%02X", byteVal));
        return -1
        ;
      }
      score = score * 100 + (high * 10 + low);
    }
    return score;
  }

  /**
   * Convert a sequence of bytes into one big-endian integer.
   */
  public static long bytesToInt(List<Integer> byteVals) {
    long value = 0;
    for (int byteVal : byteVals) {
      value = (value << 8) + byteVal;
    }
    return value;
  }

  /**
   * Convert a sequence of digit bytes into an integer.
   */
  public static long digitBytesToInt(List<Integer> byteVals, Integer digitOffset, Integer zeroByte) {
    long value = 0;
    for (int byteVal : byteVals) {
      int digit;
      if (zeroByte != null && byteVal == zeroByte) {
        digit = 0;
      } else {
        digit = byteVal - digitOffset;
      }
      if (digit < 0 || digit > 9) {
        throw new IllegalArgumentException(
            "Invalid digit byte: " + byteVal + " with offset " + digitOffset);
      }
      value = value * 10 + digit;
    }
    return value;
  }

  /**
   * Convert bytes whose high nibbles represent digits into an integer.
   */
  public static long highNibbleBytesToInt(List<Integer> byteVals, Integer zeroByte, Integer zeroIfGte) {
    long value = 0;
    List<Integer> reversed = new ArrayList<>(byteVals);
    Collections.reverse(reversed);
    for (int byteVal : reversed) {
      int digit;
      if (zeroIfGte != null && byteVal >= zeroIfGte) {
        digit = 0;
      } else if (zeroByte != null && byteVal == zeroByte) {
        digit = 0;
      } else {
        digit = (byteVal >> 4) & 0x0F;
      }
      if (digit < 0 || digit > 9) {
        throw new IllegalArgumentException("Invalid high-nibble digit byte: " + byteVal);
      }
      value = value * 10 + digit;
    }
    return value;
  }
}
