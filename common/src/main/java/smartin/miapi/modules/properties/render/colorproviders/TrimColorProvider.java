package smartin.miapi.modules.properties.render.colorproviders;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.material.palette.SpriteColorer;
import smartin.miapi.material.palette.SpriteOverlayer;

public abstract class TrimColorProvider implements ColorProvider {
    public TrimRenderer.TrimMode mode;
    public final boolean useTrim;
    private SpriteOverlayer trimOverlayer = null;

    public TrimColorProvider(TrimRenderer.TrimMode mode){
        this.mode = mode;
        useTrim = ColorProvider.isUseTrim(mode) || true;
    }


    protected MaterialRenderController getTrimController(MaterialRenderController controller,Material material,ItemStack stack) {
        if (useTrim && controller instanceof SpriteColorer spriteColorer) {
            if (trimOverlayer == null) {
                trimOverlayer = ColorProvider.getTrimmControllor(material, this.mode, stack, spriteColorer);
            }
            if (trimOverlayer != null) {
                trimOverlayer.delegate = spriteColorer;
                return trimOverlayer;
            }
        }
        return controller;
    }
}
