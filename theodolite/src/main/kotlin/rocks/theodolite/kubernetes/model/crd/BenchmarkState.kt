package rocks.theodolite.kubernetes.model.crd

import com.fasterxml.jackson.annotation.JsonValue

enum class BenchmarkState(@JsonValue val value: String) {
    PENDING("Pending"),
    READY("Ready")
}