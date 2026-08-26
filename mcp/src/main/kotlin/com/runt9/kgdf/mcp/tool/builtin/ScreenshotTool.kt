package com.runt9.kgdf.mcp.tool.builtin

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.runt9.kgdf.mcp.tool.HarnessTool
import com.runt9.kgdf.mcp.tool.RENDER_TIMEOUT
import com.runt9.kgdf.mcp.tool.output.ToolOutput
import com.runt9.kgdf.mcp.tool.output.Png
import kotlinx.coroutines.withTimeout
import ktx.async.onRenderingThread
import java.io.ByteArrayOutputStream

object ScreenshotTool : HarnessTool {
    override val name = "screenshot"

    override val description =
        "Capture the current frame as a PNG. Prefer observe, which is cheaper and already carries every value " +
            "you can act on; take a screenshot when you need to see layout or art rather than numbers."

    /**
     * **Returns the previous frame, not the current one.** Posted runnables drain before the render, so anything
     * that changed this frame is not in the image yet. Read [ObserveTool] rather than the pixels for state.
     */
    override suspend fun call(args: Map<String, String>): ToolOutput = withTimeout(RENDER_TIMEOUT) {
        onRenderingThread {
            val pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
            try {
                Png(pixmap.toPng())
            } finally {
                pixmap.dispose()
            }
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
