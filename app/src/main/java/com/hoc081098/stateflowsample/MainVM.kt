package com.hoc081098.stateflowsample

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.concatWith
import com.hoc081098.flowext.interval
import com.hoc081098.stateflowsample.utils.selfReferenced
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@FlowPreview
@ExperimentalCoroutinesApi
class MainVM : ViewModel() {
  private val actionSharedFlow = MutableSharedFlow<MainAction>()

  val stateFlow: StateFlow<MainState> by selfReferenced {
    val initialState = MainState()

    actionSharedFlow
      .flatMapLatest { action ->
        when (action) {
          MainAction.START -> {
            tickerFlow(stateFlow.value.seconds)
              .map {
                MainState(
                  seconds = it,
                  watchState = MainState.WatchState.RUNNING,
                )
              }
              .concatWith(flowOf(initialState))
          }
          MainAction.PAUSE -> {
            flowOf(
              MainState(
                watchState = MainState.WatchState.PAUSED,
                seconds = stateFlow.value.seconds
              )
            )
          }
          MainAction.RESET -> flowOf(initialState)
        }
      }
      .onEach { Log.d("###", "Main state: $it") }
      .catch { Log.d("###", "Exception: $it") }
      .stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialState
      )
  }

  fun process(action: MainAction) = viewModelScope
    .launch { actionSharedFlow.emit(action) }
    .let { }

  private companion object {
    const val MAX_SECONDS = 10

    private fun tickerFlow(resume: Long): Flow<Long> =
      interval(initialDelay = 1.seconds, period = 1.seconds)
        .map { it + 1 + resume }
        .takeWhile { it <= MAX_SECONDS }
  }
}
