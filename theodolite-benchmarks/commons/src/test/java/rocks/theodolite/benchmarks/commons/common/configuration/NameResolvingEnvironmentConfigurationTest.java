package rocks.theodolite.benchmarks.commons.common.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.configuration2.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import rocks.theodolite.benchmarks.commons.commons.configuration.NameResolvingEnvironmentConfiguration;

public class NameResolvingEnvironmentConfigurationTest {

	private static final String PROPERTY_FILES_KEY = "my.env.var";
	private static final String ENV_VAR_KEY = "MY_ENV_VAR";
	private static final String STRING_VALUE = "value";
	private static final String STRING_VALUE_2 = "value2";
	private static final int INT_VALUE = 7;

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Test
	public void testHelperLibrary() {
		this.environmentVariables.clear("name");
		this.environmentVariables.set("name", STRING_VALUE);
		assertEquals("value", System.getenv("name"));
	}

	@Test
	public void testGetUsingEnvVarFormat() {
		this.environmentVariables.clear(ENV_VAR_KEY);
		this.environmentVariables.set(ENV_VAR_KEY, STRING_VALUE);
		final Configuration config = new NameResolvingEnvironmentConfiguration();
		final String result = config.getString(ENV_VAR_KEY);
		assertEquals(STRING_VALUE, result);
	}

	@Test
	public void testGetUsingPropertiesFormat() {
		this.environmentVariables.clear(ENV_VAR_KEY);
		this.environmentVariables.set(ENV_VAR_KEY, STRING_VALUE);
		final Configuration config = new NameResolvingEnvironmentConfiguration();
		final String result = config.getString(PROPERTY_FILES_KEY);
		assertEquals(STRING_VALUE, result);
	}

	@Test
	public void testGetOfNumber() {
		this.environmentVariables.clear(ENV_VAR_KEY);
		this.environmentVariables.set(ENV_VAR_KEY, String.valueOf(INT_VALUE));
		final Configuration config = new NameResolvingEnvironmentConfiguration();
		final int result = config.getInt(PROPERTY_FILES_KEY);
		assertEquals(INT_VALUE, result);
	}

	@Test
	public void testGetOfBothExisting() {
		this.environmentVariables.clear(ENV_VAR_KEY, PROPERTY_FILES_KEY);
		this.environmentVariables.set(ENV_VAR_KEY, STRING_VALUE);
		this.environmentVariables.set(PROPERTY_FILES_KEY, STRING_VALUE_2);
		final Configuration config = new NameResolvingEnvironmentConfiguration();
		final String result = config.getString(PROPERTY_FILES_KEY);
		assertEquals(STRING_VALUE_2, result);
	}

	@Test
	public void testGetNonExistingUsingEnvVarFormat() {
		this.environmentVariables.clear(ENV_VAR_KEY);
		final Configuration config = new NameResolvingEnvironmentConfiguration();
		final String result = config.getString(ENV_VAR_KEY);
		assertNull(result);
	}

	@Test
	public void testGetNonExistingUsingPropertiesFormat() {
		this.environmentVariables.clear(ENV_VAR_KEY);
		final Configuration config = new NameResolvingEnvironmentConfiguration();
		final String result = config.getString(PROPERTY_FILES_KEY);
		assertNull(result);
	}

	@Test
	public void testFormatKeyAsEnvVariable() {
		assertEquals(ENV_VAR_KEY, NameResolvingEnvironmentConfiguration.formatKeyAsEnvVariable(PROPERTY_FILES_KEY));
	}

}
