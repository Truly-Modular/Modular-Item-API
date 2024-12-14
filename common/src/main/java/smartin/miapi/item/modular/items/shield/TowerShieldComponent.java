package smartin.miapi.item.modular.items.shield;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.Objects;

public class TowerShieldComponent {
    public static Codec<TowerShieldComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.intRange(0, 255)
                            .fieldOf("blocks")
                            .forGetter(component -> component.blockCount),
                    Codec.LONG
                            .fieldOf("last_active_tick")
                            .forGetter(component -> component.lastTickActive)
            ).apply(instance, (blocks, tick) -> {
                        var component = new TowerShieldComponent(0);
                        component.blockCount = blocks;
                        return component;
                    }
            ));
    public static DataComponentType<TowerShieldComponent> TOWER_SHIELD_COMPONENT =
            DataComponentType.<TowerShieldComponent>builder().persistent(CODEC).networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();

    public TowerShieldComponent(long lastTickActive) {
        this.lastTickActive = lastTickActive;
    }

    public void update(long lastTickActiveTick, int cooldown) {
        long tick = lastTickActive - lastTickActiveTick;
        int countReduction = (int) Math.floor((double) (int) tick / cooldown);
        blockCount = Math.max(0, blockCount - countReduction);
    }

    public int blockCount;
    public long lastTickActive;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check for reference equality
        if (o == null || getClass() != o.getClass()) return false; // Check for null or class mismatch
        TowerShieldComponent that = (TowerShieldComponent) o; // Cast to TowerShieldComponent
        return blockCount == that.blockCount && lastTickActive == that.lastTickActive; // Compare fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockCount, lastTickActive); // Compute hash using both fields
    }


}
