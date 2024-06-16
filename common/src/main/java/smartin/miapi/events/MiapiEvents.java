package smartin.miapi.events;

import com.google.common.collect.Multimap;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.GeneratedMaterial;
import smartin.miapi.modules.material.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiapiEvents {
    public static final PrioritizedEvent<LivingAttackEvent> LIVING_ATTACK = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<LivingHurt> LIVING_HURT = PrioritizedEvent.createEventResult();
    /**
     * the value in this Event reflects the Critical Multiplier instead
     */
    public static final PrioritizedEvent<LivingHurt> LIVING_HURT_CRITICAL_HIT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<LivingHurt> LIVING_HURT_AFTER = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<LivingHurt> LIVING_HURT_AFTER_ARMOR = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<EntityRide> START_RIDING = PrioritizedEvent.createLoop(); // only fires on successful rides, and is not cancellable (if I wanted to make it cancellable, i would add mixinextras)
    public static final PrioritizedEvent<EntityRide> STOP_RIDING = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<StatUpdateEvent> STAT_UPDATE_EVENT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ItemStackAttributeEvent> ITEM_STACK_ATTRIBUTE_EVENT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ReloadEvent> CACHE_CLEAR_EVENT = PrioritizedEvent.createEventResult();
    @Deprecated
    /**
     * @Deprecated use {@link MiapiEvents#GENERATE_MATERIAL_CONVERTERS} instead
     */
    public static final PrioritizedEvent<GeneratedMaterialEvent> GENERATED_MATERIAL = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<CreateMaterialModularConvertersEvent> GENERATE_MATERIAL_CONVERTERS = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<PlayerTickEvent> PLAYER_TICK_START = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<PlayerTickEvent> PLAYER_TICK_END = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<LivingEntityTickEvent> LIVING_ENTITY_TICK_END = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<MaterialCraftEvent> MATERIAL_CRAFT_EVENT = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<SmithingEvent> SMITHING_EVENT = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<LivingEntityAttributeBuild> LIVING_ENTITY_ATTRIBUTE_BUILD_EVENT = PrioritizedEvent.createLoop();
    public static final PrioritizedEvent<PlayerEquip> PLAYER_EQUIP_EVENT = PrioritizedEvent.createLoop();

    public interface ReloadEvent {
        EventResult onReload(boolean isClient);
    }

    public interface PlayerEquip {
        EventResult equip(PlayerEntity player, Map<EquipmentSlot,ItemStack> changes);
    }

    public static class IsCriticalHitEvent {
        public LivingEntity livingEntity;
        public float amount;
        public boolean isCritical = false;

        public IsCriticalHitEvent(LivingEntity livingEntity, float amount, boolean isCritical) {
            this.livingEntity = livingEntity;
            this.amount = amount;
            this.isCritical = isCritical;
        }

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
            if (damageSource.getSource() instanceof ProjectileEntity projectile && (projectile instanceof ItemProjectileEntity itemProjectile)) {
                return itemProjectile.asItemStack();

            }
            if (damageSource.getAttacker() instanceof LivingEntity attacker) {
                return attacker.getMainHandStack();
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
            if (damageSource.getSource() instanceof ProjectileEntity projectile && (projectile instanceof ItemProjectileEntity itemProjectile)) {
                itemStacks.add(itemProjectile.asItemStack());
                if (itemProjectile.getOwner() instanceof LivingEntity attacker) {
                    attacker.getArmorItems().forEach(itemStacks::add);
                }

            }
            if (damageSource.getAttacker() instanceof LivingEntity attacker) {
                attacker.getArmorItems().forEach(itemStacks::add);
            }
            return itemStacks;
        }
    }

    public static class ItemStackAttributeEventHolder {
        public ItemStack itemStack;
        public EquipmentSlot equipmentSlot;
        public Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

        public ItemStackAttributeEventHolder(ItemStack itemStack, EquipmentSlot equipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers) {
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

    public interface LivingEntityAttributeBuild {
        EventResult build(DefaultAttributeContainer.Builder builder);

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
        public ItemStack itemStack;

        public MaterialCraft(ItemStack itemStack) {
            this.itemStack = itemStack;
        }
    }

    public interface PlayerTickEvent {
        EventResult tick(PlayerEntity player);
    }

    public interface LivingEntityTickEvent {
        EventResult tick(LivingEntity entity);
    }

    public interface GeneratedMaterialEvent {
        EventResult generated(GeneratedMaterial material, ItemStack mainIngredient, List<Item> tools, boolean isClient);
    }

    public interface CreateMaterialModularConvertersEvent {
        EventResult generated(Material material, List<Item> tools, boolean isClient);
    }

    public interface SmithingEvent {
        EventResult craft(MaterialCraft itemStack);
    }

    public interface BlockBreakEvent {
        void breakBlock(ServerWorld world, BlockPos pos, ItemStack tool, IntProvider experience);
    }

    public interface LivingHurt {
        EventResult hurt(LivingHurtEvent event);
    }

    public interface BlockCraftingStatUpdate {
        EventResult call(ModularWorkBenchEntity bench, @Nullable PlayerEntity player);
    }

    public interface ItemCraftingStatUpdate {
        EventResult call(ModularWorkBenchEntity bench, Iterable<ItemStack> inventory, @Nullable PlayerEntity player);
    }

    public interface EntityRide {
        void ride(Entity passenger, Entity vehicle);
    }

    public interface StatUpdateEvent {
        EventResult update(ModularWorkBenchEntity blockEntity, StatProvidersMap map, int syncId, PlayerInventory playerInventory, PlayerEntity player, CraftingScreenHandler handler);
    }
}
