package com.hoc081098.stateflowsample.utils

import android.view.View
import androidx.annotation.CheckResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@CheckResult
fun View.clicks(): Flow<Unit> = callbackFlow {
  setOnClickListener { trySend(Unit) }
  awaitClose { setOnClickListener(null) }
}