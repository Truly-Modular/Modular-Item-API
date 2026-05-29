package smartin.miapi.mixin;

import com.redpxnda.nucleus.facet.entity.CodecEntityFacet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CodecEntityFacet.class)
public class CodecEntityFacetMixin {

    /*
    @Inject(
            method = "Lcom/redpxnda/nucleus/facet/entity/CodecEntityFacet;loadNbt(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("HEAD"),
            remap = true,
            cancellable = true
    )
    private void fixAp(CompoundTag nbt, CallbackInfo ci) {
        if(!nbt.contains("data")){
            ci.cancel();
        }
    }

     */
}
