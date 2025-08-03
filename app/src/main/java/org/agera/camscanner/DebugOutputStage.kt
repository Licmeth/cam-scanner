package org.agera.camscanner

enum class DebugOutputStage(val value: Int) {
    PREPROCESSED(0),
    CONTENT_REMOVED(1),
    EDGES_DETECTED(2),
    CONTOURS_DETECTED(3),
    CORNERS_DETECTED(4)
}