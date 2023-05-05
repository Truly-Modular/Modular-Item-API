package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;

@Environment(EnvType.CLIENT)
public class CustomColorProvider implements ItemColorProvider {

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if(stack.getItem() instanceof ModularItem)
            return tintIndex;
        return -1;
    }
}
