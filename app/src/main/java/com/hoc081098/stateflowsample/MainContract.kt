package com.hoc081098.stateflowsample

data class MainState(
  val watchState: WatchState = WatchState.IDLE,
  val seconds: Long = 0
) {
  enum class WatchState {
    RUNNING,
    PAUSED,
    IDLE
  }
}

enum class MainAction {
  START,
  PAUSE,
  RESET
}
