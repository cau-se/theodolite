package theodolite.benchmark

import io.fabric8.mockwebserver.internal.WebSocketMessage
import java.nio.charset.StandardCharsets

class ErrorChannelMessage(body: String) : WebSocketMessage(0L, getBodyBytes(OUT_STREAM_ID, body), true, true) {
    companion object {
        private const val OUT_STREAM_ID: Byte = 3
        private fun getBodyBytes(prefix: Byte, body: String): ByteArray {
            val original = body.toByteArray(StandardCharsets.UTF_8)
            val prefixed = ByteArray(original.size + 1)
            prefixed[0] = prefix
            System.arraycopy(original, 0, prefixed, 1, original.size)
            return prefixed
        }
    }
}
