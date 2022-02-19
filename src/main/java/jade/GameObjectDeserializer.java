package jade;

import com.google.gson.*;

import java.lang.reflect.Type;

public class GameObjectDeserializer implements JsonDeserializer<GameObject> {
    @Override
    public GameObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        JsonArray components = jsonObject.getAsJsonArray("components");
        Transform transform = context.deserialize(jsonObject.get("transform"), Transform.class);
        int zIndex = context.deserialize(jsonObject.get("zIndex"), int.class);

        GameObject gameObject = new GameObject(name, transform, zIndex);
        for (JsonElement jsonElement : components) {
            Component component = context.deserialize(jsonElement, Component.class);
            gameObject.addComponent(component);
        }
        return gameObject;
    }
}
