package smartin.miapi.modules.properties.attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlotGroup;

import java.util.Objects;
import java.util.Optional;

public class EquipmentSlotGroupWrapper {

    public final String raw;
    public final Optional<EquipmentSlotGroup> group;

    public EquipmentSlotGroupWrapper(String raw) {
        this.raw = raw;
        this.group = EquipmentSlotGroup.CODEC.parse(
                com.mojang.serialization.JsonOps.INSTANCE,
                new com.google.gson.JsonPrimitive(raw)
        ).result();
    }

    public EquipmentSlotGroupWrapper(EquipmentSlotGroup group) {
        this.raw = group.getSerializedName();
        this.group = Optional.of(group);
    }

    public EquipmentSlotGroupWrapper(String raw, Optional<EquipmentSlotGroup> group) {
        this.raw = raw;
        this.group = group;
    }

    /* -------------------------------- Codec -------------------------------- */

    public static final Codec<EquipmentSlotGroupWrapper> CODEC =
            Codec.STRING.flatXmap(
                    str -> {
                        Optional<EquipmentSlotGroup> parsed =
                                EquipmentSlotGroup.CODEC.parse(
                                        com.mojang.serialization.JsonOps.INSTANCE,
                                        new com.google.gson.JsonPrimitive(str)
                                ).result();

                        return DataResult.success(new EquipmentSlotGroupWrapper(str, parsed));
                    },
                    wrapper -> DataResult.success(wrapper.raw)
            );

    /* ------------------------------- StreamCodec ------------------------------- */

    public static final StreamCodec<ByteBuf, EquipmentSlotGroupWrapper> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    w -> w.raw,
                    EquipmentSlotGroupWrapper::new
            );

    /* -------------------------------- Utility -------------------------------- */

    public Optional<EquipmentSlotGroup> group() {
        return group;
    }

    public String raw() {
        return raw;
    }

    public boolean isValid() {
        return group.isPresent();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EquipmentSlotGroupWrapper that)) return false;
        return Objects.equals(this.raw, that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }
}