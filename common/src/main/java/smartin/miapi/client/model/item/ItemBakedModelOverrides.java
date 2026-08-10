package smartin.miapi.client.model.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.model.DynamicBakery;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class ItemBakedModelOverrides extends ItemOverrides {

    public ItemBakedModelOverrides() {
        super(DynamicBakery.dynamicBaker, null, new ArrayList<>());
    }

    @Override
    public BakedModel resolve(BakedModel oldmodel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
        return oldmodel;
    }
}