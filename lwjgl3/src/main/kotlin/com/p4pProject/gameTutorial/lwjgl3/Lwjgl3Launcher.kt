package com.p4pProject.gameTutorial.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.p4pProject.gameTutorial.MyGameTutorial
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

/** Launches the desktop (LWJGL3) application.  */

    fun main() {
        Lwjgl3Application(MyGameTutorial(), Lwjgl3ApplicationConfiguration().apply{
            setTitle("gameTutorial")
            setWindowedMode(16 * 96, 9*96 )
            setWindowIcon(
                "libgdx128.png",
                "libgdx64.png",
                "libgdx32.png",
                "libgdx16.png"
            )
        })
    }