package net.nvrams.mapping.superhac;

import java.util.List;

public class ScoreDecoders {

  public static long decodeScoreBytes(List<Integer> scoreBytes, String scoreDecoder, NVRamEntry entry, Integer zeroByte, Integer zeroIfGte) {
    Integer digitOffset = entry.getDigitOffset();
    switch (scoreDecoder) {
      case "bcd":
        return ByteDecoders.bcdToInt(scoreBytes);
      case "bcd_x10":
        return ByteDecoders.bcdToInt(scoreBytes) * 10;
      case "big_endian":
        return ByteDecoders.bytesToInt(scoreBytes);
      case "big_endian_x10":
        return ByteDecoders.bytesToInt(scoreBytes) * 10;
      case "byte_pair_100_1":
        return scoreBytes.get(0) * 100 + scoreBytes.get(1);
      case "low_nibble_100_bcd":
        return (scoreBytes.get(0) & 0x0F) * 100 + ByteDecoders.bcdToInt(List.of(scoreBytes.get(1)));
      case "high_nibble_digits":
        return ByteDecoders.highNibbleBytesToInt(scoreBytes, zeroByte != null? zeroByte : 255, zeroIfGte);
      case "raw_digits":
        return ByteDecoders.digitBytesToInt(scoreBytes, digitOffset != null? digitOffset : 0, zeroByte);
      case "raw_digits_x10":
        return ByteDecoders.digitBytesToInt(scoreBytes, digitOffset != null? digitOffset : 0, zeroByte) * 10;
      case "raw_byte":
        return scoreBytes.get(0);
      default:
        throw new IllegalArgumentException("Unknown score decoder: " + scoreDecoder);
    }
  }

  public static long decodeSingleBcdScore(List<Integer> bytes) {
    return ByteDecoders.bcdToInt(bytes);
  }

  public static long decodeSingleDigitScore(List<Integer> bytes, Integer digitOffset, Integer zeroByte) {
    return ByteDecoders.digitBytesToInt(bytes, digitOffset, zeroByte);
  }

  public static long decodeSingleHighNibbleScore(List<Integer> bytes, Integer zeroByte, Integer zeroIfGte) {
    return ByteDecoders.highNibbleBytesToInt(bytes, zeroByte, zeroIfGte);
  }
}
