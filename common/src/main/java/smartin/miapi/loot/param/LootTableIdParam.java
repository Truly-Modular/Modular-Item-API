package smartin.miapi.loot.param;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class LootTableIdParam extends LootContextParam<ResourceLocation> {
    public LootTableIdParam(ResourceLocation name) {
        super(name);
    }
}
