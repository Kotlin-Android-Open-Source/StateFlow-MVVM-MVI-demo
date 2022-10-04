package com.hoc081098.stateflowsample

import com.hoc081098.flowext.mapTo
import com.hoc081098.stateflowsample.MainState.WatchState
import com.hoc081098.stateflowsample.databinding.ActivityMainBinding
import com.hoc081098.stateflowsample.utils.clicks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

class MainRenderer(private val binding: ActivityMainBinding) {
  fun actionFlow(): Flow<MainAction> = merge(
    binding.buttonStart.clicks().mapTo(MainAction.START),
    binding.buttonPause.clicks().mapTo(MainAction.PAUSE),
    binding.buttonReset.clicks().mapTo(MainAction.RESET)
  )

  fun render(state: MainState) {
    val mm = (state.seconds / 60).toString().padStart(2, '0')
    val ss = (state.seconds % 60).toString().padStart(2, '0')
    binding.textView.text = "$mm:$ss"

    when (state.watchState) {
      WatchState.RUNNING -> {
        binding.buttonStart.run {
          isEnabled = false
          text = "START"
        }
        binding.buttonPause.isEnabled = true
        binding.buttonReset.isEnabled = true
      }
      WatchState.PAUSED -> {
        binding.buttonStart.run {
          isEnabled = true
          text = "RESUME"
        }
        binding.buttonPause.isEnabled = false
        binding.buttonReset.isEnabled = true
      }
      WatchState.IDLE -> {
        binding.buttonStart.run {
          isEnabled = true
          text = "START"
        }
        binding.buttonPause.isEnabled = false
        binding.buttonReset.isEnabled = false
      }
    }
  }
}
