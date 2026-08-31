package com.runt9.kgdf.api.action

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.runt9.kgdf.api.observe.renderHop
import java.io.ByteArrayOutputStream

object Screenshot {
    /**
     * **Returns the previous frame, not the current one.** Posted work drains before the render, so anything
     * that changed this frame is not in the image yet. Read state rather than the pixels.
     */
    suspend fun capture(): ByteArray = renderHop {
        val pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
        try {
            pixmap.toPng()
        } finally {
            pixmap.dispose()
        }
    }

    private fun Pixmap.toPng(): ByteArray {
        val png = PixmapIO.PNG(width * height * 4)
        try {
            // glReadPixels hands back rows bottom-up; without this the PNG is upside down.
            png.setFlipY(true)
            val out = ByteArrayOutputStream()
            png.write(out, this)
            return out.toByteArray()
        } finally {
            png.dispose()
        }
    }
}
