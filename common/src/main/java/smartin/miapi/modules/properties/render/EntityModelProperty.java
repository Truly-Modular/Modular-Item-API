package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.EntityMiapiModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.material.MaterialIcons;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class EntityModelProperty implements RenderProperty {
    public static String KEY = "entity_model";
    public static EntityModelProperty property;

    public EntityModelProperty() {
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            if (model.getOldProperties().containsKey(property)) {
                JsonElement element = model.getOldProperties().get(property);
                if (element != null && element.isJsonArray()) {
                    element.getAsJsonArray().forEach(jsonElement -> {
                        JsonObject object = jsonElement.getAsJsonObject();
                        ResourceLocation identifier = ResourceLocation.parse(object.get("id").getAsString());
                        EntityType entityType = BuiltInRegistries.ENTITY_TYPE.get(identifier);
                        Transform transform = Miapi.gson.fromJson(object.get("transform"), Transform.class);
                        if (entityType != null) {
                            Entity entity = entityType.create(Minecraft.getInstance().level);
                            if (entity != null) {
                                if (object.has("nbt")) {
                                    CompoundTag compound = CompoundTag.CODEC.parse(JsonOps.INSTANCE, object.get("nbt")).result().orElse(new CompoundTag());
                                    entity.load(compound);
                                }
                                EntityMiapiModel entityMiapiModel = new EntityMiapiModel(entity, transform);
                                entityMiapiModel.doTick = ModuleProperty.getBoolean(object, "tick", model, true);
                                entityMiapiModel.fullBright = ModuleProperty.getBoolean(object, "full_bright", model, true);
                                if(object.has("spin")){
                                    entityMiapiModel.spinSettings = MaterialIcons.SpinSettings.codec.parse(JsonOps.INSTANCE,object.get("spin")).result().get();
                                }
                                models.add(entityMiapiModel);
                            }
                        }
                    });
                }
            }
            return models;
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }
}
