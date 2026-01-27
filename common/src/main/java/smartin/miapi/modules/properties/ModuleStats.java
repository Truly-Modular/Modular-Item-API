package smartin.miapi.modules.properties;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.AllowedMaterial;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @header Module Stats Property
 * @path /data_types/properties/module_stats
 * @description_start The ModuleStats property allows for the specification of various statistics associated with a module, where each statistic
 * is represented by a key-value pair.
 * This property is integrated with the Stat Resolver and can be queried by using [module.custom_stat_name].
 * cost refers to the module cost [module.cost]
 * and [module.attribute.minecraft:generic.attack_damage] can be used to refer to the *Modules* attack damage attributes
 * If you want to adjust attributes based on other attributes we heavily recommend the attribute split property instead,
 * as using this can cause circular dependencies in the math.
 * @description_end
 * @data stats: A {@link Map} where each entry consists of a {@link String} key and a {@link Double} value, representing different
 * statistics related to the module. The statistics can include metrics like "cost" and other module-specific data.
 */

public class ModuleStats extends CodecProperty<Map<String, DoubleOperationResolvable>> {
    public static final ResourceLocation KEY = Miapi.id("module_stats");
    public static ModuleStats property;
    public static Codec<Map<String, DoubleOperationResolvable>> CODEC = Codec.dispatchedMap(Codec.STRING, (s) -> DoubleOperationResolvable.CODEC);

    public ModuleStats() {
        super(CODEC);
        property = this;
        StatResolver.registerResolver("module", new StatResolver.Resolver() {

            @Override
            public double resolveDouble(String data, ModuleInstance instance) {
                if (instance.getModule().equals(ItemModule.internal)) {
                    return 1.0;
                }
                if ("cost".equals(data)) {
                    return AllowedMaterial.getMaterialCost(instance);
                }
                if (data != null && data.startsWith("attribute.")) {
                    ResourceLocation id = ResourceLocation.parse(
                            data.replaceFirst("attribute\\.", "")
                    );

                    Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> attributeData = AttributeProperty.property.getData(instance).orElse(Map.of());
                    List<DoubleOperationResolvable.IndividualOperation> operations = new ArrayList<>();
                    attributeData.getOrDefault(id, Map.of()).forEach((operation, innerMap) -> {
                        innerMap.values().forEach(resolvable -> operations.add(
                                new DoubleOperationResolvable.IndividualOperation(
                                        resolvable.getValue(),
                                        switch (operation) {
                                            case ADD_VALUE ->
                                                    DoubleOperationResolvable.IndividualOperation.Operation.ADD_VALUE;
                                            case ADD_MULTIPLIED_BASE ->
                                                    DoubleOperationResolvable.IndividualOperation.Operation.ADD_MULTIPLIED_BASE;
                                            case ADD_MULTIPLIED_TOTAL ->
                                                    DoubleOperationResolvable.IndividualOperation.Operation.ADD_MULTIPLIED_TOTAL;
                                        })));
                    });
                    return new DoubleOperationResolvable(operations).getValue();
                }
                DoubleOperationResolvable resolvable = getData(instance).orElse(new HashMap<>()).get(data);
                if (resolvable != null) {
                    return resolvable.getValue();
                } else {
                    return 0;
                }
            }

            @Override
            public StatResolver.ResolvedDouble resolveWithTrace(String data, ModuleInstance instance) {
                return ModuleStats.this.resolveWithTrace(data, instance);
            }
        });
    }

    public StatResolver.ResolvedDouble resolveWithTrace(String data, ModuleInstance instance) {

        // ─────────────────────────────────────────
        // Internal module shortcut
        // ─────────────────────────────────────────
        if (instance.getModule().equals(ItemModule.internal)) {
            return new StatResolver.ResolvedDouble(
                    1.0,
                    new StatResolver.TraceValue(1.0, Component.literal("module.internal"))
            );
        }

        // ─────────────────────────────────────────
        // Cost
        // ─────────────────────────────────────────
        if ("cost".equals(data)) {
            double value = AllowedMaterial.getMaterialCost(instance);
            return new StatResolver.ResolvedDouble(
                    value,
                    new StatResolver.TraceReference(
                            "module.cost",
                            value,
                            new StatResolver.TraceValue(value, Component.literal("AllowedMaterial.getMaterialCost"))
                    )
            );
        }

        // ─────────────────────────────────────────
        // Attribute passthrough
        // module.attribute.minecraft:generic.armor
        // ─────────────────────────────────────────
        if (data != null && data.startsWith("attribute.")) {

            ResourceLocation id = ResourceLocation.parse(
                    data.replaceFirst("attribute\\.", "")
            );

            Map<ResourceLocation,
                    Map<AttributeModifier.Operation,
                            Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> attributeData =
                    AttributeProperty.property.getData(instance).orElse(Map.of());

            List<StatResolver.TraceNode> operationTraces = new ArrayList<>();
            List<DoubleOperationResolvable.IndividualOperation> operations = new ArrayList<>();

            attributeData.getOrDefault(id, Map.of()).forEach((operation, innerMap) -> {
                innerMap.values().forEach(resolvable -> {

                    double value = resolvable.getValue();

                    DoubleOperationResolvable.IndividualOperation.Operation op =
                            switch (operation) {
                                case ADD_VALUE -> DoubleOperationResolvable.IndividualOperation.Operation.ADD_VALUE;
                                case ADD_MULTIPLIED_BASE ->
                                        DoubleOperationResolvable.IndividualOperation.Operation.ADD_MULTIPLIED_BASE;
                                case ADD_MULTIPLIED_TOTAL ->
                                        DoubleOperationResolvable.IndividualOperation.Operation.ADD_MULTIPLIED_TOTAL;
                            };

                    operations.add(
                            new DoubleOperationResolvable.IndividualOperation(value, op)
                    );

                    operationTraces.add(
                            new StatResolver.TraceOperation(
                                    op.name(),
                                    value,
                                    List.of(
                                            new StatResolver.TraceValue(
                                                    value,
                                                    Component.literal(resolvable.toString())
                                            )
                                    )
                            )
                    );
                });
            });

            double finalValue = new DoubleOperationResolvable(operations).getValue();

            return new StatResolver.ResolvedDouble(
                    finalValue,
                    new StatResolver.TraceOperation(
                            "module.attribute." + id,
                            finalValue,
                            operationTraces
                    )
            );
        }

        // ─────────────────────────────────────────
        // Module stat lookup (module.armor, etc.)
        // ─────────────────────────────────────────
        DoubleOperationResolvable resolvable =
                getData(instance).orElse(Map.of()).get(data);

        if (resolvable != null) {
            double value = resolvable.getValue();



            return new StatResolver.ResolvedDouble(
                    value,
                    new StatResolver.TraceOperation(
                            "module." + data,
                            value,
                            resolvable.getResolvedTrace()
                    )
            );
        }

        // ─────────────────────────────────────────
        // Fallback
        // ─────────────────────────────────────────
        return new StatResolver.ResolvedDouble(
                0,
                new StatResolver.TraceValue(0, Component.literal("module." + data + " (missing)"))
        );
    }

    @Override
    public Map<String, DoubleOperationResolvable> merge(Map<String, DoubleOperationResolvable> left, Map<String, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }

    public Map<String, DoubleOperationResolvable> initialize(Map<String, DoubleOperationResolvable> data, ModuleInstance moduleInstance) {
        Map<String, DoubleOperationResolvable> map = new HashMap<>();
        data.forEach((id, value) -> {
            map.put(id, value.initialize(moduleInstance));
        });
        return map;
    }
}
