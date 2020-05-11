package com.hoc081098.stateflowsample

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hoc081098.stateflowsample.MainState.WatchState
import com.hoc081098.stateflowsample.databinding.ActivityMainBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity() {
  private val vm by viewModels<MainVM>()
  private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    lifecycleScope.launchWhenStarted {
      vm.stateFlow
        .onEach { render(it) }
        .catch { }
        .collect()
    }

    actionFlow()
      .onEach { vm.process(it) }
      .catch { }
      .launchIn(lifecycleScope)
  }

  private fun actionFlow(): Flow<MainAction> {
    return merge(
      binding.buttonStart.clicks().map { MainAction.START },
      binding.buttonPause.clicks().map { MainAction.PAUSE },
      binding.buttonReset.clicks().map { MainAction.RESET },
    )
  }

  private fun render(state: MainState) {
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

@CheckResult
@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> {
  return callbackFlow {
    setOnClickListener { offer(Unit) }
    awaitClose { setOnClickListener(null) }
  }
}