package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ConfigMapPropertiesPatcherTest {

    private lateinit var configMap: ConfigMap

    private val patcher = ConfigMapPropertiesPatcher("some-file.properties", "second.prop.value")

    @BeforeEach
    fun setUp() {
        val data = mapOf(
            "some-file.properties" to """
                first.properties.value = some-test
                second.prop.value = 1
                third = 1234
            """.trimIndent()
        )

        this.configMap = ConfigMapBuilder()
            .withNewMetadata()
            .withName("example")
            .endMetadata()
            .addToData(data)
            .build()
    }

    @ParameterizedTest
    @ValueSource(strings = ["some-string", "42", "42.42", "true"])
    fun setSettingString(inputValue: String) {
        val patched = patcher.patchSingleResource(this.configMap, inputValue)
        assertTrue(patched is ConfigMap)
        //patched.let { it as ConfigMap }.data
        patched as ConfigMap
        val properties = patched.data["some-file.properties"]
        assertTrue(properties != null)
        val matchLines = properties!!.lines().filter { it.startsWith("second.prop.value") }
        assertEquals(1, matchLines.size)
        val value = matchLines[0].split("=").getOrNull(1)
        assertEquals(inputValue, value)
    }


}