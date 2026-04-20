package net.nvrams.mapping.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextDecoders {

  //----------------------------------------------------


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
   * eg "EDYY[[[[[[", "DADD[[[[[[""
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
  public static String _monopolyNameToText(List<Integer> byteVals) {
    String suffix = monopolySuffix(byteVals);
    if (suffix != null) {
      StringBuilder initials = new StringBuilder();
      for (int i = 0; i < 3; i++) {
        initials.append((char) (int) byteVals.get(i));
      }
      return initials.toString();
    }
    // else normal extract
    return bytesToText(byteVals);
  }

    /**
   * Decode Monopoly names, where some entries are 3 initials plus a token id.
   */
  public static String monopolySuffix(List<Integer> byteVals) {
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
      return tokenNames.get(tokenId);
    }
    return null;
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
}
