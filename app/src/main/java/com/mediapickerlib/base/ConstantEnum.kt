package com.mediapickerlib.base

enum class SourceType(val value: String) {
    CAMERA("camera"), GALLERY("gallery"), BOTH("both")
}

enum class MediaType(val value: String) {
    IMAGE("image"), VIDEO("video"), BOTH("both")
}

enum class SelectMedia(val value: String) {
    SINGLE("single"), MULTIPLE("multiple")
}

