package smartin.miapi.modules.abilities.shield;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.InitializeAble;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

public class BlockData implements MergeAble<BlockData>, InitializeAble<BlockData> {
    public static MapCodec<BlockData> CODEC = AutoCodec.of(BlockData.class);
    @CodecBehavior.Optional
    public ResourceLocation pose = Miapi.id(Miapi.MOD_ID, "medium_shield_block");

    @CodecBehavior.Optional
    public ResourceLocation sound = Miapi.id("minecraft:block.iron_trapdoor.close");

    @CodecBehavior.Optional
    public DoubleOperationResolvable pitch = new DoubleOperationResolvable(1.0);

    @CodecBehavior.Optional
    public DoubleOperationResolvable volume = new DoubleOperationResolvable(1.0);

    @CodecBehavior.Optional
    @AutoCodec.Name("blocking")
    public DoubleOperationResolvable blocking = new DoubleOperationResolvable(0.0);

    @CodecBehavior.Optional
    @AutoCodec.Name("damage_return_percent")
    public DoubleOperationResolvable damageReturnPercent = new DoubleOperationResolvable(0.0);

    @CodecBehavior.Optional
    @AutoCodec.Name("cooldown_attacker_weapon")
    public DoubleOperationResolvable cooldownAttackerWeapon = new DoubleOperationResolvable(0);

    @CodecBehavior.Optional
    @AutoCodec.Name("cooldown_miss_time")
    public DoubleOperationResolvable cooldownMissTime = new DoubleOperationResolvable(40);

    @Override
    public BlockData merge(BlockData left, BlockData right, MergeType mergeType) {
        BlockData merged = new BlockData();
        merged.pose = MergeAble.decideLeftRight(left.pose, right.pose, mergeType);
        merged.sound = MergeAble.decideLeftRight(left.sound, right.sound, mergeType);
        merged.pitch = DoubleOperationResolvable.merge(left.pitch, right.pitch, mergeType);
        merged.volume = DoubleOperationResolvable.merge(left.volume, right.volume, mergeType);
        merged.damageReturnPercent = DoubleOperationResolvable.merge(left.damageReturnPercent, right.damageReturnPercent, mergeType);
        merged.cooldownAttackerWeapon = DoubleOperationResolvable.merge(left.cooldownAttackerWeapon, right.cooldownAttackerWeapon, mergeType);
        merged.cooldownMissTime = DoubleOperationResolvable.merge(left.cooldownMissTime, right.cooldownMissTime, mergeType);
        merged.blocking = DoubleOperationResolvable.merge(left.blocking, right.blocking, mergeType);
        return merged;
    }

    @Override
    public BlockData initialize(BlockData data, ModuleInstance moduleInstance) {
        BlockData initialized = new BlockData();
        initialized.pose = data.pose;
        initialized.sound = data.sound;
        initialized.pitch = data.pitch.initialize(moduleInstance);
        initialized.volume = data.volume.initialize(moduleInstance);
        initialized.damageReturnPercent = data.damageReturnPercent.initialize(moduleInstance);
        initialized.cooldownAttackerWeapon = data.cooldownAttackerWeapon.initialize(moduleInstance);
        initialized.cooldownMissTime = data.cooldownMissTime.initialize(moduleInstance);
        initialized.blocking = data.blocking.initialize(moduleInstance);
        return initialized;
    }
}
