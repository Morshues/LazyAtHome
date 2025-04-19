package com.morshues.lazyathome.ui.common

data class RowInfo(
    val id: String,
    val controllerProvider: () -> BaseRowController
)