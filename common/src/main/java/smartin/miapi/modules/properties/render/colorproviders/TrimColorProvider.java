package smartin.miapi.modules.properties.render.colorproviders;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.events.ClientEvents;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.material.palette.SpriteColorer;
import smartin.miapi.material.palette.SpriteOverlayer;

public abstract class TrimColorProvider implements ColorProvider {
    public TrimRenderer.TrimMode mode;
    public boolean useTrim;
    private SpriteOverlayer trimOverlayer = null;

    public TrimColorProvider(TrimRenderer.TrimMode mode) {
        this.mode = mode;
        useTrim = ColorProvider.shouldHaveTrim(mode) || true;
    }


    protected MaterialRenderController getTrimController(MaterialRenderController controller, Material material, ItemStack stack, ItemDisplayContext mode) {
        if (useTrim && controller instanceof SpriteColorer spriteColorer) {
            if (trimOverlayer == null) {
                trimOverlayer = ColorProvider.getTrimController(material, this.mode, stack, spriteColorer, mode);
            }
            if (trimOverlayer != null) {
                trimOverlayer.delegate = spriteColorer;
                ClientEvents.MutableValue<SpriteColorer> colorer = new ClientEvents.MutableValue<>(trimOverlayer);
                ClientEvents.RESOLVE_COLOR_PROVIDER.invoker().adjustColorProvider(colorer, material, stack, mode);
                return trimOverlayer;
            } else {
                useTrim = false;
            }
            ClientEvents.MutableValue<SpriteColorer> colorer = new ClientEvents.MutableValue<>(spriteColorer);
            ClientEvents.RESOLVE_COLOR_PROVIDER.invoker().adjustColorProvider(colorer, material, stack, mode);
            return colorer.get();
        }
        return controller;
    }
}
