package theodolite.evaluation

import mu.KotlinLogging
import theodolite.util.PrometheusResponse
import java.io.File
import java.io.PrintWriter
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 *  Used to document the data received from prometheus for additional offline analysis.
 */
class CsvExporter {

    /**
     * Uses the PrintWriter to transform a PrometheusResponse to a Csv file.
     * @param name of the file.
     * @param prom Response that is documented.
     *
     */
    fun toCsv(name: String, prom: PrometheusResponse) {
        val responseArray = promResponseToList(prom)
        val csvOutputFile = File("$name.csv")

        PrintWriter(csvOutputFile).use { pw ->
            pw.println(listOf("name", "time", "value").joinToString())
            responseArray.forEach {
                pw.println(it.joinToString())
            }
        }
        logger.info { "Wrote csv file: $name to ${csvOutputFile.absolutePath}" }
    }

    /**
     * Converts a PrometheusResponse into a List of List of Strings
     */
    private fun promResponseToList(prom: PrometheusResponse): List<List<String>> {
        val name = prom.data?.result?.get(0)?.metric?.group.toString()
        val values = prom.data?.result?.get(0)?.values
        val dataList = mutableListOf<List<String>>()

        if (values != null) {
            for (x in values) {
                val y = x as List<*>
                dataList.add(listOf(name, "${y[0]}", "${y[1]}"))
            }
        }
        return Collections.unmodifiableList(dataList)
    }
}
