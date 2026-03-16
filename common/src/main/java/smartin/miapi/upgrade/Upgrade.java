package smartin.miapi.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadHandlerBuilder;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.PropertyHolder;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.JsonOpsBooleanPatched;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public record Upgrade(
        String moduleTag,
        ModuleCondition condition,
        Map<Integer, PropertyHolder> properties,
        List<ResourceLocation> incompatible,
        int max,
        int cost
) {
    public static final MiapiRegistry<Upgrade> UPGRADE_MIAPI_REGISTRY = MiapiRegistry.getInstance(Upgrade.class);
    public static final ResourceLocation upgradeId = Miapi.id("upgrade");

    public static final Codec<Integer> INT_CODEC = Codec.withAlternative(Codec.STRING.xmap(Integer::valueOf, i -> "" + i), Codec.INT);

    public static final Codec<Upgrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("tag").forGetter(Upgrade::moduleTag),
            ConditionManager.CONDITION_CODEC_DIRECT.fieldOf("condition").forGetter(Upgrade::condition),
            Codec.unboundedMap(INT_CODEC, PropertyHolder.MAP_CODEC.codec()).fieldOf("properties").forGetter(Upgrade::properties),
            ResourceLocation.CODEC.listOf().optionalFieldOf("incompatible", List.of()).forGetter(Upgrade::incompatible),
            INT_CODEC.optionalFieldOf("max", 0).forGetter(Upgrade::max),
            INT_CODEC.optionalFieldOf("cost", 1).forGetter(Upgrade::cost)
    ).apply(instance, Upgrade::new));
    public static final Codec<Map<ResourceLocation, Integer>> MODULE_UPGRADE_ID_CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, INT_CODEC);

    public static DataComponentType<Integer> COMPONENT = DataComponentType.<Integer>builder()
            .persistent(ExtraCodecs.NON_NEGATIVE_INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT).build();

    // Dummy XP cost logic (double per level)
    public static int xpCost(int oldUpgrades) {
        return (int) Math.pow(2, oldUpgrades) * 10 + 100;
    }

    public Component name() {
        ResourceLocation id = getID();
        return Component.translatable("miapi.upgrade." + id.toString().replace(":", "."));
    }

    public Component description() {
        ResourceLocation id = getID();
        return Component.translatable("miapi.upgrade." + id.toString().replace(":", ".") + ".description");
    }

    public ResourceLocation getID() {
        return UPGRADE_MIAPI_REGISTRY.findKey(this);
    }

    public boolean isAllowed(Collection<Upgrade> upgrades) {
        return upgrades.stream().filter(upgrade -> incompatible().contains(upgrade.getID())).findAny().isEmpty();
    }

    public static void setup() {
        ReloadHandlerBuilder.builder("miapi/upgrade")
                .clear(UPGRADE_MIAPI_REGISTRY::clear)
                .codec(CODEC, (isClient, path, data, registryAccess) -> UPGRADE_MIAPI_REGISTRY.register(path, data));

        RegistryInventory.EDIT_OPTION_MIAPI_REGISTRY.register(Miapi.id("module_upgrades"), new UpgradeEditOption());

        StatResolver.registerResolver("module_upgrade", (data, instance) -> {
            if (instance.moduleData.containsKey(Upgrade.upgradeId)) {
                ResourceLocation id = Miapi.id(data.replaceFirst("\\.", ":"));

                var levelMap = Upgrade.MODULE_UPGRADE_ID_CODEC.decode(JsonOpsBooleanPatched.INSTANCE, instance.moduleData.get(Upgrade.upgradeId)).getOrThrow(
                        (s) -> new DecoderException("Could not decode UpgradeID " + s));
                return levelMap.getFirst().getOrDefault(id, 0);

            }
            return 0;
        });
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(Miapi.id("module_upgrade_xp"), () -> COMPONENT);
        MiapiEvents.MODULAR_ITEM_DAMAGE.register(
                (damage, itemStack, level) ->
                        itemStack.update(COMPONENT, 0, (old) -> old + damage));


        PropertyResolver.register("miapi/upgrade", (moduleInstance, oldMap) -> {
            AtomicReference<Map<ModuleProperty<?>, Object>> map = new AtomicReference<>(new ConcurrentHashMap<>(oldMap));
            var json = moduleInstance.moduleData.get(upgradeId);

            if (json != null) {
                MODULE_UPGRADE_ID_CODEC.parse(JsonOpsBooleanPatched.INSTANCE, json).result().ifPresent(upgrades -> {
                    for (ResourceLocation upgradeId : upgrades.keySet()) {
                        int level = upgrades.get(upgradeId);
                        Upgrade upgrade = UPGRADE_MIAPI_REGISTRY.get(upgradeId);
                        if (upgrade != null) {
                            PropertyHolder selectedHolder = null;
                            int bestLevel = Integer.MIN_VALUE;
                            for (Map.Entry<Integer, PropertyHolder> entry : upgrade.properties().entrySet()) {
                                int keyLevel = entry.getKey();
                                if (keyLevel <= level && keyLevel > bestLevel) {
                                    bestLevel = keyLevel;
                                    selectedHolder = entry.getValue();
                                }
                            }
                            if (selectedHolder != null) {
                                map.set(selectedHolder.applyHolder(map.get(), Optional.of(upgrade.name())));
                            }
                        }
                    }
                });
            }
            return map.get();
        });
    }

}

