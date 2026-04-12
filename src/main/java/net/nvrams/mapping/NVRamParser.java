package net.nvrams.mapping;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface NVRamParser {

  List<String> getSupportedNVRams() throws IOException;

  List<Score> parseNvRam(File nvRam, Locale locale) throws IOException;

  String getRaw(File nvRam, Locale locale) throws IOException;
}
