package net.nvrams.mapping.superhac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nvrams.mapping.RawScoreParser;

public class ByteDecoders {

  private final static Logger LOG = LoggerFactory.getLogger(RawScoreParser.class);

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
