package theodolite.commons.commons.configuration;

import java.util.Locale;
import org.apache.commons.configuration2.EnvironmentConfiguration;

/**
 * {@link EnvironmentConfiguration} that automatically translates Java property file style variables
 * ({@code my.variable.name}) to environment style variables ({@code MY__VARIABLE_NAME}).
 */
public class NameResolvingEnvironmentConfiguration extends EnvironmentConfiguration {

  @Override
  protected Object getPropertyInternal(final String key) {
    final Object value = super.getPropertyInternal(key);
    if (value == null) {
      return super.getPropertyInternal(formatKeyAsEnvVariable(key));
    }
    return value;
  }

  @Override
  protected boolean containsKeyInternal(final String key) {
    final boolean value = super.containsKeyInternal(key);
    if (!value) {
      return super.containsKeyInternal(formatKeyAsEnvVariable(key));
    }
    return value;
  }

  public static String formatKeyAsEnvVariable(final String key) {
    return key.toUpperCase(Locale.ROOT).replace('.', '_');
  }

}
