package net.nvrams.mapping.superhac;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NVRamSection {

  @JsonProperty("title")
  private String title;

  @JsonProperty("entries")
  private List<NVRamEntry> entries;

  @JsonProperty("enabled_setting")
  private String enabledSetting;

  //-----------------------------

  public boolean isEnabled(Map<String, ?> settings) {
    if (enabledSetting != null) {
      Object flag = settings.get(enabledSetting);
      if (!(flag instanceof Boolean) || !(Boolean) flag) {
        return false;
      }
    }
    return true;
  }

  //-----------------------------

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<NVRamEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<NVRamEntry> entries) {
    this.entries = entries;
  }

  public String getEnabledSetting() {
    return enabledSetting;
  }

  public void setEnabledSetting(String enabledSetting) {
    this.enabledSetting = enabledSetting;
  } 
}
