package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import smartin.miapi.client.model.CrystalModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class CrystalModelProperty implements ModuleProperty, RenderProperty {
    public static String KEY = "crystal_model";
    public static CrystalModelProperty property;

    public CrystalModelProperty() {
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            if (model.getOldProperties().containsKey(property)) {
                models.add(new CrystalModel());
            }
            return models;
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }
}
