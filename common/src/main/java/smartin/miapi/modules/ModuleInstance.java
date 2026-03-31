package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * an immutable Module to be read and stored on an itemstack
 * is a part for a module tree. Each module knows its parent.
 * The cache Object knows its parent, and that should be safe to use.
 * Cache internals are modifiable. Many methods point to the cache to allow for
 * cached fast lookups.
 */
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

    public ModuleInstance(ItemModule module, RegistryAccess access) {
        this(module.id(),access);
    }

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

    /**
     * Links this instance as parent of all provided children.
     * Mutates the cache state of child instances.
     */
    public ModuleInstance(ResourceLocation moduleId,
                          Map<ResourceLocation, JsonElement> data,
                          Map<String, ModuleInstance> children,
                          RegistryOps.RegistryInfoLookup getter) {
        this(moduleId, Map.copyOf(data), Map.copyOf(children), getter, new ModuleInstanceLocalCache());

        cache.record = this;

        // link parents
        children.values().forEach(child -> child.cache().parent = this);
    }

    /**
     * returns the {@link ItemModule}.
     * returns {@link ItemModule#empty} and never null
     */
    @NotNull
    public ItemModule getModule() {
        return cache().getModule();
    }

    /**
     * returns the root of the module tree.
     * if this is the root, returns itself.
     */
    @NotNull
    public ModuleInstance getRoot(){
        return cache().getRoot();
    }

    /**
     * returns the parent of the moduleInstance.
     * this is null if this Module does not have a parent.
     */
    @Nullable
    public ModuleInstance getParent() {
        return cache().getParent().orElse(null);
    }

    /**
     * Recursively calculates the position of this module instance in its hierarchy.
     * Mutates the provided list to append this module's path.
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
     * if that position does not exist, this returns null!
     */
    @Nullable
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

    /**
     * creates a flatlist of all modules within the module tree.
     * this list is ordered by priority.
     * Unmodifiable.
     * @return
     */
    public List<ModuleInstance> getFlatList() {
        return cache().allSubModules();
    }

    /**
     * gets a property from the module
     */
    public <T> T getProperty(ModuleProperty<T> property) {
        return cache().getProperty(property);
    }

    /**
     * writes this module to an item
     * @param itemStack
     */
    public void writeToItem(ItemStack itemStack) {
        itemStack.set(MODULE_INSTANCE_COMPONENT, this.getRoot());
        this.cache().clear();
    }

    /**
     * creates a Mutable copy.
     * this copies the entire tree and returns the mutable at the same position in the tree.
     */
    public MutableModuleInstance asMutable(){
        return MutableModuleInstance.fromRecord(this);
    }

    /**
     * Creates a deep copy of the module tree structure.
     * Data elements and registry access are reused.
     */
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

    /**
     * Equality is based on moduleId, data, and children only.
     * Cache and registry access are ignored.
     */
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
}