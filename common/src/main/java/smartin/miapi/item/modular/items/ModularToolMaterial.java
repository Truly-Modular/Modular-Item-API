package smartin.miapi.item.modular.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.lwjgl.system.NonnullDefault;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.DurabilityProperty;
import smartin.miapi.modules.properties.RepairPriority;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.concurrent.atomic.AtomicReference;

@NonnullDefault
public class ModularToolMaterial implements Tier {
    public static ModularToolMaterial toolMaterial = new ModularToolMaterial();

    @Override
    public int getUses() {
        return 50;
    }

    @Override
    public float getSpeed() {
        return 5;
    }

    @Override
    public float getAttackDamageBonus() {
        return 5;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    public static Tier forItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty() || VisualModularItem.isVisualModularItem(itemStack)) {
            return new ModularToolMaterial();
        }
        AtomicReference<TagKey<Block>> getIncorrectBlocksForDrops = new AtomicReference<>(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
        double maxSpeed = MiningLevelProperty.property.getData(itemStack).map(data -> {
            double max = 0;
            for (MiningLevelProperty.MiningRule rule : data.values()) {
                double current = rule.speed().getValue();
                max = Math.max(
                        max, current);
                if (rule.useMaterial()) {
                    if (current == max) {
                        rule.respectMaterialBlacklists().stream().findAny().ifPresent(m -> {
                            getIncorrectBlocksForDrops.set(m.getIncorrectBlocksForDrops());
                        });
                    }
                }
            }
            return max;
        }).orElse(0.0);

        return new Tier() {

            @Override
            public int getUses() {
                return DurabilityProperty.property.getValue(itemStack).orElse(50.0).intValue();
            }

            @Override
            public float getSpeed() {
                return (float) maxSpeed;
            }

            @Override
            public float getAttackDamageBonus() {
                return (float) AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE.value());
            }

            @Override
            public TagKey<Block> getIncorrectBlocksForDrops() {
                return getIncorrectBlocksForDrops.get();
            }

            @Override
            public int getEnchantmentValue() {
                return (int) EnchantAbilityProperty.getEnchantAbility(itemStack);
            }

            @Override
            public Ingredient getRepairIngredient() {
                return RepairPriority.getRepairIngredient(itemStack);
            }
        };
    }
}
