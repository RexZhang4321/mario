package renderer;

import org.lwjgl.opengl.GL41;

public class Framebuffer {

    private int fboId = 0;
    private Texture texture = null;

    public Framebuffer(int width, int height) {
        // generate framebuffer
        fboId = GL41.glGenFramebuffers();
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, fboId);

        // create the texture to render the data to, and attach it to our framebuffer
        this.texture = new Texture(width, height);
        GL41.glFramebufferTexture2D(GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT0, GL41.GL_TEXTURE_2D, this.texture.getTexId(), 0);

        // create render buffer to store the depth info
        int rboId = GL41.glGenRenderbuffers();
        GL41.glBindRenderbuffer(GL41.GL_RENDERBUFFER, rboId);
        GL41.glRenderbufferStorage(GL41.GL_RENDERBUFFER, GL41.GL_DEPTH_COMPONENT32, width, height);
        GL41.glFramebufferRenderbuffer(GL41.GL_FRAMEBUFFER, GL41.GL_DEPTH_ATTACHMENT, GL41.GL_RENDERBUFFER, rboId);

        assert GL41.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER) == GL41.GL_FRAMEBUFFER_COMPLETE : "Error: Framebuffer is not complete";

        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }

    public void bind() {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, fboId);
    }

    public void unbind() {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }

    public int getFboId() {
        return fboId;
    }

    public int getTextureId() {
        return texture.getTexId();
    }
}
