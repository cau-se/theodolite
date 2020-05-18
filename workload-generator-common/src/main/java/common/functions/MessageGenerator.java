package common.functions;

import common.messages.OutputMessage;

@FunctionalInterface
public interface MessageGenerator {

  public OutputMessage generateMessage(final String key);
  
}
