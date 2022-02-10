package renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Texture {

    private String filePath;
    private int texId;

    public Texture(String filePath) {
        this.filePath = filePath;

        // generate texture on GPU
        texId = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, texId);

        // set texture parameters
        // repeat image in both directions (if the height or width do not match
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_S, GL41.GL_REPEAT);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_T, GL41.GL_REPEAT);

        // when stretching the image, piexlate
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);

        // when shrinking the image, piexlate
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        ByteBuffer image = STBImage.stbi_load(filePath, width, height, channels, 0);

        if (image != null) {
            int nChannels = GL41.GL_RGB;
            if (channels.get(0) == 3) {
                nChannels = GL41.GL_RGB;
            } else if (channels.get(0) == 4) {
                nChannels = GL41.GL_RGBA;
            } else {
                assert false : "Error: (Texture) Unknown number of channels: '" + channels.get(0) + "'";
            }
            GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, nChannels, width.get(0), height.get(0),
                    0, nChannels, GL41.GL_UNSIGNED_BYTE, image);
        } else {
            assert false : "Error: (Texture) Could not load image '" + filePath + "'";
        }

        STBImage.stbi_image_free(image);
    }

    public void bind() {
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, texId);
    }

    private void unbind() {
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
    }

}
