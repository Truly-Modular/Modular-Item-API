package smartin.miapi.craft;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.material.AllowedMaterial;

import java.util.HashMap;
import java.util.Map;

public class BlueprintManager {
    public static Map<ResourceLocation, BlueprintComponent> reloadedBlueprints = new HashMap<>();
    public static ResourceLocation ID = Miapi.id("reloaded_blueprint");

    public static void setup() {
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/blueprint", reloadedBlueprints, (isClient, id, data, registryAccess) -> {
            Miapi.LOGGER.info("loaded Blueprint " + id);
            JsonElement element = Miapi.gson.fromJson(data, JsonElement.class);
            BlueprintComponent component = BlueprintComponent.CODEC.decode(JsonOps.INSTANCE, element).getOrThrow().getFirst();
            if (component.ingredient.left().isPresent() && component.ingredient.left().get()) {
                Miapi.LOGGER.warn("Datapack Blueprints cannot set the Ingredient to True!, either use false ur a Ingredient with count");
            } else {
                reloadedBlueprints.put(id, BlueprintComponent.CODEC.decode(JsonOps.INSTANCE, element).getOrThrow().getFirst());
            }
        });
        ReloadEvents.END.subscribe((isClient, registryAccess) -> Miapi.LOGGER.info("Loaded " + reloadedBlueprints.size() + " Blueprints"));
    }

    public static CraftOption asCraftOption(CraftingScreenHandler screenHandler, ResourceLocation location, BlueprintComponent blueprint) {
        return new CraftOption(
                blueprint.toMerge.module,
                () -> {
                    var decodeResult = ResourceLocation.CODEC.encodeStart(JsonOps.INSTANCE, location).getOrThrow();
                    JsonElement booleanElement = Codec.BOOL.encodeStart(JsonOps.INSTANCE, blueprint.useMaterialCrafting()).getOrThrow();
                    return Map.of(
                            ID, decodeResult,
                            AllowedMaterial.KEY, booleanElement);
                },
                -100,
                blueprint.getName());
    }

    @Nullable
    public static BlueprintComponent getBlueprint(Map<ResourceLocation, JsonElement> dataMap, CraftingScreenHandler screenHandler) {
        JsonElement json = dataMap.get(ID);
        if (json != null) {
            var decodeResult = ResourceLocation.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
            if (reloadedBlueprints.containsKey(decodeResult)) {
                return reloadedBlueprints.get(decodeResult);
            }
        }
        return null;
    }
}
