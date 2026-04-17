package net.nvrams.mapping.superhac;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.nvrams.mapping.NVRamParser;
import net.nvrams.mapping.NVRamScore;

/**
 * 
 */
@Service
public class NVRamSuperhacParser implements NVRamParser {
  private final static Logger LOG = LoggerFactory.getLogger(NVRamSuperhacParser.class);

  private Map<String, NVRamMap> _cacheMapForRom;

  
  @Override
  public List<String> getSupportedNVRams() {
    try {
      ensureCacheMapForRom();
      return new ArrayList<>(_cacheMapForRom.keySet());
    }
    catch (IOException ioe) {
      LOG.error("Cannot get supported NVRams: {}", ioe.getMessage());
      return Collections.emptyList();
    }
  }

    @Override
  public List<String> getRaw(String rom, File nvRam, Locale locale) throws IOException {
    ensureCacheMapForRom();

    NVRamMap map = _cacheMapForRom.get(rom);
    if (map != null) {
      byte[] data = Files.readAllBytes(nvRam.toPath());
      return map.getRaw(data, locale);
    }
    return null;
  }

  @Override
  public List<NVRamScore> parseNvRam(String rom, File nvRam, Locale locale, boolean parseAll) throws IOException {
    ensureCacheMapForRom();

    NVRamMap map = _cacheMapForRom.get(rom);
    if (map != null) {
      byte[] data = Files.readAllBytes(nvRam.toPath());
      return map.parseScores(data, locale, parseAll);
    }
    return Collections.emptyList();
  }

  @Override
  public List<NVRamScore> parseRaw(String rom, List<String> lines, Locale locale, boolean parseAll) throws IOException {
    ensureCacheMapForRom();

    NVRamMap map = _cacheMapForRom.get(rom);
    if (map != null) {
      return map.parseRaw(lines, locale, parseAll);
    }
    return Collections.emptyList();
  }


  //------------------------------

  private void ensureCacheMapForRom() throws IOException {
    if (_cacheMapForRom == null) {
      LOG.info("Load cache of rom map from classpath resources");
      try (InputStream in = getClass().getResourceAsStream("/net/nvrams/mapping/superhac/roms.json")) {
        if (in == null) {
          throw new IOException("roms.json not found in classpath");
        }
        ObjectMapper mapper = new ObjectMapper();
        _cacheMapForRom = mapper.readValue(in, new TypeReference<Map<String, NVRamMap>>() {});
      }
      LOG.info("Rom Cache loaded with {} roms", _cacheMapForRom.size());
    }
  }
}
