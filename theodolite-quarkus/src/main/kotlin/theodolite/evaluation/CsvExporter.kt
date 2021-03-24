package theodolite.evaluation

import theodolite.util.PrometheusResponse
import java.io.File
import java.io.PrintWriter

class CsvExporter {

    /**
     * Uses the PrintWriter to transform a PrometheusResponse to Csv
     */
    fun toCsv(name : String,prom: PrometheusResponse){
        val x = toArray(prom)
        val csvOutputFile: File = File(name+".csv")

        PrintWriter(csvOutputFile).use { pw ->
            pw.println(listOf("name","time","value").joinToString())
            x.forEach{
                pw.println(it.joinToString())
            }
        }
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
