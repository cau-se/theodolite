package theodolite.model.crd

import io.fabric8.kubernetes.api.model.KubernetesResourceList
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.apache.commons.lang3.builder.EqualsBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import theodolite.benchmark.BenchmarkExecution
import theodolite.execution.operator.*
import theodolite.util.ConfigurationOverride
import java.io.FileInputStream


// TODO move somewhere else
typealias ExecutionClient = MixedOperation<ExecutionCRD, KubernetesResourceList<ExecutionCRD>, Resource<ExecutionCRD>>

@WithKubernetesTestServer
@QuarkusTest
internal class CRDExecutionTest {

     @KubernetesTestServer
     private lateinit var server: KubernetesServer

     lateinit var executionClient: ExecutionClient

     lateinit var controller: TheodoliteController

     lateinit var stateHandler: ExecutionStateHandler

     lateinit var eventHandler: ExecutionEventHandler

     @BeforeEach
     fun setUp() {
          server.before()

          this.server.client
                  .apiextensions().v1()
                  .customResourceDefinitions()
                  .load(FileInputStream("crd/crd-execution.yaml"))
                  .create()

          this.executionClient = this.server.client.resources(ExecutionCRD::class.java)

          this.controller = mock()
          this.stateHandler = ExecutionStateHandler(server.client)
          this.eventHandler = ExecutionEventHandler(this.controller, this.stateHandler)
     }

     @AfterEach
     fun tearDown() {
          server.after()
     }

     @Test
     fun checkParsingCRDTest(){
          // BenchmarkExecution from yaml
          val execution = executionClient.load(ClassLoader.getSystemResourceAsStream("k8s-resource-files/test-execution.yaml")).create().spec

          assertEquals(0, execution.executionId)
          assertEquals("test", execution.name)
          assertEquals("uc1-kstreams", execution.benchmark)
          assertEquals(mutableListOf<ConfigurationOverride?>(), execution.configOverrides)

          assertEquals("NumSensors", execution.loads.loadType)
          assertEquals(listOf(25000, 50000, 75000, 100000, 125000, 150000),execution.loads.loadValues)

          assertEquals("Instances", execution.resources.resourceType)
          assertEquals(listOf(1, 2, 3, 4, 5), execution.resources.resourceValues)

          assertEquals("demand", execution.execution.metric)
          assertEquals(300, execution.execution.duration)
          assertEquals(1, execution.execution.repetitions)

          assertEquals("RestrictionSearch", execution.execution.strategy.name)
          assertEquals("LinearSearch", execution.execution.strategy.searchStrategy)
          assertEquals(listOf("LowerBound"), execution.execution.strategy.restrictions)
     }
}