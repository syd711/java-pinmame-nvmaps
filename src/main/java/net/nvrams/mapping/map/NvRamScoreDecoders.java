package net.nvrams.mapping.map;

import java.util.List;

import net.nvrams.mapping.NVRamScore;
import net.nvrams.mapping.common.TextDecoders;

/**
 * Utility methods for decoding specific scores
 */
public class NvRamScoreDecoders {

  public static NVRamScore decodeScore(String rom, NVRamScoreMapping scoreMapping, String label, SparseMemory memory) {
    switch (rom) {
      case "monopoly": return decodeMonopolyScore(scoreMapping, label, memory);
      case "godzilla": return decodeAustinNameScore(scoreMapping, label, memory);

      default: return scoreMapping.toScore(label, memory);
    }
  }

  private static NVRamScore decodeAustinNameScore(NVRamScoreMapping scoreMapping, String label, SparseMemory memory) {
    List<Integer> initialsBytes = scoreMapping.getInitials().getIntegers(memory);
    String initials = initialsBytes != null ? TextDecoders.austinNameToText(initialsBytes) : "";
    Long value = scoreMapping.getValue(memory);
    return new NVRamScore(initials, value, -1, label);
  }

  private static NVRamScore decodeMonopolyScore(NVRamScoreMapping scoreMapping, String label, SparseMemory memory) {
    List<Integer> initialsBytes = scoreMapping.getInitials().getIntegers(memory);
    String initials = initialsBytes != null ? TextDecoders._monopolyNameToText(initialsBytes) : "";
    String suffix = initialsBytes != null ? TextDecoders.monopolySuffix(initialsBytes) : "";
    Long value = scoreMapping.getValue(memory);
    NVRamScore sc = new NVRamScore(initials, value, -1, label);
    sc.setSuffix(suffix);
    return sc;
  }
}
