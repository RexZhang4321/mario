package util;

import components.SpriteSheet;
import renderer.Shader;
import renderer.Texture;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AssetPool {

    private static Map<String, Shader> shaders = new HashMap<>();
    private static Map<String, Texture> textures = new HashMap<>();
    private static Map<String, SpriteSheet> spriteSheets = new HashMap<>();

    public static Shader getShader(String resourceName) {
        File file = new File(resourceName);
        String path = file.getAbsolutePath();
        if (shaders.containsKey(path)) {
            return shaders.get(path);
        } else {
            Shader shader = new Shader(resourceName);
            shader.compile();
            AssetPool.shaders.put(path, shader);
            return shader;
        }
    }

    public static Texture getTexture(String resourceName) {
        File file = new File(resourceName);
        String path = file.getAbsolutePath();
        if (textures.containsKey(path)) {
            return textures.get(path);
        } else {
            Texture texture = new Texture(path);
            textures.put(path, texture);
            return texture;
        }
    }

    public static void addSpriteSheet(String resourceName, SpriteSheet spriteSheet) {
        File file = new File(resourceName);
        String path = file.getAbsolutePath();
        if (!AssetPool.spriteSheets.containsKey(path)) {
            AssetPool.spriteSheets.put(path, spriteSheet);
        }
    }

    public static SpriteSheet getSpriteSheet(String resourceName) {
        File file = new File(resourceName);
        String path = file.getAbsolutePath();
        assert AssetPool.spriteSheets.containsKey(path) : "Error: Tried to access SpriteSheet '" + resourceName + "' and it has not been added yet.";
        return AssetPool.spriteSheets.getOrDefault(path, null);
    }
}
