package theodolite.evaluation

import mu.KotlinLogging
import theodolite.util.PrometheusResponse
import java.io.File
import java.io.PrintWriter

private val logger = KotlinLogging.logger {}

class CsvExporter {

    /**
     * Uses the PrintWriter to transform a PrometheusResponse to Csv
     */
    fun toCsv(name : String,prom: PrometheusResponse){
        val responseArray = toArray(prom)
        val csvOutputFile: File = File("$name.csv")

        PrintWriter(csvOutputFile).use { pw ->
            pw.println(listOf("name","time","value").joinToString())
            responseArray.forEach{
                pw.println(it.joinToString())
            }
        }
        logger.debug{csvOutputFile.absolutePath}
        logger.info { "Wrote csv to $name" }
    }

    /**
     * Converts a PrometheusResponse into a List of List of Strings
     */
    private fun toArray(prom : PrometheusResponse): MutableList<List<String>> {
        val name = prom.data?.result?.get(0)?.metric?.group.toString()
        val values = prom.data?.result?.get(0)?.values
        val dataList = mutableListOf<List<String>>()

        if (values != null) {
            for (x in values){
                val y = x as List<*>
                dataList.add(listOf(name,"${y[0]}","${y[1]}"))
            }
        }
        return dataList
    }
}
