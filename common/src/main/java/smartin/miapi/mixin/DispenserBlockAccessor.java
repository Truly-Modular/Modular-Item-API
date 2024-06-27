package smartin.miapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;

@Mixin(DispenserBlock.class)
public interface DispenserBlockAccessor {

    @Accessor("BEHAVIORS")
    static Map<Item, DispenseItemBehavior> getBehaviours() {
        throw new AssertionError();
    }
}
