package org.example

import kotlinx.serialization.Serializable

@Serializable
data class BcbRate (
    val data: String,
    val valor: String
)