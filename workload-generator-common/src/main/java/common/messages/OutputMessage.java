package common.messages;

import titan.ccp.models.records.ActivePowerRecord;

public class OutputMessage {
  private String key;
  private ActivePowerRecord value;
  
  public OutputMessage(String key, ActivePowerRecord value) {
    super();
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public ActivePowerRecord getValue() {
    return value;
  }
  
}
