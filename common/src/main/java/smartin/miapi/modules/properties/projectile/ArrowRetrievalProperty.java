package smartin.miapi.modules.properties.projectile;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ArrowStorageFacet;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;


public class ArrowRetrievalProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("arrow_retrieve");
    public static ArrowRetrievalProperty property;


    public ArrowRetrievalProperty() {
        super(KEY);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(new MiapiProjectileEvents.ModularProjectileEntityHit() {
            @Override
            public EventResult hit(MiapiProjectileEvents.ModularProjectileEntityHitEvent event) {
                ItemStack projectileStack = event.projectile.thrownStack;
                if(event.entityHitResult.getEntity().level().isClientSide()){
                    return EventResult.pass();
                }
                property.getData(projectileStack).ifPresent((chance) -> {
                    double chanceValue = chance.getValue();
                    if (
                            chanceValue >= 1.0 ||
                            (chanceValue > 0 && event.entityHitResult.getEntity().level().random.nextFloat() < chanceValue)
                    ) {
                        ArrowStorageFacet facet = ArrowStorageFacet.KEY.get(event.entityHitResult.getEntity());
                        if (facet != null) {
                            facet.addArrow(projectileStack);
                        }
                    }
                });
                return EventResult.pass();
            }
        });
        EntityEvent.LIVING_DEATH.register(new EntityEvent.LivingDeath() {
            @Override
            public EventResult die(LivingEntity entity, DamageSource source) {
                if (entity.level().isClientSide()) {
                    return EventResult.pass();
                }
                ArrowStorageFacet facet = ArrowStorageFacet.KEY.get(entity);
                if (facet != null) {
                    facet.getStoredArrows().forEach(itemStack -> {
                        ItemEntity arrowStack = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), itemStack);
                        entity.level().addFreshEntity(arrowStack);
                    });
                }
                return EventResult.pass();
            }
        });
    }
}
