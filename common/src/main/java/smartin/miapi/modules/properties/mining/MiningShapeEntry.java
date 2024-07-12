package smartin.miapi.modules.properties.mining;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.mining.condition.MiningCondition;
import smartin.miapi.modules.properties.mining.mode.MiningMode;
import smartin.miapi.modules.properties.mining.modifier.MiningModifier;
import smartin.miapi.modules.properties.mining.shape.MiningShape;

import java.util.List;

public record MiningShapeEntry(MiningCondition condition, MiningShape shape, MiningMode mode,
                               List<? extends MiningModifier> modifiers) {
    public static Codec<MiningShapeEntry> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<MiningShapeEntry, T>> decode(DynamicOps<T> ops, T input) {
            T conditionPart = ops.getMap(input).getOrThrow().get("condition");
            ResourceLocation conditionLocation = Miapi.ID_CODEC.decode(ops, ops.getMap(conditionPart).getOrThrow().get("type"))
                    .getOrThrow(s -> new RuntimeException("could not find condition in miningProperty")).getFirst();
            Codec<? extends MiningCondition> miningConditionCodec = MiningShapeProperty.miningConditionMap.get(conditionLocation);
            MiningCondition miningCondition = miningConditionCodec.decode(ops, conditionPart)
                    .getOrThrow().getFirst();

            T collapseMode = ops.getMap(input).getOrThrow().get("collapse_mode");
            ResourceLocation collapseLocation = Miapi.ID_CODEC.decode(ops, ops.getMap(collapseMode).getOrThrow().get("type"))
                    .getOrThrow(s -> new RuntimeException("could not find collapse_mode in miningProperty")).getFirst();
            Codec<? extends MiningMode> modeCodec = MiningShapeProperty.miningModeMap.get(collapseLocation);
            MiningMode mode = modeCodec.decode(ops, conditionPart)
                    .getOrThrow().getFirst();

            T shapePart = ops.getMap(input).getOrThrow().get("shape");
            ResourceLocation location = Miapi.ID_CODEC.decode(ops, ops.getMap(shapePart).getOrThrow(s -> new RuntimeException("could decode shape id")).get("type"))
                    .getOrThrow(s -> new RuntimeException("could not find shape in miningProperty")).getFirst();
            Codec<? extends MiningShape> shapeCodec = MiningShapeProperty.miningShapeMap.get(location);
            MiningShape shape = shapeCodec.decode(ops, conditionPart)
                    .getOrThrow().getFirst();

            T modifierPart = ops.getMap(input).getOrThrow().get("modifiers");
            List<? extends MiningModifier> modifiers =
                    Codec.dispatchedMap(Miapi.ID_CODEC, (id) -> MiningShapeProperty.miningModifierMap.get(id))
                            .decode(ops, modifierPart).getOrThrow().getFirst().values().stream().toList();
            return DataResult.success(new Pair<>(new MiningShapeEntry(miningCondition, shape, mode, modifiers), input));
        }

        @Override
        public <T> DataResult<T> encode(MiningShapeEntry input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "encoding condition is not fully supported");
        }
    };

    public void execute(BlockPos pos, Level level, ItemStack stack, ServerPlayer player, Direction facing) {
        List<BlockPos> posList = condition().trimList(level, pos, shape().getMiningBlocks(level, pos, facing));
        for (MiningModifier modifier : modifiers) {
            posList = modifier.adjustMiningBlock(level, pos, player, stack, posList);
        }
        mode().execute(posList, level, player, pos, stack);
    }
}
