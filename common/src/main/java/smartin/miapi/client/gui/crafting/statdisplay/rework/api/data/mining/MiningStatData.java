package smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.mining;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.statdisplay.rework.StatGroups;
import smartin.miapi.client.gui.crafting.statdisplay.rework.StatTypes;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.Stat;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.StatMetaData;
import smartin.miapi.material.generated.TierManager;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.HashMap;
import java.util.List;

public record MiningStatData(
        String type
) implements StatMetaData<MiningStat> {

    @Override
    public MiningStat getData(ItemStack stack) {
        MiningLevelProperty.MiningRule rule = MiningLevelProperty.property
                .getData(stack)
                .orElse(new HashMap<>())
                .get(type);

        if (rule == null) {
            return null;
        }

        return new MiningStat(
                type,
                rule.speed().getValue(),
                rule.speed(),
                List.copyOf(rule.respectMaterialBlacklists()),
                material -> TierManager
                        .getPreferredPickaxe(material.getIncorrectBlocksForDrops())
                        .map(pickaxe -> List.of(pickaxe.getDefaultInstance()))
                        .orElse(List.of())
        );
    }

    public static @NotNull Stat<MiningStat, MiningStatData>
    getMiningStat(String type) {
        ResourceLocation statId = Miapi.id("mining." + type);
        Stat<MiningStat, MiningStatData> stat = new Stat<MiningStat, MiningStatData>(statId,
                (item, data, meta) -> Component.translatable("miapi.stat.miapi.mining.level." + type),
                (item, data, meta) -> Component.translatable("miapi.stat.miapi.mining.level." + type + ".description"),
                StatTypes.PICKAXE_MINING,
                StatGroups.MINING_GROUP,
                new MiningStatData(type)
        );
        Stat.STAT_REGISTRY.register(statId, stat);
        return stat;
    }
}