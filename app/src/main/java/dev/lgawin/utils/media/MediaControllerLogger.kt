package dev.lgawin.utils.media

import android.app.PendingIntent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture

@OptIn(UnstableApi::class) @Suppress("FunctionName")
fun MediaControllerLogger(tag: String = "MediaControllerLogger") = object : MediaController.Listener {

    private val TAG = tag

    override fun onDisconnected(controller: MediaController) {
        Log.d(TAG, "onDisconnected: ")
        super.onDisconnected(controller)
    }

    override fun onSetCustomLayout(
        controller: MediaController,
        layout: MutableList<CommandButton>,
    ): ListenableFuture<SessionResult> {
        Log.d(TAG, "onSetCustomLayout: ")
        return super.onSetCustomLayout(controller, layout)
    }

    override fun onCustomLayoutChanged(controller: MediaController, layout: MutableList<CommandButton>) {
        Log.d(TAG, "onCustomLayoutChanged: ")
        super.onCustomLayoutChanged(controller, layout)
    }

    override fun onAvailableSessionCommandsChanged(controller: MediaController, commands: SessionCommands) {
        Log.d(TAG, "onAvailableSessionCommandsChanged: ")
        super.onAvailableSessionCommandsChanged(controller, commands)
    }

    override fun onCustomCommand(
        controller: MediaController,
        command: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> {
        Log.d(TAG, "onCustomCommand: ")
        return super.onCustomCommand(controller, command, args)
    }

    override fun onExtrasChanged(controller: MediaController, extras: Bundle) {
        Log.d(TAG, "onExtrasChanged: ")
        super.onExtrasChanged(controller, extras)
    }

    override fun onSessionActivityChanged(controller: MediaController, sessionActivity: PendingIntent) {
        Log.d(TAG, "onSessionActivityChanged: ")
        super.onSessionActivityChanged(controller, sessionActivity)
    }
}
