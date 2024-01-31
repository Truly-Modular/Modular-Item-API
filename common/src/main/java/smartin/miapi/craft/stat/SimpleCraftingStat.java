package smartin.miapi.craft.stat;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;
import java.util.Map;

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
    public NbtElement saveToNbt(Double instance) {
        return NbtDouble.of(instance);
    }

    @Override
    public Double createFromNbt(NbtElement nbt) {
        return ((NbtDouble) nbt).doubleValue();
    }

    @Override
    public boolean canCraft(
            Double instance,
            Double expected,
            ItemStack old,
            ItemStack crafting,
            @Nullable ModularWorkBenchEntity bench,
            PlayerEntity player,
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
    public Text asText(Double instance) {
        return Text.literal(String.valueOf(instance));
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
