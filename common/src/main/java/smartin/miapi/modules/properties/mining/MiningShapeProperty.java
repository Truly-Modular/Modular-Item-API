package smartin.miapi.modules.properties.mining;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
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
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Property Manages the complicated task of Mining Multiple Blocks when only one is mined
 * Area,Vein and other stuff
 */
public class MiningShapeProperty extends CodecBasedProperty<List<MiningShapeEntry>> {
    public static String KEY = "mining_shape";
    public static MiningShapeProperty property;
    public static Map<ResourceLocation, MapCodec<? extends MiningCondition>> miningConditionMap = new HashMap<>();
    public static Map<ResourceLocation, MapCodec<? extends MiningShape>> miningShapeMap = new HashMap<>();
    public static Map<ResourceLocation, MapCodec<? extends MiningMode>> miningModeMap = new HashMap<>();
    public static Map<ResourceLocation,MapCodec<? extends MiningModifier>> miningModifierMap = new HashMap<>();

    @Override
    public List<MiningShapeEntry> merge(List<MiningShapeEntry> left, List<MiningShapeEntry> right, MergeType mergeType) {
        return List.of();
    }


    public MiningShapeProperty() {
        super(Codec.list(MiningShapeEntry.CODEC));
        property = this;
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (!level.isClientSide() && !player.isShiftKeyDown()) {
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
