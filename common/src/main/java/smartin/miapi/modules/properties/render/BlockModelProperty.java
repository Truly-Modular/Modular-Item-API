package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.BlockRenderModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.material.MaterialIcons;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class BlockModelProperty implements ModuleProperty {
    public static String KEY = "block_model";
    public static BlockModelProperty property;

    public BlockModelProperty() {
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            if (model.getProperties().containsKey(property)) {
                JsonElement element = model.getProperties().get(property);
                if (element != null && element.isJsonArray()) {
                    element.getAsJsonArray().forEach(jsonElement -> {
                        JsonObject object = jsonElement.getAsJsonObject();
                        Identifier identifier = new Identifier(object.get("id").getAsString());
                        Block block = Registries.BLOCK.get(identifier);
                        Transform transform = Miapi.gson.fromJson(object.get("transform"), Transform.class);
                        BlockState blockState = block.getDefaultState();
                        if (object.has("nbt")) {
                            blockState = BlockState.CODEC.parse(JsonOps.INSTANCE, object.get("nbt")).result().orElse(blockState);
                        }
                        BlockRenderModel blockRenderModel = new BlockRenderModel(blockState, transform);
                        if(object.has("spin")){
                            blockRenderModel.spinSettings = MaterialIcons.SpinSettings.codec.parse(JsonOps.INSTANCE,object.get("spin")).result().get();
                        }
                        models.add(blockRenderModel);
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
