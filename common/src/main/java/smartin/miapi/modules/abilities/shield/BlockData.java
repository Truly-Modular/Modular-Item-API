package smartin.miapi.modules.abilities.shield;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.InitializeAble;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

public record BlockData(
        ResourceLocation pose,
        ResourceLocation sound,
        DoubleOperationResolvable pitch,
        DoubleOperationResolvable volume,
        DoubleOperationResolvable blocking,
        DoubleOperationResolvable damageReturnPercent,
        DoubleOperationResolvable cooldownAttackerWeapon,
        DoubleOperationResolvable cooldownMissTime,
        DoubleOperationResolvable angle
) implements MergeAble<BlockData>, InitializeAble<BlockData> {

    public static final BlockData DEFAULT = new BlockData(
            Miapi.id(Miapi.MOD_ID, "medium_shield_block"),
            Miapi.id("minecraft:block.iron_trapdoor.close"),
            new DoubleOperationResolvable(1.0),
            new DoubleOperationResolvable(1.0),
            new DoubleOperationResolvable(0.0),
            new DoubleOperationResolvable(0.0),
            new DoubleOperationResolvable(0),
            new DoubleOperationResolvable(40),
            new DoubleOperationResolvable(45)
    );

    public static final MapCodec<BlockData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("pose", DEFAULT.pose()).forGetter(BlockData::pose),
            ResourceLocation.CODEC.optionalFieldOf("sound", DEFAULT.sound()).forGetter(BlockData::sound),
            DoubleOperationResolvable.CODEC.optionalFieldOf("pitch", DEFAULT.pitch()).forGetter(BlockData::pitch),
            DoubleOperationResolvable.CODEC.optionalFieldOf("volume", DEFAULT.volume()).forGetter(BlockData::volume),
            DoubleOperationResolvable.CODEC.optionalFieldOf("blocking", DEFAULT.blocking()).forGetter(BlockData::blocking),
            DoubleOperationResolvable.CODEC.optionalFieldOf("damage_return_percent", DEFAULT.damageReturnPercent()).forGetter(BlockData::damageReturnPercent),
            DoubleOperationResolvable.CODEC.optionalFieldOf("cooldown_attacker_weapon", DEFAULT.cooldownAttackerWeapon()).forGetter(BlockData::cooldownAttackerWeapon),
            DoubleOperationResolvable.CODEC.optionalFieldOf("cooldown_miss_time", DEFAULT.cooldownMissTime()).forGetter(BlockData::cooldownMissTime),
            DoubleOperationResolvable.CODEC.optionalFieldOf("angle", DEFAULT.angle()).forGetter(BlockData::angle)
    ).apply(instance, BlockData::new));

    @Override
    public BlockData merge(BlockData left, BlockData right, MergeType mergeType) {
        return new BlockData(
                MergeAble.decideLeftRight(left.pose, right.pose, mergeType),
                MergeAble.decideLeftRight(left.sound, right.sound, mergeType),
                DoubleOperationResolvable.merge(left.pitch, right.pitch, mergeType),
                DoubleOperationResolvable.merge(left.volume, right.volume, mergeType),
                DoubleOperationResolvable.merge(left.blocking, right.blocking, mergeType),
                DoubleOperationResolvable.merge(left.damageReturnPercent, right.damageReturnPercent, mergeType),
                DoubleOperationResolvable.merge(left.cooldownAttackerWeapon, right.cooldownAttackerWeapon, mergeType),
                DoubleOperationResolvable.merge(left.cooldownMissTime, right.cooldownMissTime, mergeType),
                DoubleOperationResolvable.merge(left.angle, right.angle, mergeType)
        );
    }

    @Override
    public BlockData initialize(BlockData data, ModuleInstance moduleInstance) {
        return new BlockData(
                data.pose,
                data.sound,
                data.pitch.initialize(moduleInstance),
                data.volume.initialize(moduleInstance),
                data.blocking.initialize(moduleInstance),
                data.damageReturnPercent.initialize(moduleInstance),
                data.cooldownAttackerWeapon.initialize(moduleInstance),
                data.cooldownMissTime.initialize(moduleInstance),
                data.angle.initialize(moduleInstance)
        );
    }
}
