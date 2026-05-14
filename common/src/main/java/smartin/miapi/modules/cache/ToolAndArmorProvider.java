package smartin.miapi.modules.cache;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ToolMaterial;

public interface ToolAndArmorProvider {

    ToolMaterial getToolMaterial();

    ArmorMaterial getArmorMaterial();

    void clearMiapiCaches();
}
