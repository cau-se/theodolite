package theodolite.util

import com.google.gson.GsonBuilder
import mu.KotlinLogging
import java.io.File
import java.io.PrintWriter

private val logger = KotlinLogging.logger {}

/**
 * The IOHandler handles most common I/O operations within the Theodolite framework
 */
class IOHandler {

    /**
     * The location in which Theodolite store result and configuration file are depends on
     * the values of the environment variables `RESULT_FOLDER` and `CREATE_RESULTS_FOLDER`
     *
     * @return the URL of the result folder
     */
    fun getResultFolderURL(): String {
        var resultsFolder: String = System.getenv("RESULTS_FOLDER") ?: ""
        val createResultsFolder = System.getenv("CREATE_RESULTS_FOLDER") ?: "false"

        if (resultsFolder != ""){
            logger.info { "RESULT_FOLDER: $resultsFolder" }
            val directory = File(resultsFolder)
            if (!directory.exists()) {
                logger.error { "Folder $resultsFolder does not exist" }
                if (createResultsFolder.toBoolean()) {
                    directory.mkdirs()
                } else {
                    throw IllegalArgumentException("Result folder not found")
                }
            }
            resultsFolder += "/"
        }
        return  resultsFolder
    }

    /**
     * Read a file as String
     *
     * @param fileURL the URL of the file
     * @return The content of the file as String
     */
    fun readFileAsString(fileURL: String): String {
        return File(fileURL).inputStream().readBytes().toString(Charsets.UTF_8).trim()
    }

    /**
     * Creates a JSON string of the given object and store them to file
     *
     * @param T class of the object to save
     * @param objectToSave object which should be saved as file
     * @param fileURL the URL of the file
     */
    fun <T> writeToJSONFile(objectToSave: T, fileURL: String) {
        val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()
        writeStringToTextFile(fileURL, gson.toJson(objectToSave))
    }

    /**
     * Write to CSV file
     *
     * @param fileURL the URL of the file
     * @param data  the data to write in the file, as list of list, each subList corresponds to a row in the CSV file
     * @param columns columns of the CSV file
     */
    fun writeToCSVFile(fileURL: String, data: List<List<String>>, columns: List<String>) {
        val outputFile = File("$fileURL.csv")
        PrintWriter(outputFile).use { pw ->
            pw.println(columns.joinToString(separator=","))
            data.forEach {
                pw.println(it.joinToString(separator=","))
            }
        }
        logger.info { "Wrote CSV file: $fileURL to ${outputFile.absolutePath}." }
    }

    /**
     * Write to text file
     *
     * @param fileURL the URL of the file
     * @param data the data to write in the file as String
     */
    fun writeStringToTextFile(fileURL: String, data: String) {
        val outputFile = File("$fileURL")
        outputFile.printWriter().use {
                it.println(data)
        }
        logger.info { "Wrote txt file: $fileURL to ${outputFile.absolutePath}." }
    }
}