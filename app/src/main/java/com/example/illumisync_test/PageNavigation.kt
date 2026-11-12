
package com.example.illumisync_test

import android.app.Activity
import android.content.Intent

object PageNavigation {

    fun open(activity: Activity, destination: Class<*>) {
        if (activity.javaClass == destination) return

        val intent = Intent(activity, destination)
        activity.startActivity(intent)

        activity.overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )

        activity.finish()
    }
}
