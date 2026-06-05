package smartin.miapi.modules.properties.mining;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.*;

import java.util.*;

/**
 * The `MiningLevelProperty` class defines the property for controlling mining speed and levels of tools.
 * This property allows for detailed control over how tools interact with different blocks, including mining speed adjustments and block compatibility rules.
 *
 * @header Mining Level Property
 * @path /data_types/properties/mining/mining
 * @description_start The MiningLevelProperty manages the mining capabilities of tools, determining their effectiveness based on various rules and configurations.
 * These configurations include block-specific mining speeds, block blacklists, and conditions for correct tool usage.
 * <p>
 * By default, the mining level is influenced by the material properties associated with the tool.
 * Custom rules can be defined to adjust mining speeds and tool compatibilities dynamically.
 * @description_end
 * @path /data_types/properties/mining/mining_level
 * @data mining_rules:
 * @data `blocks`: List of blocks that the tool can mine.
 * @data `block_list | blacklist_tag`: List of blocks that the tool cannot mine.
 * @data `speed`: A resolvable value that determines the mining speed for the tool.
 * @data `correctForDrops`: Optional boolean indicating whether the tool is correct for drops.
 * @data `useMaterial`: Boolean indicating if material properties should affect mining rules.
 */

public class MiningLevelProperty extends CodecProperty<Map<String, MiningLevelProperty.MiningRule>> implements ComponentApplyProperty {
    public static MiningLevelProperty property;
    public static final ResourceLocation KEY = Miapi.id("mining_level");
    public static Map<String, TagKey<Block>> miningCapabilities = new HashMap<>();
    public static Codec<Map<String, MiningRule>> CODEC = Codec.unboundedMap(Codec.STRING, MiningRule.CODEC);
    public static String CACHEKEY = KEY + "finished_component";
    public static String CACHEKEY_SPEED = KEY + "destroy_speed";


    public MiningLevelProperty() {
        super(CODEC);
        property = this;
        ModularItemCache.setSupplier(CACHEKEY, this::asComponent);
        ModularItemCache.setSupplier(CACHEKEY_SPEED, (stack) -> new Reference2FloatOpenHashMap<Block>());
    }


    @Override
    public Map<String, MiningRule> merge(Map<String, MiningRule> left, Map<String, MiningRule> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (k, l, r) -> MiningRule.merge(l, r, mergeType));
    }

    @Override
    public Map<String, MiningLevelProperty.MiningRule> initialize(Map<String, MiningLevelProperty.MiningRule> data, ModuleInstance context) {
        Map<String, MiningLevelProperty.MiningRule> initialized = new HashMap<>();
        data.forEach((key, entry) -> initialized.put(key, entry.initialize(context)));
        return initialized;
    }

    Tool asComponent(ItemStack itemStack) {
        List<Tool.Rule> rules = new ArrayList<>();
        var rawData = getData(itemStack).orElse(new HashMap<>());
        rawData.values().forEach(miningRule -> {
            rules.addAll(miningRule.asRules());
        });
        return new Tool(rules, 1.0f, 1);
    }

    Tool asComponentCached(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, CACHEKEY, new Tool(new ArrayList<>(), 1, 1));
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        itemStack.set(DataComponents.TOOL, new Tool(new ArrayList<>(), 1.0f, 1));
    }

    public static float getDestroySpeed(ItemStack stack, BlockState state) {
        return ModularItemCache.get(stack, CACHEKEY_SPEED,
                new Reference2FloatOpenHashMap<Block>())
                .computeIfAbsent(state.getBlock(), (b) -> Math.max(property.asComponentCached(stack)
                        .getMiningSpeed(state), 1.0F));
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        Tool tool = property.asComponentCached(stack);
        return tool.isCorrectForDrops(state);
    }

    public static boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        Tool ourComponent = property.asComponentCached(stack);
        int toolDamage = ourComponent.damagePerBlock();
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F && toolDamage > 0) {
            stack.hurtAndBreak(toolDamage, miningEntity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    public record MiningRule(List<HolderSet<Block>> blocks, List<HolderSet<Block>> blacklist,
                             DoubleOperationResolvable speed,
                             Optional<Boolean> correctForDrops, boolean useMaterial,
                             List<Material> respectMaterialBlacklists) {
        public static final Codec<MiningRule> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                            RegistryCodecs
                                    .homogeneousList(Registries.BLOCK)
                                    .listOf()
                                    .optionalFieldOf("allowed", List.of())
                                    .forGetter(MiningRule::blocks),
                            RegistryCodecs
                                    .homogeneousList(Registries.BLOCK)
                                    .listOf()
                                    .optionalFieldOf("forbidden", List.of())
                                    .forGetter(MiningRule::blacklist),
                            DoubleOperationResolvable.CODEC
                                    .optionalFieldOf("speed", new DoubleOperationResolvable(1))
                                    .forGetter(MiningRule::speed),
                            Miapi.FIXED_BOOL_CODEC
                                    .optionalFieldOf("correct_for_drops")
                                    .forGetter(MiningRule::correctForDrops),
                            Miapi.FIXED_BOOL_CODEC
                                    .optionalFieldOf("use_material", false)
                                    .forGetter(MiningRule::useMaterial))
                    .apply(instance, (blockList, blacklist, speed, correct, useMaterial) -> new MiningRule(
                            blockList,
                            blacklist, speed, correct, useMaterial, new ArrayList<>()));
        });

        public static MiningRule merge(MiningRule left, MiningRule right, MergeType mergeType) {
            List<HolderSet<Block>> blocks = MergeAble.mergeList(left.blocks(), right.blocks(), mergeType);
            List<HolderSet<Block>> blacklist = MergeAble.mergeList(left.blacklist(), right.blacklist(), mergeType);
            List<Material> mergedMaterials = MergeAble.mergeList(left.respectMaterialBlacklists(), right.respectMaterialBlacklists(), mergeType);
            DoubleOperationResolvable merged = left.speed().merge(right.speed(), mergeType);
            Optional<Boolean> mergedBoolean = Optional.empty();
            if (left.correctForDrops().isPresent()) {
                mergedBoolean = left.correctForDrops();
            }
            if (right.correctForDrops().isPresent()) {
                mergedBoolean = right.correctForDrops();
            }
            return new MiningRule(blocks, blacklist, merged, mergedBoolean, left.useMaterial() || right.useMaterial(), mergedMaterials);
        }

        public MiningRule initialize(ModuleInstance moduleInstance) {
            List<HolderSet<Block>> blockBlackList = new ArrayList<>(blacklist().stream().toList());
            Optional<Boolean> correctForDrops = correctForDrops();
            List<Material> mergedMaterials = new ArrayList<>(this.respectMaterialBlacklists());
            if (useMaterial()) {
                Material material = MaterialProperty.getMaterial(moduleInstance);
                if (material != null) {
                    mergedMaterials.add(material);
                }
            }
            return new MiningRule(blocks().stream().toList(), blockBlackList, speed().initialize(moduleInstance), correctForDrops, useMaterial(), mergedMaterials);
        }

        public List<Tool.Rule> asRules() {
            float speedEvaluated = (float) speed().evaluate(0.0, 1.0);
            if (speedEvaluated < 1) {
                speedEvaluated = 1.0f;
            }
            if (useMaterial()) {
                List<Holder<Block>> canDropBlocks = toList(blocks());
                if (!canDropBlocks.isEmpty()) {
                    toList(blacklist())
                            .stream()
                            .map(Holder::value)
                            .distinct()
                            .forEach(canDropBlocks::remove);
                }
                List<Holder<Block>> blocksWithMiningSpeed = new ArrayList<>(canDropBlocks);
                List<Holder<Block>> toRemoveFromMaterial = null;

                for (var material : respectMaterialBlacklists()) {
                    Optional<HolderSet.Named<Block>> maybeTag = BuiltInRegistries.BLOCK.getTag(material.getIncorrectBlocksForDrops());
                    if (maybeTag.isEmpty()) continue;

                    List<Holder<Block>> currentList = maybeTag.get().stream().distinct().toList();

                    if (toRemoveFromMaterial == null) {
                        toRemoveFromMaterial = new ArrayList<>(currentList);
                    } else {
                        toRemoveFromMaterial.retainAll(currentList);
                    }
                }

                if (toRemoveFromMaterial == null) {
                    toRemoveFromMaterial = new ArrayList<>();
                }


                toRemoveFromMaterial.forEach(canDropBlocks::remove);

                toRemoveFromMaterial.forEach(canDropBlocks::remove);
                List<Block> rawBlocks = new HashSet<>(canDropBlocks).stream().distinct().map(Holder::value).toList();
                Tool.Rule mineAndDrop = Tool.Rule.minesAndDrops(rawBlocks, speedEvaluated);
                Tool.Rule overrideSpeed = Tool.Rule.overrideSpeed(blocksWithMiningSpeed.stream().map(Holder::value).toList(), speedEvaluated);
                return List.of(mineAndDrop, overrideSpeed);
            }
            List<Holder<Block>> canDropBlocks = new ArrayList<>();
            blocks.forEach(set -> set.forEach(canDropBlocks::add));
            List<Holder<Block>> forbiddenBlocks = new ArrayList<>();
            blacklist.forEach(set -> set.forEach(canDropBlocks::add));
            return List.of(
                    new Tool.Rule(HolderSet.direct(forbiddenBlocks), Optional.of(speedEvaluated), Optional.of(false)),
                    new Tool.Rule(HolderSet.direct(canDropBlocks), Optional.of(speedEvaluated), correctForDrops())
            );
        }
    }

    /**
     * yeah, idk ask the easy anvil team why they are incompetent and dont properly add their tags
     *
     * @param blocks
     * @return
     */
    private static List<Holder<Block>> toList(List<HolderSet<Block>> blocks) {
        List<Holder<Block>> canDropBlocks = new ArrayList<>();
        BuiltInRegistries.BLOCK.forEach(block -> {
            blocks.forEach(set -> {
                var holder = BuiltInRegistries.BLOCK.wrapAsHolder(block);
                if (set.contains(holder)) {
                    canDropBlocks.add(holder);
                }
            });
        });
        return canDropBlocks;
    }

    /**
     * yeah, idk ask the easy anvil team why they are incompetent and dont properly add their tags
     *
     * @param blocks
     * @return
     */
    private static List<Holder<Block>> toList(HolderSet<Block> blocks) {
        List<Holder<Block>> canDropBlocks = new ArrayList<>();
        BuiltInRegistries.BLOCK.forEach(block -> {
            var holder = BuiltInRegistries.BLOCK.wrapAsHolder(block);
            if (blocks.contains(holder)) {
                canDropBlocks.add(holder);
            }
        });
        return canDropBlocks;
    }
}
