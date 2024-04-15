package com.ddangddangddang.android.util.view

import android.view.View
import androidx.annotation.StringRes
import com.ddangddangddang.android.R
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(
    @StringRes
    textId: Int,
    @StringRes
    actionId: Int = R.string.all_snackbar_default_action,
    action: () -> Unit = {},
    anchorView: View? = null,
) {
    Snackbar.make(this, context.getString(textId), Snackbar.LENGTH_SHORT)
        .setAction(actionId) {
            action()
        }
        .apply { anchorView?.let { this.anchorView = anchorView } }
        .show()
}

fun View.showSnackbar(
    message: String,
    actionMessage: String,
    action: () -> Unit = {},
    anchorView: View? = null,
) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .setAction(actionMessage) {
            action()
        }
        .apply { anchorView?.let { this.anchorView = anchorView } }
        .show()
}
