package smartin.miapi.client.model;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.properties.render.ModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CustomModelOverrides extends ModelOverrideList {

    public CustomModelOverrides(ModelLoader modelLoader, JsonUnbakedModel parent, Function<Identifier, UnbakedModel> unbakedModelGetter, List<ModelOverride> overrides) {
        super(modelLoader,parent,unbakedModelGetter,overrides);
    }
    public CustomModelOverrides(){
        super(null,null,null,new ArrayList<>());
    }

    @Override
    public BakedModel apply(BakedModel oldmodel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
        BakedModel model = ModelProperty.getModel(stack);
        if(model!=null){
            ModelOverrideList modelOverride = model.getOverrides();
            if(modelOverride!=null){
                BakedModel bakedModel = modelOverride.apply(model,stack,world,entity,seed);
                return bakedModel;
            }
            return model;
        }
        return oldmodel;
    }
}
