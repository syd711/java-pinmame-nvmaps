# VPX Manager

A JavaFX desktop application for managing Visual Pinball X table files.

## Features

| Button | What it does |
|--------|-------------|
| **Open File** | Opens a file-picker filtered to `.vpx` files and loads its metadata |
| **Close File** | Empties the UI without closing the window |
| **Launch Game** | Hands off to `VpxService.launchGame()` — wire in your VPinballX call |
| **Extract VBS Script** | Calls `VpxService.extractVbs()` and reports the output path; the VBS status label turns green when the file exists |
| **Alternate ROM** | Dialog to replace the ROM name inside the extracted VBS script |
| **Load NVRAM** | Calls `VpxService.parseNvram()` and displays the result in the lower text area |

---

## Project Structure

```
vpx-manager/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/vpxmanager/
    │       ├── App.java                     ← JavaFX entry point
    │       ├── controller/
    │       │   └── MainController.java      ← UI logic / FXML controller
    │       ├── model/
    │       │   └── VpxFile.java             ← VPX file data model
    │       └── service/
    │           └── VpxService.java          ← ★ YOUR processing logic goes here
    └── resources/
        ├── fxml/MainView.fxml               ← UI layout
        └── css/style.css                    ← Dark industrial theme
```

---

## Build & Run

### Prerequisites
- JDK 17 or later
- Maven 3.8+

### Run directly
```bash
mvn javafx:run
```

### Build fat JAR
```bash
mvn package
java -jar target/vpx-manager-1.0.0-shaded.jar
```

---

## Wiring In Your Processing Logic

All operations are stubs in `VpxService.java`. Each method is clearly documented:

```java
// Launch the game
public void launchGame(VpxFile vpxFile) { ... }

// Extract the embedded VBS script
public boolean extractVbs(VpxFile vpxFile) { ... }

// Replace ROM name in the VBS file
public boolean replaceRomInScript(VpxFile vpxFile, String alternateRom) { ... }

// Load and parse the NVRAM file – return human-readable string
public String parseNvram(VpxFile vpxFile) { ... }
```

The `VpxFile` model exposes:
- `getFilePath()` / `getDirectory()` / `getBaseName()`
- `getExpectedVbsFile()` — same dir, `.vbs` extension
- `getExpectedNvramFile()` — based on ROM name, `.nv` extension
- `getRomName()` / `setRomName()`
