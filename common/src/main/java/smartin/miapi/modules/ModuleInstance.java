package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ModuleInstance(
        ResourceLocation moduleId,
        Map<ResourceLocation, JsonElement> data,
        Map<String, ModuleInstance> children,
        RegistryOps.RegistryInfoLookup getter,
        ModuleInstanceLocalCache cache
) {
    public static Codec<ModuleInstance> CODEC = new ModuleInstanceCodec();
    public static DataComponentType<ModuleInstance> MODULE_INSTANCE_COMPONENT = DataComponentType.<ModuleInstance>builder().persistent(CODEC).networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();
    public static DataComponentType<JsonElement> MODULE_BACKUP = DataComponentType.<JsonElement>builder()
            .persistent(StatResolver.Codecs.JSONELEMENT_CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(StatResolver.Codecs.JSONELEMENT_CODEC)).build();

    public ModuleInstance(ResourceLocation moduleId,
                          RegistryAccess access) {
        this(moduleId, Map.of(), Map.of(), new MiapiHolderLookupAdapter(access));
    }

    public ModuleInstance(ResourceLocation moduleId,
                          Map<ResourceLocation, JsonElement> data,
                          Map<String, ModuleInstance> children,
                          RegistryAccess access) {
        this(moduleId, data, children, new MiapiHolderLookupAdapter(access));
    }


    public ModuleInstance(ResourceLocation moduleId,
                          Map<ResourceLocation, JsonElement> data,
                          Map<String, ModuleInstance> children,
                          RegistryOps.RegistryInfoLookup getter) {
        this(moduleId, data, children, getter, new ModuleInstanceLocalCache());

        cache.record = this;

        // link parents
        children.values().forEach(child -> child.cache().parent = this);
    }

    public ItemModule getModule() {
        return cache().getModule();
    }

    public ModuleInstance getRoot(){
        return cache().getRoot();
    }

    /**
     * Recursively calculates the position of this module instance in its hierarchy.
     *
     * @param position The list to store the position.
     */
    public void calculatePosition(List<String> position) {
        ModuleInstance parent = getParent();
        if (parent != null) {
            for (Map.Entry<String, ModuleInstance> entry : parent.children.entrySet()) {
                if (entry.getValue() == this) {
                    parent.calculatePosition(position);
                    position.add(entry.getKey());
                    return;
                }
            }
        }
    }

    /**
     * Retrieves the module instance at the specified position in the hierarchy.
     *
     * @param position The position of the module instance.
     * @return The module instance at the specified position.
     */
    public ModuleInstance getPosition(List<String> position) {
        if (position == null || position.isEmpty()) {
            return this;
        }
        ModuleInstance current = this;
        for (String key : position) {
            current = current.children.get(key);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public List<ModuleInstance> getFlatList() {
        return cache().allSubModules();
    }

    public <T> T getProperty(ModuleProperty<T> property) {
        return cache().getProperty(property);
    }

    public void writeToItem(ItemStack itemStack) {
        itemStack.set(MODULE_INSTANCE_COMPONENT, this.getRoot());
        this.cache().clear();
    }

    public MutableModuleInstance asMutable(){
        return MutableModuleInstance.fromRecord(this);
    }

    public ModuleInstance copy() {
        Map<String, ModuleInstance> copiedChildren = new HashMap<>();

        for (var entry : children.entrySet()) {
            copiedChildren.put(entry.getKey(), entry.getValue().copy());
        }

        return new ModuleInstance(
                this.moduleId,
                new HashMap<>(this.data),
                copiedChildren,
                this.getter
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ModuleInstance other)) return false;

        if (!this.moduleId.equals(other.moduleId)) return false;
        if (!this.data.equals(other.data)) return false;

        if (this.children.size() != other.children.size()) return false;

        for (var entry : this.children.entrySet()) {
            ModuleInstance otherChild = other.children.get(entry.getKey());
            if (otherChild == null) return false;
            if (!entry.getValue().equals(otherChild)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = moduleId.hashCode();
        result = 31 * result + data.hashCode();
        result = 31 * result + children.hashCode();
        return result;
    }

    public ModuleInstance getParent() {
        return cache().getParent().orElse(null);
    }
}