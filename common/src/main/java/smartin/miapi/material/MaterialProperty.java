package smartin.miapi.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import io.netty.handler.codec.DecoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.base.Material;
import smartin.miapi.mixin.NamedAccessor;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.JsonOpsBooleanPatched;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

/**
 * This is the Property relating to materials of a Module
 */
public class MaterialProperty extends CodecProperty<ResourceLocation> {
    public static final ResourceLocation KEY = Miapi.id("material");
    public static ModuleProperty property;
    public static MiapiRegistry<Material> MATERIAL_REGISTRY = MiapiRegistry.getInstance(Material.class);
    public static Codec<Material> MATERIAL_CODEC = MaterialCodecs.MATERIAL_CODEC;

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
        ReloadEvents.END.subscribe((isClient, registryAccess, worker) -> {
            if (isClient) {
                Minecraft.getInstance().execute(() -> {
                    RenderSystem.assertOnRenderThread();
                    //MiapiClient.materialAtlasManager.apply(null, Minecraft.getInstance().getProfiler());
                });

            }
        }, 1);
        ReloadEvents.END.subscribe(((isClient, registryAccess, worker) -> {
            HolderSet.Named<Item> named = BuiltInRegistries.ITEM.getOrCreateTag(RegistryInventory.MIAPI_MATERIALS);
            if (named instanceof NamedAccessor namedAccessor) {
                MATERIAL_REGISTRY.getFlatMap().forEach((id, material) -> {
                });
                namedAccessor.callBind(List.of());
            }
            Miapi.LOGGER.info("Loaded " + MATERIAL_REGISTRY.getFlatMap().size() + " Materials");
        }));
        ModularItemCache.MODULE_CACHE_SUPPLIER.put(KEY.toString(), MaterialProperty::getMaterialRaw);
    }

    public static void loadMaterialExtention(ResourceLocation path, String data, RegistryAccess registryAccess) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(data).getAsJsonObject();
            String idString = obj.get("key").getAsString();
            Material material = MATERIAL_REGISTRY.get(Miapi.id(idString));
            if (material != null && material instanceof CodecMaterial codecMaterial) {
                CodecMaterial toMerge = CodecMaterial.CODEC.decode(RegistryOps.create(JsonOpsBooleanPatched.INSTANCE, registryAccess), obj).getOrThrow(s -> new DecoderException("Could not decode Material Extention " + s)).getFirst();
                codecMaterial.merge(toMerge);
            } else {
                Miapi.LOGGER.error("Miapi could not find Material for Material extension " + idString + " " + path);
            }
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("Miapi could not find Material for Material extension " + path, e);
        }
    }

    @Override
    public ResourceLocation merge(ResourceLocation left, ResourceLocation right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    public static List<String> getTextureKeys() {
        Set<String> textureKeys = new HashSet<>();
        textureKeys.add("base");
        for (Material material : MATERIAL_REGISTRY.getFlatMap().values()) {
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

        for (Material material : MATERIAL_REGISTRY.getFlatMap().values().stream().toList()) {
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

    @Nullable
    /**
     * Gets the used Material of a ModuleInstance
     *
     * @param instance
     * @return
     */
    public static Material getMaterial(ModuleInstance instance) {
        return instance.getFromCache(KEY.toString(), () -> null);
    }

    @Nullable
    private static Material getMaterialRaw(ModuleInstance instance) {
        if (instance.moduleData.containsKey(KEY)) {
            JsonElement element = instance.moduleData.get(KEY);
            try {
                Material jsonMaterial = MaterialProperty.MATERIAL_CODEC.decode(JsonOpsBooleanPatched.INSTANCE, element).getOrThrow().getFirst();
                if (jsonMaterial != null) {
                    return MaterialOverwriteProperty.property.adjustMaterial(instance, jsonMaterial.getMaterial(instance, instance.initializedProperties));
                }
                return jsonMaterial;
            } catch (RuntimeException ignored) {

            }
        }
        if (property.getData(instance).isPresent()) {
            Material material = MaterialProperty.MATERIAL_REGISTRY.get((ResourceLocation) property.getData(instance).get());
            if (material != null) {
                material = material.getMaterial(instance, instance.initializedProperties);
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
            return MaterialProperty.MATERIAL_REGISTRY.get(id);
        }
        return null;
    }

    /**
     * Sets a material of a Moduleinstance
     *
     * @param instance
     * @param material
     */
    public static void setMaterial(ModuleInstance instance, Material material) {
        instance.moduleData.put(KEY, MaterialProperty.MATERIAL_CODEC.encodeStart(JsonOpsBooleanPatched.INSTANCE, material).getOrThrow());
        ModuleDataPropertiesManager.setProperty(instance, property, null);
        material.setMaterial(instance);
    }
}
