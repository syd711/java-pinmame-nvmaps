package net.nvrams.mapping.extracter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Service layer for VPX operations.
 *
 * All methods contain stub / placeholder implementations.
 * Replace the bodies with your real logic while keeping the signatures.
 */
public class VpxService {
  private final static Logger LOG = LoggerFactory.getLogger(VpxService.class);

  /** Holds the running game process so it can be stopped later. */
  private Process runningGame = null;

  private static Robot robot;
  static {
    try {
      robot = new Robot();
    }
    catch (Exception e) {
      LOG.info("Failed to create robot: " + e.getMessage());
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Launch game
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Launches the VPX game.
   * @param vpxFile the loaded VPX file
   */
  public boolean launchGame(VpxFile vpxFile) {
    File vpxExe = new File("C:\\Visual Pinball\\VPinballX64.exe");
    List<String> strings = new ArrayList<>();
    strings.add(vpxExe.getAbsolutePath());
    strings.add("-Minimized");
    strings.add("-Play");
      
    strings.add("\"" + vpxFile.getFilePath() + "\"");
    try {
      runningGame  = new ProcessBuilder(strings).start();
      return true;
    }
    catch (Exception e) {
      LOG.error("Cannot launch game", e);
      return false;
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Stop game
  // ─────────────────────────────────────────────────────────────────────────
 
  /**
   * Stops the currently running game process.
   * @return true  if a process was running and has been destroyed, false if no game was running.
   */
  public boolean stopGame() {
    if (runningGame != null && runningGame.isAlive()) {
      // close nicely
      robot.keyPress(KeyEvent.VK_ALT);
      robot.keyPress(KeyEvent.VK_TAB);
      robot.delay(10);
      robot.keyRelease(KeyEvent.VK_ALT);
      robot.keyRelease(KeyEvent.VK_TAB);
      robot.delay(100);

      robot.keyPress(KeyEvent.VK_ESCAPE);
      robot.delay(10);
      robot.keyRelease(KeyEvent.VK_ESCAPE);
      robot.delay(3000);

      if (!runningGame.isAlive()) {
        runningGame = null;
        LOG.info("stopGame: process terminated.");
        return true;
      }
      LOG.info("stopGame: process not terminated.");
    }
    else {
      LOG.info("stopGame: no running game process found.");
    }
    return false;
  }

  /**
   * Returns true if a game process is currently running.
   */
  public boolean isGameRunning() {
    return runningGame != null && runningGame.isAlive();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Extract VBS
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Extracts the VBS script embedded in the VPX file.
   * The script is written to the same directory with the same base name and .vbs extension.
   *
   * @param vpxFile the loaded VPX file
   * @return true if extraction succeeded (file exists afterwards)
   */
  public boolean extractVbs(VpxFile vpxFile) {

    File vbsFile = vpxFile.getExpectedVbsFile();
    LOG.info("extractVbs: " + vpxFile.getFilePath() + " → " + vbsFile.getAbsolutePath());

    // Stub: create an empty placeholder so the UI can reflect success during dev
    try {
      //VPXUtil.exportVBS(vpxFile.getFile(), true);
      return vbsFile.exists();
    } 
    catch (Exception e) {
      LOG.error("Cannot extract script {}", vbsFile.getAbsolutePath(), e);
      return false;
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Replace ROM in VBS script
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Replaces the current ROM name in the VBS script with the given alternate ROM name.
   *
   * @param vpxFile the loaded VPX file (must have VBS extracted)
   * @param alternateRom the new ROM name to inject
   * @return true if the replacement was performed successfully
   */
  public boolean replaceRomInScript(VpxFile vpxFile, String alternateRom) {
    File vbsFile = vpxFile.getExpectedVbsFile();
    if (!vbsFile.exists()) {
      LOG.info("[STUB] replaceRomInScript: VBS file not found.");
      return false;
    }

    try {
      String content = new String(Files.readAllBytes(vbsFile.toPath()), StandardCharsets.UTF_8);

      String currentRom = vpxFile.getRomName();
      if (currentRom == null || currentRom.isEmpty()) {
        LOG.info("[STUB] replaceRomInScript: no current ROM name set.");
        return false;
      }

      // TODO: adapt the replacement pattern to match the actual VBS structure.
      // Common pattern:  cGameName = "romname"
      // A simple case-insensitive replacement is done here as a starting point.
      String updated = content.replaceAll(
          "(?i)(cGameName\\s*=\\s*\")" + java.util.regex.Pattern.quote(currentRom) + "\"",
          "$1" + alternateRom + "\""
      );

      if (updated.equals(content)) {
        // Fallback: plain string replace anywhere in the file
        updated = content.replace(currentRom, alternateRom);
      }

      Files.write(vbsFile.toPath(), updated.getBytes(StandardCharsets.UTF_8));
      LOG.info("[STUB] replaceRomInScript: replaced '" + currentRom + "' with '" + alternateRom + "'");
      return true;

    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Parse NVRAM
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Loads and parses the NVRAM file for the current ROM.
   *
   * @param vpxFile the loaded VPX file
   * @return a human-readable string representation of the NVRAM data,
   *     or null if the NVRAM file does not exist.
   */
  public String parseNvram(VpxFile vpxFile) {
    File nvramFile = vpxFile.getExpectedNvramFile();

    if (!nvramFile.exists()) {
      LOG.info("[STUB] parseNvram: file not found: " + nvramFile.getAbsolutePath());
      return null;
    }

    // TODO: replace with real NVRAM parsing logic.
    // NVRAM layout is ROM-specific. A common approach is to use pinmame's
    // nvram layout files or a dedicated library.
    // For now we produce a hex dump as a useful placeholder.
    try {
      byte[] bytes = Files.readAllBytes(nvramFile.toPath());
      return buildHexDump(bytes, nvramFile.getName());
    } catch (IOException e) {
      e.printStackTrace();
      return "Error reading NVRAM file: " + e.getMessage();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Private utilities
  // ─────────────────────────────────────────────────────────────────────────

  private String buildHexDump(byte[] data, String fileName) {
    StringBuilder sb = new StringBuilder();
    sb.append("NVRAM file : ").append(fileName).append("\n");
    sb.append("Size     : ").append(data.length).append(" bytes\n");
    sb.append("─".repeat(70)).append("\n");
    sb.append(String.format("%-10s  %-48s  %s%n", "Offset", "Hex", "ASCII"));
    sb.append("─".repeat(70)).append("\n");

    for (int i = 0; i < data.length; i += 16) {
      sb.append(String.format("%08X  ", i));
      StringBuilder hex = new StringBuilder();
      StringBuilder ascii = new StringBuilder();
      for (int j = 0; j < 16; j++) {
        if (i + j < data.length) {
          byte b = data[i + j];
          hex.append(String.format("%02X ", b));
          ascii.append((b >= 32 && b < 127) ? (char) b : '.');
        } else {
          hex.append("   ");
          ascii.append(' ');
        }
        if (j == 7) hex.append(' ');
      }
      sb.append(String.format("%-49s  %s%n", hex.toString(), ascii.toString()));
    }
    return sb.toString();
  }
}
