package smartin.miapi.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import smartin.miapi.loot.LootHelper;

public class LootTableCondition implements LootItemCondition {
    public static MapCodec<LootTableCondition> CONDITION = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    net.minecraft.resources.ResourceLocation.CODEC.fieldOf("loot_table_id").forGetter(l -> l.id)
            ).apply(instance, (l) -> {
                LootTableCondition condition = new LootTableCondition();
                condition.id = l;
                return condition;
            }));
    public static LootItemConditionType TYPE = new LootItemConditionType(CONDITION);

    public ResourceLocation id;

    @Override
    public LootItemConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.hasParam(LootHelper.LOOT_TABLE_PARAM)) {
            return lootContext.getParam(LootHelper.LOOT_TABLE_PARAM).equals(id);
        }
        return false;
    }
}
