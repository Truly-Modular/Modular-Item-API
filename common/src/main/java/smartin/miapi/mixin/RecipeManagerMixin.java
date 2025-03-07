package smartin.miapi.mixin;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.material.generated.SmithingRecipeUtil;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "apply(Ljava/lang/Object;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    public void miapi$captureRecipeManager(Object object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        RecipeManager stack = (RecipeManager) (Object) this;
        SmithingRecipeUtil.manager = stack;
        //SmithingRecipeUtil.setupSmithingRecipes(false, Miapi.registryAccess, stack);
    }
}
