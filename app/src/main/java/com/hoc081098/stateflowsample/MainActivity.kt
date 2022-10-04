package com.hoc081098.stateflowsample

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hoc081098.stateflowsample.databinding.ActivityMainBinding
import com.hoc081098.stateflowsample.utils.collectIn
import kotlin.LazyThreadSafetyMode.NONE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity() {
  private val vm by viewModels<MainVM>()
  private val binding by lazy(NONE) { ActivityMainBinding.inflate(layoutInflater) }
  private val renderer by lazy(NONE) { MainRenderer(binding) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    bind()
  }

  private fun bind() {
    vm.stateFlow.collectIn(this, action = renderer::render)
    renderer
      .actionFlow()
      .onEach(vm::process)
      .launchIn(lifecycleScope)
  }
}
