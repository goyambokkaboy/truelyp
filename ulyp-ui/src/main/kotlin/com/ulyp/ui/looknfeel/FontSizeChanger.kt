package com.ulyp.ui.looknfeel

import javafx.scene.Scene
import javafx.scene.text.Font
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

@Component
class FontSizeChanger {
    private var currentFontSize = 1.0
    private val fontChooser = FontChooser()

    fun upscale(scene: Scene) {
        currentFontSize += 0.05
        refreshFont(scene, currentFontSize)
    }

    fun downscale(scene: Scene) {
        currentFontSize -= 0.05
        refreshFont(scene, currentFontSize)
    }

    private fun refreshFont(scene: Scene, font: Double) {
        try {
            val path = Files.createTempFile(STYLE_PREFIX, null)
            path.toFile().deleteOnExit()
            Files.write(
                path,
                """.ulyp-ctt {
                -fx-font-family: ${fontChooser.getFontName()};
                -fx-font-size: ${font}em;
                }""".toByteArray(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE
            )
            var index: Int? = null
            for (i in scene.stylesheets.indices) {
                if (scene.stylesheets[i].contains(STYLE_PREFIX)) {
                    index = i
                    break
                }
            }
            if (index != null) {
                scene.stylesheets[index] = path.toFile().toURI().toString()
            } else {
                scene.stylesheets.add(path.toFile().toURI().toString())
            }
        } catch (e: IOException) {
            // TODO show error
            e.printStackTrace()
        }
    }

    companion object {
        private const val STYLE_PREFIX = "ulyp-ctt-font-style"
    }
}