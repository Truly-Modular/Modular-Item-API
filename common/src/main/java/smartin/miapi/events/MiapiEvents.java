package smartin.miapi.events;

import com.google.common.collect.Multimap;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.material.Material;
import smartin.miapi.material.generated.GeneratedMaterial;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiapiEvents {
    /**
     * make sure {@link ModularAttackEvents} arent what you are looking for.
     * Generally any kind of modular onhit effect should be implemented in {@link ModularAttackEvents}
     * While Attribute based stuff should be implemented using these, since they are called regardless of weapon
     */
    public static final PrioritizedEvent<LivingHurt> LIVING_HURT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<LivingHurt> LIVING_HURT_AFTER = PrioritizedEvent.createEventResult();


    public static final PrioritizedEvent<LivingEntityXpAdjust> ADJUST_DROP_XP = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<LivingHurt> LIVING_HURT_AFTER_ARMOR = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<EntityRide> START_RIDING = PrioritizedEvent.createLoop(); // only fires on successful rides, and is not cancellable (if I wanted to make it cancellable, i would add mixinextras)
    public static final PrioritizedEvent<EntityRide> STOP_RIDING = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<StatUpdateEvent> STAT_UPDATE_EVENT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ItemConvertEvent> CONVERT_ITEM = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<CreateMaterialModularConvertersEvent> GENERATE_MATERIAL_CONVERTERS = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<PlayerTickEvent> PLAYER_TICK_START = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<PlayerTickEvent> PLAYER_TICK_END = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<LivingEntityTickEvent> LIVING_ENTITY_TICK_END = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<MaterialCraftEvent> MATERIAL_CRAFT_EVENT = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<SmithingEvent> SMITHING_EVENT = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<LivingEntityAttributeBuild> LIVING_ENTITY_ATTRIBUTE_BUILD_EVENT = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<PlayerEquip> PLAYER_EQUIP_EVENT = PrioritizedEvent.createLoop();

    static {
        MiapiEvents.SMITHING_EVENT.register((listener) -> {
            ComponentApplyProperty.updateItemStack(listener.itemStack, listener.registryAccess);
            return EventResult.pass();
        });
    }

    public interface ReloadEvent {
        EventResult onReload(boolean isClient);
    }

    public interface PlayerEquip {
        EventResult equip(Player player, Map<EquipmentSlot, ItemStack> changes);
    }

    public static class LivingHurtEvent {
        public final LivingEntity livingEntity;
        public DamageSource damageSource;
        public float amount;
        public boolean isCritical = false;

        public LivingHurtEvent(LivingEntity livingEntity, DamageSource damageSource, float amount) {
            this.livingEntity = livingEntity;
            this.damageSource = damageSource;
            this.amount = amount;

        }

        public static ItemStack getCausingItemStack(DamageSource damageSource) {
            if (damageSource.getDirectEntity() instanceof Projectile projectile && (projectile instanceof ItemProjectileEntity itemProjectile)) {
                return itemProjectile.getPickupItem();

            }
            if (damageSource.getEntity() instanceof LivingEntity attacker) {
                return attacker.getMainHandItem();
            }
            return ItemStack.EMPTY;
        }

        public ItemStack getCausingItemStack() {
            return getCausingItemStack(this.damageSource);
        }

        public Iterable<ItemStack> getCausingItemStackAndArmorOfAttacker() {
            return getCausingItemStackAndArmorOfAttacker(damageSource);
        }

        public static Iterable<ItemStack> getCausingItemStackAndArmorOfAttacker(DamageSource damageSource) {
            List<ItemStack> itemStacks = new ArrayList<>();
            if (damageSource.getDirectEntity() instanceof Projectile projectile && (projectile instanceof ItemProjectileEntity itemProjectile)) {
                itemStacks.add(itemProjectile.getPickupItem());
                if (itemProjectile.getOwner() instanceof LivingEntity attacker) {
                    attacker.getArmorSlots().forEach(itemStacks::add);
                }

            }
            if (damageSource.getEntity() instanceof LivingEntity attacker) {
                attacker.getArmorSlots().forEach(itemStacks::add);
            }
            return itemStacks;
        }
    }

    public static class ItemStackAttributeEventHolder {
        public ItemStack itemStack;
        public EquipmentSlot equipmentSlot;
        public Multimap<Attribute, AttributeModifier> attributeModifiers;

        public ItemStackAttributeEventHolder(ItemStack itemStack, EquipmentSlot equipmentSlot, Multimap<Attribute, AttributeModifier> attributeModifiers) {
            this.itemStack = itemStack;
            this.equipmentSlot = equipmentSlot;
            this.attributeModifiers = attributeModifiers;
        }

    }

    public interface ItemStackAttributeEvent {
        EventResult adjust(ItemStackAttributeEventHolder info);
    }

    public interface LivingAttackEvent {
        EventResult attack(@Nullable LivingEntity attacker, @Nullable LivingEntity defender);
    }

    public interface LivingEntityXpAdjust {
        EventResult death(LivingEntity livingEntity, MutableFloat xp);

    }

    public interface LivingEntityAttributeBuild {
        EventResult build(AttributeSupplier.Builder builder);

    }

    public interface ItemConvertEvent {
        EventResult convert(ItemStack old, Mutable<ItemStack> converting);
    }

    public static class MaterialCraftEventData {
        public ItemStack crafted;
        public final ItemStack materialStack;
        public Material material;
        public ModuleInstance moduleInstance;
        CraftAction action;

        public MaterialCraftEventData(ItemStack crafted,
                                      ItemStack materialStack,
                                      Material material,
                                      ModuleInstance moduleInstance,
                                      CraftAction action) {
            this.crafted = crafted;
            this.material = material;
            this.materialStack = materialStack;
            this.moduleInstance = moduleInstance;
            this.action = action;
        }
    }

    public interface MaterialCraftEvent {
        EventResult craft(MaterialCraftEventData data);
    }

    public static class MaterialCraft {
        public RegistryAccess registryAccess;
        public ItemStack itemStack;

        public MaterialCraft(ItemStack itemStack) {
            this.itemStack = itemStack;
        }
    }

    public interface PlayerTickEvent {
        EventResult tick(Player player);
    }

    public interface LivingEntityTickEvent {
        EventResult tick(LivingEntity entity);
    }

    public interface GeneratedMaterialEvent {
        EventResult generated(GeneratedMaterial material, ItemStack mainIngredient, List<Item> tools, boolean isClient);
    }

    public interface CreateMaterialModularConvertersEvent {
        EventResult generated(Material material, List<TieredItem> tools, boolean isClient);
    }

    public interface SmithingEvent {
        EventResult craft(MaterialCraft itemStack);
    }

    public interface BlockBreakEvent {
        void breakBlock(ServerLevel world, BlockPos pos, ItemStack tool, IntProvider experience);
    }

    public interface LivingHurt {
        EventResult hurt(LivingHurtEvent event);
    }

    public interface BlockCraftingStatUpdate {
        EventResult call(ModularWorkBenchEntity bench, @Nullable Player player);
    }

    public interface ItemCraftingStatUpdate {
        EventResult call(ModularWorkBenchEntity bench, Iterable<ItemStack> inventory, @Nullable Player player);
    }

    public interface EntityRide {
        void ride(Entity passenger, Entity vehicle);
    }

    public interface StatUpdateEvent {
        EventResult update(ModularWorkBenchEntity blockEntity, StatProvidersMap map, int syncId, Inventory playerInventory, Player player, CraftingScreenHandler handler);
    }
}
