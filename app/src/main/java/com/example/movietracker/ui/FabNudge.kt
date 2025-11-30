package com.example.movietracker.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Simple, wiggle animation for a FAB-like view.
 */
fun View.wiggle(duration: Long = 650L) {
  if (!isShown || !isAttachedToWindow) return
  if (!ValueAnimator.areAnimatorsEnabled()) return

  val rotateLeft =
    ObjectAnimator.ofFloat(this, View.ROTATION, 0f, -12f).apply { this.duration = duration / 3 }
  val rotateRight =
    ObjectAnimator.ofFloat(this, View.ROTATION, -12f, 12f).apply { this.duration = duration / 3 }
  val rotateCenter =
    ObjectAnimator.ofFloat(this, View.ROTATION, 12f, 0f).apply { this.duration = duration / 3 }

  val scaleXUp =
    ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 1.06f).apply { this.duration = duration / 6 }
  val scaleXDown =
    ObjectAnimator.ofFloat(this, View.SCALE_X, 1.06f, 1f).apply { this.duration = duration / 6 }
  val scaleYUp =
    ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, 1.06f).apply { this.duration = duration / 6 }
  val scaleYDown =
    ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.06f, 1f).apply { this.duration = duration / 6 }

  AnimatorSet().apply {
    playTogether(scaleXUp, scaleYUp)
    playSequentially(
      rotateLeft,
      rotateRight,
      rotateCenter,
      AnimatorSet().apply { playTogether(scaleXDown, scaleYDown) }
    )
  }.start()
}

/**
 * Schedule a one-time wiggle after [initialDelayMs] while this Fragment's view is alive.
 */
fun Fragment.scheduleFabWiggle(fab: View, initialDelayMs: Long = 30_000L) {
  viewLifecycleOwner.lifecycleScope.launch {
    // Wait for the delay; cancel automatically when view lifecycle is destroyed
    delay(initialDelayMs)
    if (!isActive) return@launch
    if (!isAdded) return@launch
    if (fab.isShown && fab.isAttachedToWindow) {
      fab.wiggle()
    }
  }
}
