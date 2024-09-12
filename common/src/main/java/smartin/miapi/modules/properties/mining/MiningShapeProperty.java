package smartin.miapi.modules.properties.mining;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.mining.condition.AlwaysMiningCondition;
import smartin.miapi.modules.properties.mining.condition.BlockTagCondition;
import smartin.miapi.modules.properties.mining.condition.MiningCondition;
import smartin.miapi.modules.properties.mining.mode.InstantMiningMode;
import smartin.miapi.modules.properties.mining.mode.MiningMode;
import smartin.miapi.modules.properties.mining.mode.StaggeredMiningMode;
import smartin.miapi.modules.properties.mining.modifier.MiningModifier;
import smartin.miapi.modules.properties.mining.modifier.SameBlockModifier;
import smartin.miapi.modules.properties.mining.shape.CubeMiningShape;
import smartin.miapi.modules.properties.mining.shape.MiningShape;
import smartin.miapi.modules.properties.mining.shape.VeinMiningShape;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

/**
 * The `MiningShapeProperty` class manages the complex functionality of mining multiple blocks when only one is mined.
 * This property allows for different mining shapes and modes, enabling advanced mining behavior like area and vein mining.
 *
 * @header Mining Shape Property
 * @path /data_types/properties/mining/shape
 * @description_start
 * The MiningShapeProperty controls how tools mine multiple blocks based on the configured mining shapes, conditions, and modes.
 * This allows for varied mining behaviors such as mining entire veins or large areas with a single action.
 *
 * The property supports different mining shapes (e.g., cubes, veins), conditions (e.g., block tags, always mine), and modes (e.g., instant, staggered).
 * It also incorporates mining modifiers that can influence mining behavior, such as requiring all blocks to be the same type.
 * @description_end
 * @data mining_modes: instant or staggered exist by default
 * @data mining_conditions: always and block_tag exist by default
 * @data mining_modifiers:require_same exists by default
 */

public class MiningShapeProperty extends CodecProperty<List<MiningShapeEntry>> {
    public static final ResourceLocation KEY = Miapi.id("mining_shape");
    public static MiningShapeProperty property;
    public static Map<ResourceLocation, MapCodec<? extends MiningCondition>> miningConditionMap = new HashMap<>();
    public static Map<ResourceLocation, MapCodec<? extends MiningShape>> miningShapeMap = new HashMap<>();
    public static Map<ResourceLocation, MapCodec<? extends MiningMode>> miningModeMap = new HashMap<>();
    public static Map<ResourceLocation, Codec<? extends MiningModifier>> miningModifierMap = new HashMap<>();
    public static List<BlockPos> blockedPositions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public List<MiningShapeEntry> merge(List<MiningShapeEntry> left, List<MiningShapeEntry> right, MergeType mergeType) {
        return List.of();
    }


    public MiningShapeProperty() {
        super(Codec.list(MiningShapeEntry.CODEC));
        property = this;
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (!level.isClientSide() && !player.isShiftKeyDown() && !blockedPositions.contains(pos)) {
                ItemStack miningItem = player.getMainHandItem();
                List<MiningShapeEntry> miningShapeJsons = getData(miningItem).orElse(new ArrayList<>());
                HitResult hitResult = player.pick(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 0, false);
                if (hitResult instanceof BlockHitResult blockHitResult) {
                    Direction facing = blockHitResult.getDirection();
                    miningShapeJsons.stream().filter(miningShapeJson ->
                                    miningShapeJson.condition().canMine(player, level, miningItem, pos, facing)).
                            forEach(miningShapeJson ->
                                    miningShapeJson.execute(pos, level, miningItem, player, facing));
                }
            }
            return EventResult.pass();
        });

        miningModeMap.put(Miapi.id("instant"), InstantMiningMode.CODEC);
        miningModeMap.put(Miapi.id("staggered"), StaggeredMiningMode.CODEC);

        miningModifierMap.put(Miapi.id("require_same"), SameBlockModifier.CODEC);

        miningConditionMap.put(Miapi.id("always"), AlwaysMiningCondition.CODEC);
        miningConditionMap.put(Miapi.id("block_tag"), BlockTagCondition.CODEC);

        //MiningLevelProperty.miningCapabilities.keySet().forEach(s -> miningConditionMap.put(s, new MiningTypeCondition(s)));

        miningShapeMap.put(Miapi.id("cube"), CubeMiningShape.CODEC);
        miningShapeMap.put(Miapi.id("vein"), VeinMiningShape.CODEC);
    }
}
