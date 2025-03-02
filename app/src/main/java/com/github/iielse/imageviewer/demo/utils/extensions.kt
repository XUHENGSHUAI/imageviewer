package com.github.iielse.imageviewer.demo.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.github.iielse.imageviewer.demo.R
import com.github.iielse.imageviewer.demo.core.LifecycleDisposable
import io.reactivex.disposables.Disposable

fun View.setOnClickCallback(interval: Long = 500L, callback: (View) -> Unit) {
    if (!isClickable) isClickable = true
    if (!isFocusable) isFocusable = true
    setOnClickListener(object : View.OnClickListener {
        override fun onClick(v: View?) {
            v ?: return
            val lastClickedTimestamp = v.getTag(R.id.view_last_click_timestamp)?.toString()?.toLongOrNull() ?: 0L
            val currTimestamp = System.currentTimeMillis()
            if (currTimestamp - lastClickedTimestamp < interval) return
            v.setTag(R.id.view_last_click_timestamp, currTimestamp)
            callback(v)
        }
    })
}

fun Disposable.bindLifecycle(lifecycle: Lifecycle?) {
    if (lifecycle == null) return
    lifecycle.addObserver(LifecycleDisposable(lifecycle, this))
}

val View.activity: Activity?
    get() = getActivity(context)

// https://stackoverflow.com/questions/9273218/is-it-always-safe-to-cast-context-to-activity-within-view/45364110
private fun getActivity(context: Context?): Activity? {
    if (context == null) return null
    if (context is Activity) return context
    if (context is ContextWrapper) return getActivity(context.baseContext)
    return null
}

val View.lifecycleOwner: LifecycleOwner? get() {
    val activity = activity as? FragmentActivity? ?: return null
    val fragment = findSupportFragment(this, activity)
    return fragment?.viewLifecycleOwner ?: activity
}
private val tempViewToSupportFragment = ArrayMap<View, Fragment>()
private fun findSupportFragment(target: View, activity: FragmentActivity): Fragment? {
    tempViewToSupportFragment.clear()
    findAllSupportFragmentsWithViews(
            activity.supportFragmentManager.fragments, tempViewToSupportFragment
    )
    var result: Fragment? = null
    val activityRoot = activity.findViewById<View>(android.R.id.content)
    var current = target
    while (current != activityRoot) {
        result = tempViewToSupportFragment[current]
        if (result != null) {
            break
        }
        current = if (current.parent is View) {
            current.parent as View
        } else {
            break
        }
    }
    tempViewToSupportFragment.clear()
    return result
}

private fun findAllSupportFragmentsWithViews(
        topLevelFragments: Collection<Fragment?>?, result: MutableMap<View?, Fragment>
) {
    if (topLevelFragments == null) {
        return
    }
    for (fragment in topLevelFragments) {
        // getFragment()s in the support FragmentManager may contain null values, see #1991.
        if (fragment?.view == null) {
            continue
        }
        result[fragment.view] = fragment
        findAllSupportFragmentsWithViews(fragment.childFragmentManager.fragments, result)
    }
}

fun ViewGroup.inflate(resId: Int): View {
    return LayoutInflater.from(context).inflate(resId, this, false)
}

