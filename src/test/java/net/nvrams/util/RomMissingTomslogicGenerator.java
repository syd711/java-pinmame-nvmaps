package net.nvrams.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generates rom-missing-tomlogic.md from rom-overview.md.
 *
 * Includes all ROMs that have an 'x' in the pinemHi column but not in the
 * tomslogic column, AND are not listed in the tomlogic GitHub maplist, and
 * adds an "nvram available" column indicating whether a matching .nv file
 * exists in the nvrams/ folder.
 *
 * rom-overview.md columns (pipe-separated):
 *   # | rom | Name | clone of | pinemHi | tomslogic | superhac
 *   0   1     2      3          4         5            6        (1-based pipe-split index)
 */
public class RomMissingTomslogicGenerator {

    private static final String TOMSLOGIC_MAPLIST_URL =
            "https://raw.githubusercontent.com/tomlogic/pinmame-nvram-maps/refs/heads/main/maplist.md";

    // Column indices in the pipe-split array (0 = leading empty before first |)
    private static final int COL_ROM      = 2;
    private static final int COL_PINEMHI  = 5;
    private static final int COL_TOMSLOGIC = 6;

    // "nvram available" is inserted at this index in the output
    private static final int COL_NVRAM_INSERT = 5;

    public static void main(String[] args) throws IOException {
        Path projectRoot = args.length > 0 ? Paths.get(args[0]) : Paths.get(".");
        generate(projectRoot);
    }

    public static void generate(Path projectRoot) throws IOException {
        Path overviewFile = projectRoot.resolve("rom-overview.md");
        Path nvramDir     = projectRoot.resolve("nvrams");
        Path outputFile   = projectRoot.resolve("rom-missing-tomlogic.md");

        Set<String> nvFiles = collectNvFiles(nvramDir);
        Set<String> tomslogicMapRoms = fetchTomslogicMapRoms(TOMSLOGIC_MAPLIST_URL);
        List<String> inputLines = Files.readAllLines(overviewFile);

        List<String> output = new ArrayList<>();
        output.add("# ROMs missing in tomslogic (available in pinemHi)");
        output.add("");

        boolean headerWritten = false;
        int[] colCounts = new int[4]; // nvram available, pinemHi, tomslogic, superhac
        int rowIndex = 0;

        for (String line : inputLines) {
            if (!line.startsWith("|")) {
                continue;
            }

            String[] parts = line.split("\\|", -1);

            if (isSeparatorRow(parts)) {
                parts = insertColumn(parts, COL_NVRAM_INSERT, " --- ");
                output.add(join(parts));
                continue;
            }

            if (!headerWritten) {
                parts[1] = " Index ";
                parts = insertColumn(parts, COL_NVRAM_INSERT, " nvram available ");
                output.add(join(parts));
                headerWritten = true;
                continue;
            }

            String pinemhi   = parts[COL_PINEMHI].trim();
            String tomslogic = parts[COL_TOMSLOGIC].trim();
            String rom = parts[COL_ROM].trim().toLowerCase();

            if (!pinemhi.equals("x") || tomslogic.equals("x") || tomslogicMapRoms.contains(rom)) {
                continue;
            }

            parts[1] = " " + (++rowIndex) + " ";
            String nvramValue = nvFiles.contains(rom) ? " x " : "  ";
            if (nvramValue.trim().equals("x"))   colCounts[0]++;
            if (pinemhi.equals("x"))             colCounts[1]++;
            if (tomslogic.equals("x"))           colCounts[2]++;
            if (parts.length > COL_TOMSLOGIC + 1 && parts[COL_TOMSLOGIC + 1].trim().equals("x")) colCounts[3]++;
            parts = insertColumn(parts, COL_NVRAM_INSERT, nvramValue);
            output.add(join(parts));
        }

        // Summary row with counts of 'x' for the last 4 columns
        String sumRow = "| | **Sum** | | | " + countCell(colCounts[0]) + " | " + countCell(colCounts[1])
                + " | " + countCell(colCounts[2]) + " | " + countCell(colCounts[3]) + "  |";
        output.add(sumRow);

        Files.write(outputFile, output);
        System.out.println("Written " + (output.size() - 5) + " ROMs to " + outputFile);
    }

    /**
     * Fetches the tomlogic maplist.md and returns the set of ROM names found in it.
     * ROM names appear as markdown italic: _romname_ at the end of each list entry.
     */
    private static Set<String> fetchTomslogicMapRoms(String url) throws IOException {
        Set<String> roms = new HashSet<>();
        Pattern pattern = Pattern.compile("_([a-z0-9_]+)_\\s*$");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(URI.create(url).toURL().openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = pattern.matcher(line.trim().toLowerCase());
                if (m.find()) {
                    roms.add(m.group(1));
                }
            }
        }
        System.out.println("Fetched " + roms.size() + " ROM names from tomlogic maplist.");
        return roms;
    }

    private static Set<String> collectNvFiles(Path nvramDir) throws IOException {
        if (!Files.isDirectory(nvramDir)) {
            return Set.of();
        }
        return Files.list(nvramDir)
                .map(p -> p.getFileName().toString())
                .filter(name -> name.endsWith(".nv"))
                .map(name -> name.substring(0, name.length() - 3).toLowerCase())
                .collect(Collectors.toSet());
    }

    private static boolean isSeparatorRow(String[] parts) {
        for (String part : parts) {
            if (part.trim().startsWith("---")) {
                return true;
            }
        }
        return false;
    }

    private static String[] insertColumn(String[] parts, int index, String value) {
        String[] result = new String[parts.length + 1];
        System.arraycopy(parts, 0, result, 0, index);
        result[index] = value;
        System.arraycopy(parts, index, result, index + 1, parts.length - index);
        return result;
    }

    private static String countCell(int count) {
        return count == 0 ? " " : " " + count + " ";
    }

    private static String join(String[] parts) {
        return String.join("|", parts);
    }
}
