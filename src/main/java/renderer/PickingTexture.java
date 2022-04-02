package renderer;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL41;

public class PickingTexture {

    private int pickingTextureId;
    private int fboId;
    private int depthTexture;

    public PickingTexture(int width, int height) {
        assert init(width, height) : "Error initializing PickingTexture.";
    }

    private boolean init(int width, int height) {
        // generate framebuffer
        fboId = GL41.glGenFramebuffers();
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, fboId);

        pickingTextureId = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, pickingTextureId);
        // wrap x direction
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_S, GL41.GL_REPEAT);
        // y direction
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_T, GL41.GL_REPEAT);
        // pixelate
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGB32F, width, height, 0, GL41.GL_RGB, GL41.GL_FLOAT, 0);
        GL41.glFramebufferTexture2D(GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT0, GL41.GL_TEXTURE_2D, pickingTextureId, 0);

        // create texture object for the depth buffer
        GL41.glEnable(GL41.GL_TEXTURE_2D);
        depthTexture = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, depthTexture);
        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_DEPTH_COMPONENT, width, height, 0, GL41.GL_DEPTH_COMPONENT, GL41.GL_FLOAT, 0);
        GL41.glFramebufferTexture2D(GL41.GL_FRAMEBUFFER, GL41.GL_DEPTH_ATTACHMENT, GL41.GL_TEXTURE_2D, depthTexture, 0);

        // disable the reading
        GL41.glReadBuffer(GL41.GL_NONE);
        GL41.glDrawBuffer(GL41.GL_COLOR_ATTACHMENT0);

        assert GL41.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER) == GL41.GL_FRAMEBUFFER_COMPLETE : "Error: Framebuffer is not complete";

        // unbind the texture and framebuffer
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
        return true;
    }

    public void enableWriting() {
        GL41.glBindFramebuffer(GL41.GL_DRAW_FRAMEBUFFER, fboId);
    }

    public void disableWriting() {
        GL41.glBindFramebuffer(GL41.GL_DRAW_FRAMEBUFFER, 0);
    }

    public int readPixel(int x, int y) {
        GL41.glBindFramebuffer(GL41.GL_READ_FRAMEBUFFER, fboId);
        GL41.glReadBuffer(GL41.GL_COLOR_ATTACHMENT0);

        float pixels[] = new float[3];
        GL41.glReadPixels(x, y, 1, 1, GL41.GL_RGB, GL41.GL_FLOAT, pixels);

        return (int)(pixels[0]) - 1;
    }

    public float[] readPixels(Vector2i start, Vector2i end) {
        GL41.glBindFramebuffer(GL41.GL_READ_FRAMEBUFFER, fboId);
        GL41.glReadBuffer(GL41.GL_COLOR_ATTACHMENT0);

        Vector2i size = new Vector2i(end).sub(start).absolute();
        int numPixels = size.x * size.y;
        float pixels[] = new float[3 * numPixels];
        GL41.glReadPixels(start.x, start.y, size.x, size.y, GL41.GL_RGB, GL41.GL_FLOAT, pixels);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] -= 1;
        }

        return pixels;
    }

}
