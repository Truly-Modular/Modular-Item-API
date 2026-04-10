package smartin.miapi.blueprint;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.datapack.ReloadHandlerBuilder;
import smartin.miapi.material.properties.AllowedMaterial;
import smartin.miapi.registries.JsonOpsBooleanPatched;
import smartin.miapi.registries.MiapiRegistry;

import java.util.Map;

public class BlueprintManager {
    public static MiapiRegistry<BlueprintComponent> RELOADED_BLUEPRINTS = MiapiRegistry.getInstance(BlueprintComponent.class);
    public static ResourceLocation ID = Miapi.id("reloaded_blueprint");

    public static void setup() {
        ReloadHandlerBuilder.builder("miapi/blueprint")
                .clear(RELOADED_BLUEPRINTS::clear)
                .priority(5)
                .codec(BlueprintComponent.CODEC, (isClient, path, blueprint, registryAccess) -> {
                    if (blueprint.ingredient.left().isPresent() && blueprint.ingredient.left().get()) {
                        Miapi.LOGGER.warn("Datapack Blueprints cannot set the Ingredient to True!, either use false ur a Ingredient with count");
                    } else {
                        RELOADED_BLUEPRINTS.register(path, blueprint);
                    }
                })
                .afterLoop(((isClient, registryAccess, worker) -> {
                    Miapi.LOGGER.info("Loaded " + RELOADED_BLUEPRINTS.getFlatMap().size() + " Blueprints");
                }));
    }

    public static CraftOption asCraftOption(CraftingScreenHandler screenHandler, ResourceLocation location, BlueprintComponent blueprint) {
        return new CraftOption(
                blueprint.toMerge.getModule(),
                () -> {
                    var decodeResult = ResourceLocation.CODEC.encodeStart(JsonOpsBooleanPatched.INSTANCE, location).getOrThrow();
                    JsonElement booleanElement = Miapi.FIXED_BOOL_CODEC.encodeStart(JsonOpsBooleanPatched.INSTANCE, blueprint.useMaterialCrafting()).getOrThrow();
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
            var decodeResult = ResourceLocation.CODEC.decode(JsonOpsBooleanPatched.INSTANCE, json).getOrThrow().getFirst();
            if (RELOADED_BLUEPRINTS.containsKey(decodeResult)) {
                return RELOADED_BLUEPRINTS.get(decodeResult);
            }
        }
        return null;
    }
}
