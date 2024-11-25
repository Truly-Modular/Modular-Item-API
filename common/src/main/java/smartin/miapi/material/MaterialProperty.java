package smartin.miapi.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.mixin.NamedAccessor;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the Property relating to materials of a Module
 */
public class MaterialProperty extends CodecProperty<ResourceLocation> {
    public static final ResourceLocation KEY = Miapi.id("material");
    public static ModuleProperty property;
    public static Map<ResourceLocation, Material> materials = new ConcurrentHashMap<>();

    public MaterialProperty() {
        super(ResourceLocation.CODEC);
        property = this;
        StatResolver.registerResolver("material", new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ModuleInstance instance) {
                try {
                    Material material = getMaterial(instance);
                    if (material != null) {
                        return material.getDouble(data);
                    }
                } catch (Exception exception) {
                    Miapi.LOGGER.warn("Error during Material Resolve", exception);
                }
                return 0;
            }

            @Override
            public String resolveString(String data, ModuleInstance instance) {
                try {
                    Material material = getMaterial(instance);
                    if (material != null) {
                        return material.getData(data);
                    }
                } catch (Exception exception) {
                    Miapi.LOGGER.warn("Error during Material Resolve", exception);
                }
                return "";
            }
        });
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/materials", materials, (isClient, path, data) -> {
            JsonParser parser = new JsonParser();
            String id = path.toString();
            id = id.replace("miapi/materials/", "");
            id = id.replace(".json", "");
            ResourceLocation revisedID = ResourceLocation.parse(id);
            JsonObject obj = parser.parse(data).getAsJsonObject();
            //if (materials.containsKey(material.getID())) {
            //    Miapi.LOGGER.warn("Overwriting Materials isn't 100% safe. The ordering might be wrong, please set the overwrite material in the same path as the origin Material" + path + " is overwriting " + material.getID());
            //}
            try {
                CodecMaterial codecMaterial = CodecMaterial.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, Miapi.registryAccess), obj).getOrThrow().getFirst();
                codecMaterial.setID(revisedID);
                materials.put(revisedID, codecMaterial);
            } catch (RuntimeException e) {
                JsonMaterial material = new JsonMaterial(revisedID, obj, isClient);
                Miapi.LOGGER.error("FAILED MATERIAL DECODE " + material.getDebugJson(), e);
            }
        }, -2f);


        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/material_extensions", (isClient) -> {

        }, (isClient, path, data) -> {
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(data).getAsJsonObject();
            String idString = obj.get("key").getAsString();
            Material material = materials.get(Miapi.id(idString));
            if (material != null) {
                if (material instanceof JsonMaterial jsonMaterial) {
                    jsonMaterial.mergeJson(obj, isClient);
                }
                if (material instanceof CodecMaterial codecMaterial) {
                    CodecMaterial toMerge = CodecMaterial.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, Miapi.registryAccess), obj).getOrThrow().getFirst();
                    codecMaterial.merge(toMerge);
                }
            } else {
                Miapi.LOGGER.error("Miapi could not find Material for Material extension " + idString + " " + path);
            }
        }, -1.5f);
        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            if (isClient) {
                Minecraft.getInstance().execute(() -> {
                    RenderSystem.assertOnRenderThread();
                    //MiapiClient.materialAtlasManager.apply(null, Minecraft.getInstance().getProfiler());
                });

            }
        }, 1);
        ReloadEvents.END.subscribe(((isClient, registryAccess) -> {
            HolderSet.Named<Item> named = BuiltInRegistries.ITEM.getOrCreateTag(RegistryInventory.MIAPI_MATERIALS);
            if (named instanceof NamedAccessor namedAccessor) {
                materials.forEach((id, material) -> {
                });
                namedAccessor.callBind(List.of());
            }
            Miapi.LOGGER.info("Loaded " + materials.size() + " Materials");
        }));
    }

    @Override
    public ResourceLocation merge(ResourceLocation left, ResourceLocation right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }

    public static List<String> getTextureKeys() {
        Set<String> textureKeys = new HashSet<>();
        textureKeys.add("base");
        for (Material material : materials.values()) {
            textureKeys.add(material.getStringID());
            textureKeys.addAll(material.getTextureKeys());
        }
        return new ArrayList<>(textureKeys);
    }

    /**
     * Resolves a Material form an Itemstack. if no Material is set for the Itemstack, returns null
     *
     * @param item
     * @return
     */
    @Nullable
    public static Material getMaterialFromIngredient(ItemStack item) {
        double lowestPrio = Double.MAX_VALUE;
        Material foundMaterial = null;
        for (Material material : materials.values()) {
            Double matPrio = material.getPriorityOfIngredientItem(item);
            if (matPrio != null && matPrio < lowestPrio) {
                lowestPrio = matPrio;
                foundMaterial = material;
            }
        }
        if (foundMaterial != null) {
            return foundMaterial.getMaterialFromIngredient(item);
        } else {
            return null;
        }
    }

    /**
     * This call should only used if no valid moduleinstance is known and only the Key of the material is important
     *
     * @param element
     * @return
     */
    @Nullable
    public static Material getMaterial(JsonElement element) {
        if (element != null) {
            return materials.get(element.getAsString());
        }
        return null;
    }

    /**
     * Gets the used Material of a ModuleInstance
     *
     * @param instance
     * @return
     */
    @Nullable
    public static Material getMaterial(ModuleInstance instance) {
        if (property.getData(instance).isPresent()) {
            Material material = MaterialProperty.materials.get(property.getData(instance).get());
            if (material != null) {
                material = material.getMaterial(instance);
                return MaterialOverwriteProperty.property.adjustMaterial(instance, material);
            }
        }
        if (CopyParentMaterialProperty.property.isTrue(instance) && instance.getParent() != null) {
            return MaterialOverwriteProperty.property.adjustMaterial(instance, getMaterial(instance.getParent()));
        }
        return null;
    }

    /**
     * Gets the used Material of some Properties
     *
     * @return
     */
    @Nullable
    public static Material getMaterial(Map<ModuleProperty<?>, Object> properties) {
        ResourceLocation id = (ResourceLocation) properties.get(property);
        if (id != null) {
            return MaterialProperty.materials.get(id);
        }
        return null;
    }

    /**
     * Sets a material of a MOduleinstance via Stringkey
     *
     * @param instance
     * @param material
     */
    public static void setMaterial(ModuleInstance instance, Material material) {
        ModuleDataPropertiesManager.setProperty(instance, MaterialProperty.property, material.getID());
        material.setMaterial(instance);
    }
}
