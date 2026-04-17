package net.nvrams.mapping.extracter;

import java.io.File;

public class VpxFile {

  private final File file;
  private String romName;
  private boolean vbsExtracted;

  public VpxFile(File file) {
    this.file = file;
    this.romName = "";
    this.vbsExtracted = false;
  }

  public File getFile() {
    return file;
  }

  public String getFileName() {
    return file.getName();
  }

  public String getFilePath() {
    return file.getAbsolutePath();
  }

  public String getDirectory() {
    return file.getParent();
  }

  public long getFileSizeBytes() {
    return file.length();
  }

  public String getFileSizeFormatted() {
    long bytes = file.length();
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
    if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
    return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
  }

  public String getLastModified() {
    return new java.util.Date(file.lastModified()).toString();
  }

  public String getRomName() {
    return romName;
  }

  public void setRomName(String romName) {
    this.romName = romName;
  }

  public boolean isVbsExtracted() {
    return vbsExtracted;
  }

  public void setVbsExtracted(boolean vbsExtracted) {
    this.vbsExtracted = vbsExtracted;
  }

  /**
   * Returns the expected VBS output path (same directory, same name, .vbs extension)
   */
  public File getExpectedVbsFile() {
    String baseName = file.getName();
    if (baseName.toLowerCase().endsWith(".vpx")) {
      baseName = baseName.substring(0, baseName.length() - 4);
    }
    return new File(file.getParent(), baseName + ".vbs");
  }

  /**
   * Returns the expected NVRAM file path
   */
  public File getExpectedNvramFile() {
    return new File(Constants.NVRAM_FOLDER, (romName.isEmpty() ? getBaseName() : romName) + ".nv");
  }

  public String getBaseName() {
    String name = file.getName();
    if (name.toLowerCase().endsWith(".vpx")) {
      return name.substring(0, name.length() - 4);
    }
    return name;
  }
}
