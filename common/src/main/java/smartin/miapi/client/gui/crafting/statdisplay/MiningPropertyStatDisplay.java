package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MiningPropertyStatDisplay extends SingleStatDisplayDouble {
    public final String type;

    public MiningPropertyStatDisplay(String type) {
        super(0, 0, 51, 19, (
                        stack -> Component.translatable("miapi.stat.miapi.mining.level." + type)),
                s -> Component.translatable("miapi.stat.miapi.mining.level." + type + ".description"));
        this.type = type;
        this.maxValue = 15;
    }

    @Override
    public double getValue(ItemStack stack) {
        MiningLevelProperty.MiningRule rule = MiningLevelProperty.property.getData(stack).orElse(new HashMap<>()).get(type);
        if (rule != null) {
            return rule.speed().getValue();
        }
        return 0.0;
    }

    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        return MiningLevelProperty.property.getData(original).orElse(new HashMap<>()).containsKey(type) &&
               !(MiningLevelProperty.property.getData(original).orElse(new HashMap<>()).get(type).speed().getValue() == 1 ||
                 MiningLevelProperty.property.getData(original).orElse(new HashMap<>()).get(type).speed().getValue() == 0) ||
               MiningLevelProperty.property.getData(compareTo).orElse(new HashMap<>()).containsKey(type) &&
               !(MiningLevelProperty.property.getData(compareTo).orElse(new HashMap<>()).get(type).speed().getValue() == 1 ||
                 MiningLevelProperty.property.getData(compareTo).orElse(new HashMap<>()).get(type).speed().getValue() == 0);
    }

    public DoubleOperationResolvable getResolvable(ItemStack stack) {
        var rule = MiningLevelProperty.property.getData(compareTo == null ? original : compareTo).orElse(new HashMap<>()).get(type);
        if (rule != null) {
            return rule.speed();
        }
        return null;
    }

    public List<Component> additionalHoverLines() {
        List<Component> lines = new ArrayList<>();
        var rule = MiningLevelProperty.property.getData(compareTo == null ? original : compareTo).orElse(new HashMap<>()).get(type);
        if (rule != null) {
            rule.respectMaterialBlacklists().stream().distinct().forEach(m -> {
                lines.add(
                        Component.translatable(
                                        "miapi.stat.miapi.mining.level.material.source",
                                        m.getTranslation())
                                .withStyle(ChatFormatting.GRAY));

                m.getMiningLevelToolTip().forEach(c -> {
                    lines.add(LoreProperty.format(c,ChatFormatting.DARK_GRAY));
                });
            });
        }
        return lines;
    }
}
