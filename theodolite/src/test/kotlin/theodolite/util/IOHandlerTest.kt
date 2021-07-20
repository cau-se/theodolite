package theodolite.util

import com.google.gson.GsonBuilder
import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.rules.TemporaryFolder
import org.junitpioneer.jupiter.ClearEnvironmentVariable
import org.junitpioneer.jupiter.SetEnvironmentVariable


const val FOLDER_URL = "Test-Folder"

@QuarkusTest
internal class IOHandlerTest {

    @Rule
    private var temporaryFolder = TemporaryFolder()

    @Test
    fun testWriteStringToText() {
        temporaryFolder.create()
        val testContent = "Test-File-Content"
        val folder = temporaryFolder.newFolder(FOLDER_URL)

        IOHandler().writeStringToTextFile(
            fileURL = "${folder.absolutePath}/test-file.txt",
            data = testContent
        )

        assertEquals(
            testContent,
            IOHandler().readFileAsString("${folder.absolutePath}/test-file.txt")
        )
    }

    @Test
    fun testWriteToCSVFile() {
        temporaryFolder.create()
        val folder = temporaryFolder.newFolder(FOLDER_URL)

        val testContent = listOf(
            listOf("apples", "red"),
            listOf("bananas", "yellow"),
            listOf("avocado", "brown")
        )
        val columns = listOf("Fruit", "Color")

        IOHandler().writeToCSVFile(
            fileURL = "${folder.absolutePath}/test-file",
            data = testContent,
            columns = columns
        )

        var expected = "Fruit,Color\n"
        testContent.forEach { expected += it[0] + "," + it[1] + "\n" }

        assertEquals(
            expected.trim(),
            IOHandler().readFileAsString("${folder.absolutePath}/test-file.csv")
        )
    }

    @Test
    fun testWriteToJSONFile() {
        temporaryFolder.create()
        val folder = temporaryFolder.newFolder(FOLDER_URL)
        val testContent = Resource(0, emptyList())

        IOHandler().writeToJSONFile(
            fileURL = "${folder.absolutePath}/test-file.json",
            objectToSave = testContent
        )

        val expected = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create().toJson(testContent)

        assertEquals(
            expected,
            IOHandler().readFileAsString("${folder.absolutePath}/test-file.json")
        )
    }

    // Test the function `getResultFolderString`

    @Test
    @ClearEnvironmentVariable.ClearEnvironmentVariables(
        ClearEnvironmentVariable(key = "RESULTS_FOLDER"),
        ClearEnvironmentVariable(key = "CREATE_RESULTS_FOLDER")
    )
    fun testGetResultFolderURL_emptyEnvironmentVars() {
        assertEquals("", IOHandler().getResultFolderURL())
    }


    @Test()
    @SetEnvironmentVariable.SetEnvironmentVariables(
        SetEnvironmentVariable(key = "RESULTS_FOLDER", value = "./src/test/resources"),
        SetEnvironmentVariable(key = "CREATE_RESULTS_FOLDER", value = "false")
    )
    fun testGetResultFolderURL_FolderExist() {
        assertEquals("./src/test/resources/", IOHandler().getResultFolderURL())
    }

    @Test()
    @SetEnvironmentVariable.SetEnvironmentVariables(
        SetEnvironmentVariable(key = "RESULTS_FOLDER", value = "$FOLDER_URL-0"),
        SetEnvironmentVariable(key = "CREATE_RESULTS_FOLDER", value = "false")
    )
    fun testGetResultFolderURL_FolderNotExist() {
        var exceptionWasThrown = false
        try {
            IOHandler().getResultFolderURL()
        } catch (e: Exception) {
            exceptionWasThrown = true
            assertThat(e.toString(), containsString("Result folder not found"))
        }
        assertTrue(exceptionWasThrown)
    }

    @Test()
    @SetEnvironmentVariable.SetEnvironmentVariables(
        SetEnvironmentVariable(key = "RESULTS_FOLDER", value = FOLDER_URL),
        SetEnvironmentVariable(key = "CREATE_RESULTS_FOLDER", value = "true")
    )
    fun testGetResultFolderURL_CreateFolderIfNotExist() {
        assertEquals("$FOLDER_URL/", IOHandler().getResultFolderURL())
    }

    @Test()
    @ClearEnvironmentVariable(key = "RESULTS_FOLDER")
    @SetEnvironmentVariable(key = "CREATE_RESULTS_FOLDER", value = "true")
    fun testGetResultFolderURL_CreateFolderButNoFolderGiven() {
        assertEquals("", IOHandler().getResultFolderURL())
    }
}
