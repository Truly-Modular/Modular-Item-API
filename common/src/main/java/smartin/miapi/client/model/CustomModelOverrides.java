package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.render.ModelProperty;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class CustomModelOverrides extends ModelOverrideList {

    public CustomModelOverrides() {
        super(DynamicBakery.dynamicBaker, null, new ArrayList<>());
    }

    @Override
    public BakedModel apply(BakedModel oldmodel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
        BakedModel model = ModelProperty.getItemModel(stack);
        if (model != null) {
            ModelOverrideList modelOverride = model.getOverrides();
            if (modelOverride != null) {
                return modelOverride.apply(model, stack, world, entity, seed);
            }
            return model;
        }
        return oldmodel;
    }
}
