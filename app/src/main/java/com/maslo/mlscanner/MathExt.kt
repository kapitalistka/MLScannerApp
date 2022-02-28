package com.maslo.mlscanner

import kotlin.math.exp


fun softMax(params: FloatArray): FloatArray {
    var sum = 0.0
    for (i in params.indices) {
        params[i] = exp(params[i].toDouble()).toFloat()
        sum += params[i]
    }
    if (java.lang.Double.isNaN(sum) || sum < 0) {
        for (i in params.indices) {
            params[i] = (1.0 / params.size).toFloat()
        }
    } else {
        for (i in params.indices) {
            params[i] = (params[i] / sum).toFloat()
        }
    }
    return params
}