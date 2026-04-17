package net.nvrams.mapping.extracter;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable {

  // ── File info panel ──────────────────────────────────────────────────────
  @FXML private VBox fileInfoPane;
  @FXML private Label labelFileName;
  @FXML private Label labelFilePath;
  @FXML private Label labelFileSize;
  @FXML private Label labelLastModified;
  @FXML private Label labelVbsStatus;

  // ── ROM field ─────────────────────────────────────────────────────────────
  @FXML private TextField textRomName;

  // ── Action buttons ────────────────────────────────────────────────────────
  @FXML private Button btnOpenFile;
  @FXML private Button btnCloseFile;
  @FXML private Button btnLaunchGame;
    @FXML private Button btnStopGame;
  @FXML private Button btnExtractVbs;
  @FXML private Button btnAlternateRom;
  @FXML private Button btnLoadNvram;

  // ── NVRAM output area ─────────────────────────────────────────────────────
  @FXML private TextArea textNvramOutput;

  // ── Status bar ────────────────────────────────────────────────────────────
  @FXML private Label labelStatus;

  // ── Empty-state placeholder ───────────────────────────────────────────────
  @FXML private VBox emptyStatePane;

  // ── Internal state ────────────────────────────────────────────────────────
  private VpxFile currentVpxFile;
  private final VpxService vpxService = new VpxService();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setFileLoaded(false);
    setStatus("Ready — open a VPX file to get started.");
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Button handlers
  // ─────────────────────────────────────────────────────────────────────────

  @FXML
  private void onOpenFile() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Open VPX File");
    chooser.setInitialDirectory(new File(Constants.TABLES_FOLDER));
    chooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Visual PinballX Files", "*.vpx"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
    );

    Stage stage = (Stage) btnOpenFile.getScene().getWindow();
    File selected = chooser.showOpenDialog(stage);

    if (selected != null) {
      loadVpxFile(selected);
    }
  }

  @FXML
  private void onCloseFile() {
    currentVpxFile = null;
    textRomName.clear();
    textNvramOutput.clear();
    setFileLoaded(false);
    setStatus("File closed.");
  }

  @FXML
  private void onLaunchGame() {
    if (currentVpxFile == null) return;
    currentVpxFile.setRomName(textRomName.getText().trim());
    setStatus("Launching: " + currentVpxFile.getFileName() + " …");
    vpxService.launchGame(currentVpxFile);

    btnStopGame.setDisable(false);
    btnLaunchGame.setDisable(true);
  }

  @FXML
  private void onStopGame() {
    boolean wasStopped = vpxService.stopGame();
    if (wasStopped) {
        btnStopGame.setDisable(true);
        btnLaunchGame.setDisable(false);
        setStatus("Game stopped.");
    } else {
        setStatus("No running game process to stop.");
    }
  }

  @FXML
  private void onExtractVbs() {
    if (currentVpxFile == null) return;
    setStatus("Extracting VBS script …");

    boolean success = vpxService.extractVbs(currentVpxFile);

    if (success) {
      currentVpxFile.setVbsExtracted(true);
      refreshVbsStatus();
      setStatus("VBS script extracted → " + currentVpxFile.getExpectedVbsFile().getAbsolutePath());
    } else {
      setStatus("VBS extraction failed or returned no output.");
      showAlert(Alert.AlertType.WARNING, "Extraction Failed",
              "The VBS script could not be extracted.\n" +
              "Make sure VPinballX is installed and accessible.");
    }
  }

  @FXML
  private void onAlternateRom() {
    if (currentVpxFile == null) return;

    TextInputDialog dialog = new TextInputDialog(currentVpxFile.getRomName());
    dialog.setTitle("Alternate ROM");
    dialog.setHeaderText("Replace ROM name in script");
    dialog.setContentText("Enter the alternate ROM name:");
    applyDialogStyle(dialog);

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(altRom -> {
        if (altRom.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty ROM Name", "Please enter a valid ROM name.");
            return;
        }
        String oldRom = currentVpxFile.getRomName();
        boolean ok = vpxService.replaceRomInScript(currentVpxFile, altRom.trim());
        if (ok) {
            currentVpxFile.setRomName(altRom.trim());
            textRomName.setText(altRom.trim());
            setStatus("ROM replaced: '" + oldRom + "' → '" + altRom.trim() + "' in VBS script.");
        } else {
            setStatus("Could not replace ROM name — make sure the VBS script is extracted first.");
            showAlert(Alert.AlertType.WARNING, "ROM Replace Failed",
                    "The ROM name could not be replaced.\n" +
                    "Ensure the VBS script has been extracted first.");
        }
    });
  }

  @FXML
  private void onLoadNvram() {
    if (currentVpxFile == null) return;
    currentVpxFile.setRomName(textRomName.getText().trim());

    setStatus("Loading NVRAM …");
    String nvramContent = vpxService.parseNvram(currentVpxFile);

    if (nvramContent != null) {
        textNvramOutput.setText(nvramContent);
        setStatus("NVRAM loaded from: " + currentVpxFile.getExpectedNvramFile().getAbsolutePath());
    } else {
        textNvramOutput.setText("[ No NVRAM data found ]\n\n" +
                "Expected file: " + currentVpxFile.getExpectedNvramFile().getAbsolutePath() + "\n\n" +
                "Make sure the game has been played at least once so the NVRAM file is generated, " +
                "and that the ROM name is set correctly.");
        setStatus("NVRAM file not found for ROM: " + currentVpxFile.getRomName());
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Internal helpers
  // ─────────────────────────────────────────────────────────────────────────

  private void loadVpxFile(File file) {
      currentVpxFile = new VpxFile(file);
      textNvramOutput.clear();

      // Populate info labels
      labelFileName.setText(currentVpxFile.getFileName());
      labelFilePath.setText(currentVpxFile.getFilePath());
      labelFileSize.setText(currentVpxFile.getFileSizeFormatted());
      labelLastModified.setText(currentVpxFile.getLastModified());

      // Try to detect a VBS that already exists
      if (currentVpxFile.getExpectedVbsFile().exists()) {
          currentVpxFile.setVbsExtracted(true);
      }
      refreshVbsStatus();

      // Pre-fill ROM name with the base filename as a sensible default
      textRomName.setText(currentVpxFile.getBaseName());
      currentVpxFile.setRomName(currentVpxFile.getBaseName());

      setFileLoaded(true);
      setStatus("Loaded: " + currentVpxFile.getFileName());
  }

  private void refreshVbsStatus() {
      if (currentVpxFile == null) return;
      if (currentVpxFile.isVbsExtracted() || currentVpxFile.getExpectedVbsFile().exists()) {
          labelVbsStatus.setText("✔  VBS script present → " + currentVpxFile.getExpectedVbsFile().getName());
          labelVbsStatus.getStyleClass().removeAll("vbs-missing");
          labelVbsStatus.getStyleClass().add("vbs-present");
      } else {
          labelVbsStatus.setText("✘  VBS script not yet extracted");
          labelVbsStatus.getStyleClass().removeAll("vbs-present");
          labelVbsStatus.getStyleClass().add("vbs-missing");
      }
  }

  private void setFileLoaded(boolean loaded) {
      emptyStatePane.setVisible(!loaded);
      emptyStatePane.setManaged(!loaded);
      fileInfoPane.setVisible(loaded);
      fileInfoPane.setManaged(loaded);

      btnCloseFile.setDisable(!loaded);
      btnLaunchGame.setDisable(!loaded);
      btnStopGame.setDisable(!loaded);
      btnExtractVbs.setDisable(!loaded);
      btnAlternateRom.setDisable(!loaded);
      btnLoadNvram.setDisable(!loaded);
      textRomName.setDisable(!loaded);
  }

  private void setStatus(String message) {
      Platform.runLater(() -> labelStatus.setText(message));
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
      Alert alert = new Alert(type);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(content);
      applyDialogStyle(alert);
      alert.showAndWait();
  }

  private void applyDialogStyle(Dialog<?> dialog) {
      try {
          dialog.getDialogPane().getStylesheets().add(
                  getClass().getResource("/css/style.css").toExternalForm());
          dialog.getDialogPane().getStyleClass().add("dialog-pane");
      } catch (Exception ignored) {}
  }

  @FXML
  private void clearNvram() {
      textNvramOutput.clear();
      setStatus("NVRAM output cleared.");
  }
}
