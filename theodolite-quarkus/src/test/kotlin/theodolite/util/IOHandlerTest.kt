package theodolite.util

import com.google.gson.GsonBuilder
import io.quarkus.test.junit.QuarkusTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.rules.TemporaryFolder


const val FOLDER_URL = "Test-Folder"
@QuarkusTest
internal class IOHandlerTest {

    @Rule
    var temporaryFolder = TemporaryFolder()

    @Test
    fun testWriteStringToText() {
        temporaryFolder.create()
        val testContent = "Test-File-Content"
        val folder = temporaryFolder.newFolder(FOLDER_URL)

        IOHandler().writeStringToTextFile(
            fileURL = "${folder.absolutePath}/test-file.txt",
            data = testContent)

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
            listOf("apples","red"),
            listOf("bananas","yellow"),
            listOf("avocado","brown"))
        val columns = listOf("Fruit", "Color")

        IOHandler().writeToCSVFile(
            fileURL = "${folder.absolutePath}/test-file",
            data = testContent,
            columns = columns)

        var expected = "Fruit,Color\n"
        testContent.forEach { expected += it[0] + "," +  it[1] + "\n" }

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
            fileURL =  "${folder.absolutePath}/test-file.json",
            objectToSave = testContent)

        val expected = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create().toJson(testContent)

        assertEquals(
            expected,
            IOHandler().readFileAsString("${folder.absolutePath}/test-file.json")
        )
    }
}
