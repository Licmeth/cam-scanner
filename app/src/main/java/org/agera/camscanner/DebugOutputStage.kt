package org.agera.camscanner

enum class DebugOutputStage(val value: Int) {
    PREPROCESSED(0),
    CONTENT_REMOVED(1),
    EDGES_DETECTED(2),
    FINAL_OUTPUT(3)
}