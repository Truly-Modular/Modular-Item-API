package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
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
    public static World fakeWorld;

    public EntityModelProperty() {
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            if (model.getProperties().containsKey(property)) {
                JsonElement element = model.getProperties().get(property);
                if (element != null && element.isJsonArray()) {
                    element.getAsJsonArray().forEach(jsonElement -> {
                        JsonObject object = jsonElement.getAsJsonObject();
                        Identifier identifier = new Identifier(object.get("id").getAsString());
                        EntityType entityType = Registries.ENTITY_TYPE.get(identifier);
                        Transform transform = Miapi.gson.fromJson(object.get("transform"), Transform.class);
                        if (entityType != null) {
                            Entity entity = entityType.create(MinecraftClient.getInstance().world);
                            if (entity != null) {
                                if (object.has("nbt")) {
                                    NbtCompound compound = NbtCompound.CODEC.parse(JsonOps.INSTANCE, object.get("nbt")).result().orElse(new NbtCompound());
                                    entity.readNbt(compound);
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
