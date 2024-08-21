package smartin.miapi.modules.properties.mining;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.*;

import java.util.*;

/**
 * The Property controls mining speed and levels of tools
 */
public class MiningLevelProperty extends CodecProperty<Map<String, MiningLevelProperty.MiningRule>> implements ComponentApplyProperty {
    public static MiningLevelProperty property;
    public static final ResourceLocation KEY = Miapi.id("mining_level");
    public static Map<String, TagKey<Block>> miningCapabilities = new HashMap<>();
    public static Codec<Map<String, MiningRule>> CODEC = Codec.unboundedMap(Codec.STRING, MiningRule.CODEC);
    public static String CACHEKEY = KEY + "finished_component";


    public MiningLevelProperty() {
        super(CODEC);
        property = this;
        ModularItemCache.setSupplier(CACHEKEY, this::asComponent);
    }

    @Override
    public Map<String, MiningRule> merge(Map<String, MiningRule> left, Map<String, MiningRule> right, MergeType mergeType) {
        return ModuleProperty.mergeMap(left, right, mergeType, (triple) -> MiningRule.merge(triple.getLeft(), triple.getMiddle(), triple.getRight()));
    }

    @Override
    public Map<String, MiningLevelProperty.MiningRule> initialize(Map<String, MiningLevelProperty.MiningRule> data, ModuleInstance context) {
        Map<String, MiningLevelProperty.MiningRule> initialized = new HashMap<>();
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
        var map = getData(itemStack).orElse(new HashMap<>());
        map.values().forEach(miningRule -> {
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

    public record MiningRule(HolderSet<Block> blocks, HolderSet<Block> blacklist, DoubleOperationResolvable speed,
                             Optional<Boolean> correctForDrops, boolean useMaterial,
                             List<Material> respectMaterialBlacklists) {
        public static final Codec<MiningRule> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                            RegistryCodecs
                                    .homogeneousList(Registries.BLOCK)
                                    .optionalFieldOf("block_list", HolderSet.direct())
                                    .forGetter(MiningRule::blocks),
                            ResourceLocation.CODEC
                                    .optionalFieldOf("blocks")
                                    .forGetter((rule) -> Optional.empty()),
                            RegistryCodecs
                                    .homogeneousList(Registries.BLOCK)
                                    .optionalFieldOf("blacklist", HolderSet.direct())
                                    .forGetter(MiningRule::blacklist),
                            ResourceLocation.CODEC
                                    .optionalFieldOf("blacklist_tag")
                                    .forGetter((rule) -> Optional.empty()),
                            DoubleOperationResolvable.CODEC
                                    .optionalFieldOf("speed", new DoubleOperationResolvable(1))
                                    .forGetter(MiningRule::speed),
                            Codec.BOOL
                                    .optionalFieldOf("correct_for_drops")
                                    .forGetter(MiningRule::correctForDrops),
                            Codec.BOOL
                                    .optionalFieldOf("use_material", false)
                                    .forGetter(MiningRule::useMaterial))
                    .apply(instance, (blockList, blockTag, blacklist, blackTag, speed, correct, useMaterial) -> {
                        List<Holder<Block>> blocks = new ArrayList<>();
                        blockTag.ifPresent(location -> BuiltInRegistries.BLOCK.getTags().forEach(tagKeyNamedPair -> {
                            if (tagKeyNamedPair.getFirst().location().equals(location)) {
                                tagKeyNamedPair.getSecond().forEach(blocks::add);
                            }
                        }));
                        blockList.stream().forEach(blocks::add);
                        List<Holder<Block>> black = new ArrayList<>();
                        blackTag.ifPresent(location -> BuiltInRegistries.BLOCK.getTags().forEach(tagKeyNamedPair -> {
                            if (tagKeyNamedPair.getFirst().location().equals(location)) {
                                tagKeyNamedPair.getSecond().forEach(black::add);
                            }
                        }));
                        blacklist.stream().forEach(black::add);
                        return new MiningRule(
                                HolderSet.direct(blocks),
                                HolderSet.direct(black), speed, correct, useMaterial, new ArrayList<>());
                    });
        });

        public static MiningRule merge(MiningRule left, MiningRule right, MergeType mergeType) {
            List<Holder<Block>> blocks = new ArrayList<>(left.blocks().stream().toList());
            blocks.addAll(right.blocks().stream().toList());
            List<Holder<Block>> blacklist = new ArrayList<>(left.blacklist().stream().toList());
            blacklist.addAll(right.blacklist().stream().toList());
            DoubleOperationResolvable merged = left.speed().merge(right.speed(), mergeType);
            Optional<Boolean> mergedBoolean = Optional.empty();
            if (left.correctForDrops().isPresent()) {
                mergedBoolean = left.correctForDrops();
            }
            if (right.correctForDrops().isPresent()) {
                mergedBoolean = right.correctForDrops();
            }
            List<Material> mergedMaterials = new ArrayList<>(left.respectMaterialBlacklists());
            mergedMaterials.addAll(right.respectMaterialBlacklists);
            return new MiningRule(HolderSet.direct(blocks), HolderSet.direct(blacklist), merged, mergedBoolean, left.useMaterial() || right.useMaterial(), mergedMaterials);
        }

        public MiningRule initialize(ModuleInstance moduleInstance) {
            List<Holder<Block>> blockBlackList = new ArrayList<>(blacklist().stream().toList());
            Optional<Boolean> correctForDrops = correctForDrops();
            List<Material> mergedMaterials = new ArrayList<>(this.respectMaterialBlacklists());
            if (useMaterial()) {
                Material material = MaterialProperty.getMaterial(moduleInstance);
                if (material != null) {
                    mergedMaterials.add(material);
                }
            }
            return new MiningRule(HolderSet.direct(blocks().stream().toList()), HolderSet.direct(blockBlackList), speed().initialize(moduleInstance), correctForDrops, useMaterial(), mergedMaterials);
        }

        public List<Tool.Rule> asRules() {
            float speedEvaluated = (float) speed().evaluate(1.0, 1.0);
            if (speedEvaluated < 1) {
                speedEvaluated = 1.0f;
            }
            if (useMaterial()) {
                List<Block> canDropBlocks = new ArrayList<>(blocks().stream().map(Holder::value).distinct().toList());
                if (!canDropBlocks.isEmpty()) {
                    blacklist()
                            .stream()
                            .map(Holder::value)
                            .distinct()
                            .forEach(canDropBlocks::remove);
                }
                List<Block> blocksWithMiningSpeed = new ArrayList<>(canDropBlocks);
                List<Block> toRemoveFromMaterial = new ArrayList<>();
                respectMaterialBlacklists().forEach(material -> {
                    if (toRemoveFromMaterial.isEmpty()) {
                        BuiltInRegistries.BLOCK.getTag(
                                material.getIncorrectBlocksForDrops()
                        ).ifPresent(named -> {
                            named.stream().map(Holder::value).distinct().forEach(toRemoveFromMaterial::add);
                        });
                    } else {
                        List<Block> notShared = new ArrayList<>(toRemoveFromMaterial);
                        BuiltInRegistries.BLOCK.getTag(
                                material.getIncorrectBlocksForDrops()
                        ).ifPresent(named -> {
                            named.stream().map(Holder::value).distinct().forEach(notShared::remove);
                        });
                        notShared.forEach(toRemoveFromMaterial::remove);
                    }
                });
                toRemoveFromMaterial.forEach(canDropBlocks::remove);
                List<Block> rawBlocks = canDropBlocks.stream().distinct().toList();
                Tool.Rule mineAndDrop = Tool.Rule.minesAndDrops(rawBlocks, speedEvaluated);
                Tool.Rule overrideSpeed = Tool.Rule.overrideSpeed(blocksWithMiningSpeed, speedEvaluated);
                return List.of(mineAndDrop, overrideSpeed);
            }
            return List.of(
                    new Tool.Rule(blacklist, Optional.of(speedEvaluated), Optional.of(false)),
                    new Tool.Rule(blocks, Optional.of(speedEvaluated), correctForDrops())
            );
        }
    }
}
