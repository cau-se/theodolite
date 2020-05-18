package common.functions;

import common.messages.OutputMessage;

@FunctionalInterface
public interface Transport {
  
  public void consume(final OutputMessage message);

}
