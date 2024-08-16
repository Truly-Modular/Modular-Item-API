package smartin.miapi.modules.properties.render;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.CrystalModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

import java.util.ArrayList;
import java.util.List;

public class CrystalModelProperty extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("crystal_model");
    public static CrystalModelProperty property;

    public CrystalModelProperty() {
        super(KEY, false);
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            if (isTrue(model)) {
                models.add(new CrystalModel());
            }
            return models;
        });
    }
}
