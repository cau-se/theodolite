package rocks.theodolite.kubernetes.patcher

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@QuarkusTest
internal class ConfigMapYamlPatcherTest {

    private lateinit var configMap: ConfigMap

    private val patcher = ConfigMapYamlPatcher("some-file.yaml", "second")

    @BeforeEach
    fun setUp() {
        val data = mapOf(
            "some-file.yaml" to """
                first: some-test
                # some comment: with colon
                second: 1
                third: 1234
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
        val yaml = patched.data["some-file.yaml"]
        assertTrue(yaml != null)
        val line = yaml!!.lines().getOrNull(1)
        assertTrue(line != null)
        val value = line!!.split(": ").getOrNull(1)
        assertEquals(inputValue, value)
    }


}