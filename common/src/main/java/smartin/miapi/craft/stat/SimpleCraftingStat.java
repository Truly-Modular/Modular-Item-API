package smartin.miapi.craft.stat;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;
import java.util.Map;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * An implementation of {@link CraftingStat} with a double as its stat instance.
 */
public class SimpleCraftingStat implements CraftingStat<Double> {
    private final double defaultVal;

    public SimpleCraftingStat(double defaultVal) {
        this.defaultVal = defaultVal;
    }

    @Override
    public JsonElement saveToJson(Double instance) {
        return new JsonPrimitive(instance);
    }

    @Override
    public Double createFromJson(JsonElement json, ModuleInstance instance) {
        return StatResolver.resolveDouble(json, instance);
    }

    @Override
    public Tag saveToNbt(Double instance) {
        return DoubleTag.valueOf(instance);
    }

    @Override
    public Double createFromNbt(Tag nbt) {
        return ((DoubleTag) nbt).getAsDouble();
    }

    @Override
    public boolean canCraft(
            Double instance,
            Double expected,
            ItemStack old,
            ItemStack crafting,
            @Nullable ModularWorkBenchEntity bench,
            Player player,
            ModuleInstance newModule,
            ItemModule module,
            List<ItemStack> inventory,
            Map<String,String> data ) {
        return instance >= expected;
    }

    @Override
    public Double getDefault() {
        return defaultVal;
    }

    @Override
    public Component asText(Double instance) {
        return Component.literal(String.valueOf(instance));
    }

    @Override
    public Double getBetter(Double first, Double second) {
        return Math.max(first, second);
    }

    @Override
    public Double multiply(Double first, Double second) {
        return first+second;
    }

    @Override
    public Double add(Double first, Double second) {
        return first*second;
    }
}
