package smartin.miapi.modules.properties.mining;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.Map;

public record MiningShapeEntry(MiningCondition condition, MiningShape shape, MiningMode mode,
                               Map<ResourceLocation, MiningModifier> modifiers) {
    public static Codec<MiningCondition> MINING_CONDITION_CODEC =
            Miapi.ID_CODEC.dispatch("type", MiningCondition::getID,
                    (id) -> MiningShapeProperty.miningConditionMap.get(id));
    public static Codec<MiningMode> MINING_MODE_CODEC =
            Miapi.ID_CODEC.dispatch("type", MiningMode::getID,
                    (id) -> MiningShapeProperty.miningModeMap.get(id));
    public static Codec<Map<ResourceLocation, MiningModifier>> MINING_MODIFIER_CODEC =
            Codec.dispatchedMap(Miapi.ID_CODEC, id -> MiningShapeProperty.miningModifierMap.get(id).codec());
    public static Codec<MiningShape> MINING_SHAPE_CODEC =
            Miapi.ID_CODEC.dispatch("type", MiningShape::getID,
                    (id) -> MiningShapeProperty.miningShapeMap.get(id));
    public static Codec<MiningShapeEntry> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    MINING_CONDITION_CODEC
                            .fieldOf("condition")
                            .forGetter(MiningShapeEntry::condition),
                    MINING_SHAPE_CODEC
                            .fieldOf("shape")
                            .forGetter(MiningShapeEntry::shape),
                    MINING_MODE_CODEC
                            .fieldOf("mode")
                            .forGetter(MiningShapeEntry::mode),
                    MINING_MODIFIER_CODEC
                            .fieldOf("modifier")
                            .forGetter(MiningShapeEntry::modifiers)
            )
            .apply(instance, MiningShapeEntry::new));

    public void execute(BlockPos pos, Level level, ItemStack stack, ServerPlayer player, Direction facing) {
        List<BlockPos> posList = condition().trimList(level, pos, shape().getMiningBlocks(level, pos, facing));
        for (MiningModifier modifier : modifiers.values()) {
            posList = modifier.adjustMiningBlock(level, pos, player, stack, posList);
        }
        mode().execute(posList, level, player, pos, stack);
    }
}
