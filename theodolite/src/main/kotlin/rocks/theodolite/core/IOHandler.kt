package rocks.theodolite.core

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

        if (resultsFolder != "") {
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
        return resultsFolder
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
     * @param data the data to write in the csv, as list of list,
     *             each sublist corresponds to a row in the CSV file
     * @param columns name of the columns
     */
    fun writeToCSVFile(fileURL: String, data: List<List<String>>, columns: List<String>) {
        val outputFile = File("$fileURL.csv")
        PrintWriter(outputFile).use { pw ->

            val writeColumns = addQuotationMarks(columns)
            pw.println(writeColumns.joinToString(separator = ","))

            data.forEach{ row ->
                val writeRow = addQuotationMarks(row)
                pw.println(writeRow.joinToString(separator = ","))
            }
        }
        logger.info { "Wrote CSV file: $fileURL to ${outputFile.absolutePath}." }
    }

    /**
     * For a list of Strings:
     *  - adds additional quotation mark to existing one
     *  - adds quotation marks around entries that contain a comma
     */
    private fun addQuotationMarks(stringList: List<String> ): List<String> {
        val stringMutableList = stringList.toMutableList()
        stringMutableList.forEachIndexed { index, entry ->
            // add additional quotation marks to escape them in csv
            if (entry.contains("\"")){
                stringMutableList[index] = stringMutableList[index].replace('"'+"", "\"" + '"')
            }

            // add quotation marks around entries that contain a comma
            if (entry.contains(",")){
                stringMutableList[index] = '"' + stringMutableList[index] + '"'
            }
        }
        return stringMutableList
    }

    /**
     * Write to text file
     *
     * @param fileURL the URL of the file
     * @param data the data to write in the file as String
     */
    fun writeStringToTextFile(fileURL: String, data: String) {
        val outputFile = File(fileURL)
        outputFile.printWriter().use {
            it.println(data)
        }
        logger.info { "Wrote txt file: $fileURL to ${outputFile.absolutePath}." }
    }
}