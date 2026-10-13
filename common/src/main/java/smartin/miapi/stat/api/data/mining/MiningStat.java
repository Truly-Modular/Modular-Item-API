package smartin.miapi.stat.api.data.mining;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.List;
import java.util.function.Function;

public record MiningStat(
        String level,
        double speed,
        DoubleOperationResolvable resolvable,
        List<Material> materialSources,
        Function<Material, List<ItemStack>> exampleTools
) {

    public List<ItemStack> getExampleTools(Material material) {
        return exampleTools.apply(material);
    }
}
