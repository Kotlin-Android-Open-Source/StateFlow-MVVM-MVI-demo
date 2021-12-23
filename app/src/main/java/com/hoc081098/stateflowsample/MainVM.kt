package com.hoc081098.stateflowsample

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class MainVM : ViewModel() {
  private companion object {
    const val MAX_SECONDS = 10
  }

  private val actionChannel = Channel<MainAction>(Channel.BUFFERED)

  val stateFlow: StateFlow<MainState>

  suspend fun process(action: MainAction) = actionChannel.send(action)

  init {
    val initial = MainState()
    stateFlow = MutableStateFlow(initial)

    // keep resume seconds
    var resume = 0L

    actionChannel
      .consumeAsFlow()
      .onEach { action ->
        when (action) {
          MainAction.START -> Unit
          MainAction.PAUSE -> resume = stateFlow.value.seconds
          MainAction.RESET -> resume = 0
        }
      }
      .flatMapLatest { action ->
        when (action) {
          MainAction.START -> {
            generateSequence(resume + 1) { it + 1 }
              .asFlow()
              .onEach { delay(1_000) }
              .onStart { emit(resume) }
              .takeWhile { it <= MAX_SECONDS }
              .map {
                MainState(
                  seconds = it,
                  watchState = MainState.WatchState.RUNNING,
                )
              }
              .onCompletion { emit(initial) }
          }
          MainAction.PAUSE -> {
            flowOf(
              MainState(
                MainState.WatchState.PAUSED,
                resume
              )
            )
          }
          MainAction.RESET -> {
            flowOf(initial)
          }
        }
      }
      .onEach { stateFlow.value = it }
      .onEach { Log.d("###", "Main state: $it") }
      .catch { Log.d("###", "Exception: $it") }
      .launchIn(viewModelScope)
  }
}