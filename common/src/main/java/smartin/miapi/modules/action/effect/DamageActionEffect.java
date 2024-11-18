package smartin.miapi.modules.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.action.ActionContext;
import smartin.miapi.modules.action.ActionEffect;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.List;
import java.util.Optional;

public record DamageActionEffect(String sourceKey, String targetKey, boolean useWeaponOnHit, boolean useEnchants,
                                 Optional<String> item,
                                 DoubleOperationResolvable value,
                                 ResourceLocation type) implements ActionEffect {

    public static final ResourceLocation TYPE = Miapi.id("damage_action_effect");

    // Codec for serialization with fields "damageType", "sourceKey", and "targetKey"
    public static final Codec<DamageActionEffect> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("source").forGetter(DamageActionEffect::sourceKey),
                    Codec.STRING.fieldOf("target").forGetter(DamageActionEffect::targetKey),
                    Codec.BOOL.optionalFieldOf("use_weapon_effects", true).forGetter(DamageActionEffect::useWeaponOnHit),
                    Codec.BOOL.optionalFieldOf("use_enchants", true).forGetter(DamageActionEffect::useEnchants),
                    Codec.STRING.optionalFieldOf("item").forGetter(DamageActionEffect::item),
                    DoubleOperationResolvable.CODEC.fieldOf("damage").forGetter(DamageActionEffect::value),
                    ResourceLocation.CODEC.fieldOf("type").orElse(TYPE).forGetter(DamageActionEffect::type)
            ).apply(instance, DamageActionEffect::new));

    @Override
    public List<String> dependency(ActionContext context) {
        // This effect depends on a source entity and a list of target entities in the context
        Advancement advancement;
        return List.of(sourceKey, targetKey);
    }

    @Override
    public boolean setup(ActionContext context) {
        // Check if the source entity and target entities are present in the context
        boolean sourceExists = context.getObject(Entity.class, sourceKey).isPresent();
        boolean targetsExist = context.getList(Entity.class, targetKey).isPresent();
        return sourceExists && targetsExist;
    }

    @Override
    public void execute(ActionContext context) {
        // Retrieve the source entity
        Optional<Entity> sourceOpt = context.getObject(Entity.class, sourceKey);
        // Retrieve the list of target entities
        Optional<List<Entity>> targetsOpt = context.getList(Entity.class, targetKey);

        ItemStack itemStack = context.contextItem;
        if (item.isPresent()) {
            var opt = context.getObject(ItemStack.class, item().get());
            if (opt.isPresent()) {
                itemStack = opt.get();
            }
        }

        if (sourceOpt.isPresent() && targetsOpt.isPresent()) {
            Entity sourceEntity = sourceOpt.get();
            List<Entity> targetEntities = targetsOpt.get();

            // Create a damage source based on the damage typ
            Holder<DamageType> damageType = context.level.registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolder(type).get();

            DamageSource damageSource = new DamageSource(damageType, sourceEntity);

            // Apply damage to each target entity
            for (Entity targetEntity : targetEntities) {
                float bonusDamage = 0;
                if (useEnchants() && itemStack != null) {
                    if (sourceEntity instanceof Player player) {
                        if (targetEntity instanceof LivingEntity livingTarget) {
                            itemStack.hurtEnemy(livingTarget, player);
                        }
                    }
                    bonusDamage = itemStack.getItem().getAttackDamageBonus(targetEntity, (float) value().getValue(), damageSource);
                }
                if (useEnchants()) {
                    bonusDamage += EnchantmentHelper.modifyDamage(context.level, itemStack, targetEntity, damageSource, (float) value().getValue());
                }
                targetEntity.hurt(damageSource, (float) value().getValue() + bonusDamage);
                if (useEnchants()) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(context.level, sourceEntity, damageSource, itemStack);
                }
            }
        }
    }

    @Override
    public DamageActionEffect initialize(ModuleInstance moduleInstance) {
        return new DamageActionEffect(sourceKey(), targetKey(), useWeaponOnHit(), useEnchants(), item(), value().initialize(moduleInstance), type());
    }

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }
}
