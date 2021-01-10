package theodolite

class RunUc {
   fun waitExecution(executionMinutes: Int) {
    var milliToMinutes = 60000
    System.out.println("Wait while executing")
    for (i in 1.rangeTo(executionMinutes)) {
       Thread.sleep((milliToMinutes*i).toLong());
       System.out.println("Executed: "+i.toString()+" minutes")
    }

    System.out.println("Execution finished")
 }

    fun create_topics(topics: List<Pair<String,String>>){

    }

    fun delete_topics(topics: List<Pair<String,String>>){

    }

    fun reset_zookeeper(){

    }

    fun start_workload_generator(wg: String, dim_value:Integer, uc_id: String){

    }
}
