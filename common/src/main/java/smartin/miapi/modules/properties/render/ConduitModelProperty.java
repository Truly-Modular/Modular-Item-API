package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.ConduitRendererEntity;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;

import java.util.ArrayList;
import java.util.List;

public class ConduitModelProperty implements RenderProperty {
    public static String KEY = "conduit_model";
    public static ConduitModelProperty property;

    public ConduitModelProperty() {
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            JsonElement element = model.getProperties().get(property);
            if (element != null && element.isJsonArray()) {
                element.getAsJsonArray().forEach(jsonElement -> {
                    Transform transform = Miapi.gson.fromJson(jsonElement.getAsJsonObject().get("transform"), Transform.class);
                    models.add(new ConduitRendererEntity(transform));
                });
            }
            return models;
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }
}
