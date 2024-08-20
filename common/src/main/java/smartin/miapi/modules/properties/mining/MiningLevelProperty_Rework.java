package smartin.miapi.modules.properties.mining;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.*;

import java.util.*;

/**
 * The Property controls mining speed and levels of tools
 */
/*
public class MiningLevelProperty_Rework extends CodecProperty<Map<String, MiningLevelProperty_Rework.MiningRule>> implements ComponentApplyProperty {
    public static MiningLevelProperty_Rework property;
    public static final ResourceLocation KEY = Miapi.id("mining_level");
    public static Map<String, TagKey<Block>> miningCapabilities = new HashMap<>();
    public static Codec<Map<String, MiningRule>> CODEC = Codec.unboundedMap(Codec.STRING, MiningRule.CODEC);
    public static String CACHEKEY = KEY + "finished_component";


    public MiningLevelProperty_Rework() {
        super(CODEC);
        ModularItemCache.setSupplier(CACHEKEY, this::asComponent);
    }

    @Override
    public Map<String, MiningRule> merge(Map<String, MiningRule> left, Map<String, MiningRule> right, MergeType mergeType) {
        return ModuleProperty.mergeMap(left, right, mergeType, (triple) -> MiningRule.merge(triple.getLeft(), triple.getMiddle(), triple.getRight()));
    }

    @Override
    public Map<String, MiningLevelProperty_Rework.MiningRule> initialize(Map<String, MiningLevelProperty_Rework.MiningRule> data, ModuleInstance context) {
        Map<String, MiningLevelProperty_Rework.MiningRule> initialized = new HashMap<>();
        data.forEach((key, entry) -> initialized.put(key, entry.initialize(context)));
        return initialized;
    }

    Tool asComponent(ItemStack itemStack) {
        List<Tool.Rule> rules = new ArrayList<>();
        getData(itemStack).orElse(new HashMap<>()).values().forEach(miningRule -> {
            rules.addAll(miningRule.asRules());
        });
        return new Tool(rules, 1.0f, 1);
    }

    Tool asComponentCached(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, CACHEKEY, new Tool(new ArrayList<>(), 0, 0));
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        List<Tool.Rule> rules = new ArrayList<>();
        getData(itemStack).orElse(new HashMap<>()).values().forEach(miningRule -> {
            rules.addAll(miningRule.asRules());
        });
        itemStack.set(DataComponents.TOOL, new Tool(rules, 0.0f, 0));
    }

    public static float getDestroySpeed(ItemStack stack, BlockState state) {
        Tool tool = stack.get(DataComponents.TOOL);
        return Math.max(property.asComponentCached(stack).getMiningSpeed(state), tool != null ? tool.getMiningSpeed(state) : 1.0F);
    }

    public static boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        Tool tool = stack.get(DataComponents.TOOL);
        Tool ourComponent = property.asComponentCached(stack);
        if (tool == null && ourComponent.rules().isEmpty()) {
            return false;
        }
        int toolDamage = ourComponent.damagePerBlock();
        if (tool != null) {
            toolDamage = Math.max(toolDamage, tool.damagePerBlock());
        }
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F && toolDamage > 0) {
            stack.hurtAndBreak(toolDamage, miningEntity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        Tool tool = stack.get(DataComponents.TOOL);
        return tool != null && tool.isCorrectForDrops(state) || property.asComponentCached(stack).isCorrectForDrops(state);
    }

    public record MiningRule(MaterialSensitiveBlockSet blocks, MaterialSensitiveBlockSet blacklist,
                             DoubleOperationResolvable speed,
                             Optional<Boolean> correctForDrops, boolean useMaterial) {
        public static final Codec<MiningRule> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                            MaterialSensitiveBlockSet.CODEC
                                    .optionalFieldOf("blocks", new MaterialSensitiveBlockSet())
                                    .forGetter(MiningRule::blocks),
                            MaterialSensitiveBlockSet.CODEC
                                    .optionalFieldOf("blacklist", new MaterialSensitiveBlockSet())
                                    .forGetter(MiningRule::blacklist),
                            DoubleOperationResolvable.CODEC
                                    .optionalFieldOf("speed", new DoubleOperationResolvable(0))
                                    .forGetter(MiningRule::speed),
                            Codec.BOOL
                                    .optionalFieldOf("correct_for_drops")
                                    .forGetter(MiningRule::correctForDrops),
                            Codec.BOOL
                                    .optionalFieldOf("use_material", false)
                                    .forGetter(MiningRule::useMaterial))
                    .apply(instance, MiningRule::new);
        });

        public static MiningRule merge(MiningRule left, MiningRule right, MergeType mergeType) {
            DoubleOperationResolvable merged = left.speed().merge(right.speed(), mergeType);
            Optional<Boolean> mergedBoolean = Optional.empty();
            if (left.correctForDrops().isPresent()) {
                mergedBoolean = left.correctForDrops();
            }
            if (right.correctForDrops().isPresent()) {
                mergedBoolean = right.correctForDrops();
            }
            return new MiningRule(
                    MaterialSensitiveBlockSet.merge(left.blocks, right.blocks),
                    MaterialSensitiveBlockSet.merge(left.blacklist, left.blacklist),
                    merged,
                    mergedBoolean,
                    left.useMaterial() || right.useMaterial());
        }

        public MiningRule initialize(ModuleInstance moduleInstance) {
            var blocks = blocks().initialize(moduleInstance);
            var blocks = blacklist().initialize(moduleInstance);
            Optional<Boolean> correctForDrops = correctForDrops();
            if (useMaterial()) {
                Material material = MaterialProperty.getMaterial(moduleInstance);
                if (material != null) {
                    blocks = blocks.stream().filter(b -> b.is(material.getIncorrectBlocksForDrops())).toList();
                    correctForDrops = Optional.of(true);
                }
            }
            return new MiningRule(blocks().initialize(moduleInstance), blacklist().initialize(moduleInstance), speed(), correctForDrops, false);
        }

        public List<Tool.Rule> asRules() {
            return List.of(
                    new Tool.Rule(blacklist, Optional.of((float) speed().evaluate(1.0, 1.0)), Optional.of(false)),
                    new Tool.Rule(blocks, Optional.of((float) speed().evaluate(1.0, 1.0)), correctForDrops())
            );
        }
    }

    public static class MaterialSensitiveBlockSet {
        HolderSet<Block> initialized;
        List<HolderSet<Block>> blocks = new ArrayList<>();
        List<String> materialSensitive = new ArrayList<>();
        public static Codec<MaterialSensitiveBlockSet> CODEC = Codec.withAlternative(
                RecordCodecBuilder.create((instance) -> {
                    return instance.group(
                                    RegistryCodecs
                                            .homogeneousList(Registries.BLOCK)
                                            .fieldOf("blocks")
                                            .forGetter(MaterialSensitiveBlockSet::mergeBlocks),
                                    Codec.list(Codec.STRING)
                                            .fieldOf("material_sensitive")
                                            .forGetter(set -> set.materialSensitive))
                            .apply(instance, (blocksSet, strings) -> {
                                var newSet = new MaterialSensitiveBlockSet();
                                newSet.blocks.add(blocksSet);
                                newSet.materialSensitive.addAll(strings);
                                return newSet;
                            });
                }),
                Codec.withAlternative(
                        new Codec<MaterialSensitiveBlockSet>() {
                            @Override
                            public <T> DataResult<T> encode(MaterialSensitiveBlockSet input, DynamicOps<T> ops, T prefix) {
                                return DataResult.error(() -> "cannot encode material sensitive blocks this way");
                            }

                            @Override
                            public <T> DataResult<Pair<MaterialSensitiveBlockSet, T>> decode(DynamicOps<T> ops, T input) {
                                var result = RegistryCodecs
                                        .homogeneousList(Registries.BLOCK).decode(ops, input);
                                if (result.isSuccess()) {
                                    MaterialSensitiveBlockSet set = new MaterialSensitiveBlockSet();
                                    set.blocks.add(result.getOrThrow().getFirst());
                                    return DataResult.success(new Pair<>(set, result.getOrThrow().getSecond()));
                                }

                                return DataResult.error(() -> "cannot decode material sensitive blocks this way");
                            }
                        },
                        new Codec<MaterialSensitiveBlockSet>() {
                            @Override
                            public <T> DataResult<T> encode(MaterialSensitiveBlockSet input, DynamicOps<T> ops, T prefix) {
                                return DataResult.error(() -> "cannot encode material sensitive blocks this way");
                            }

                            @Override
                            public <T> DataResult<Pair<MaterialSensitiveBlockSet, T>> decode(DynamicOps<T> ops, T input) {
                                var result = Codec.STRING.decode(ops, input);
                                if (result.isSuccess()) {
                                    MaterialSensitiveBlockSet set = new MaterialSensitiveBlockSet();
                                    set.materialSensitive.add(result.getOrThrow().getFirst());
                                    return DataResult.success(new Pair<>(set, result.getOrThrow().getSecond()));
                                }

                                return DataResult.error(() -> "cannot decode material sensitive blocks this way");
                            }
                        }
                )
        );

        public static MaterialSensitiveBlockSet merge(MaterialSensitiveBlockSet left, MaterialSensitiveBlockSet right) {
            if (left.initialized != null && right.initialized != null) {
                List<Holder<Block>> initializedMerge = new ArrayList<>(right.initialized.stream().toList());
                initializedMerge.addAll(left.initialized.stream().toList());
                MaterialSensitiveBlockSet merged = new MaterialSensitiveBlockSet();
                merged.initialized = HolderSet.direct(initializedMerge);
                return merged;
            }
            List<HolderSet<Block>> mergedBlocks = new ArrayList<>(left.blocks);
            mergedBlocks.addAll(right.blocks);
            List<String> mergedMaterial = new ArrayList<>(left.materialSensitive);
            mergedMaterial.addAll(right.materialSensitive);
            MaterialSensitiveBlockSet merged = new MaterialSensitiveBlockSet();
            merged.blocks = mergedBlocks;
            merged.materialSensitive = mergedMaterial;
            return merged;
        }

        public MaterialSensitiveBlockSet initialize(ModuleInstance moduleInstance) {
            List<Holder<Block>> mergedBlocks = new ArrayList<>();
            blocks.forEach(set -> {
                mergedBlocks.addAll(set.stream().toList());
            });

            materialSensitive.forEach(data -> {
                String resolved = StatResolver.resolveString(data, moduleInstance);
                var encoded = Codec.STRING.encodeStart(JsonOps.INSTANCE, resolved).getOrThrow();
                var decoded = RegistryCodecs
                        .homogeneousList(Registries.BLOCK).decode(JsonOps.INSTANCE, encoded);
                mergedBlocks.addAll(decoded.getOrThrow().getFirst().stream().toList());
            });
            MaterialSensitiveBlockSet initialized = new MaterialSensitiveBlockSet();
            initialized.initialized = HolderSet.direct(mergedBlocks);
            return initialized;
        }

        public HolderSet<Block> mergeBlocks() {
            List<Holder<Block>> mergedBlocks = new ArrayList<>();
            blocks.forEach(set -> {
                mergedBlocks.addAll(set.stream().toList());
            });
            return HolderSet.direct(mergedBlocks);
        }
    }
}

 */