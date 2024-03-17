package smartin.miapi.forge.compat.epic_fight;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class EpicFightCompatProperty implements ModuleProperty {
    public static EpicFightCompatProperty property;
    public static String KEY = "epic_fight";

    public EpicFightCompatProperty(){
        property = this;
        ModularItemCache.setSupplier(KEY, EpicFightCompatProperty::createCache);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) {
        return loadJsonData(data) != null;
    }

    public static CapabilityItem loadJsonData(JsonElement element) {
        try {
            NbtCompound compound = NbtCompound.CODEC.parse(JsonOps.INSTANCE, element).result().orElse(null);
            if (compound == null) return null;
            return ItemCapabilityReloadListener.deserializeWeapon(Items.AIR, compound);
        } catch (Exception e) {
            Miapi.LOGGER.error("Failed to load epic fight data!", e);
            return null;
        }
    }

    public static CapabilityItem get(ItemStack itemStack) {
        return ModularItemCache.getRaw(itemStack, KEY);
    }

    public static CapabilityItem createCache(ItemStack stack) {
        JsonElement data = ItemModule.getMergedProperty(stack, property);
        if (data == null) return null;
        return loadJsonData(data);
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (type == MergeType.SMART || type == MergeType.EXTEND) {
            return toMerge;
        } else if (type == MergeType.OVERWRITE) {
            return old;
        }
        return ModuleProperty.super.merge(old, toMerge, type);
    }
}
