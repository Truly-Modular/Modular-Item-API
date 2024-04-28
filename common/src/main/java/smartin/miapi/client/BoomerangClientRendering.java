package smartin.miapi.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.modules.abilities.BoomerangThrowingAbility;

import java.util.stream.Stream;

public class BoomerangClientRendering {

    public static void setup(){
        if(true){
            return;
        }
        ClientTickEvent.CLIENT_PRE.register((instance -> {
            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.getWorld() != null) {
                BoomerangThrowingAbility.getLookingEntity(MinecraftClient.getInstance().player, 200, instance.getTickDelta(), 0.015).findFirst().ifPresent(entity -> {
                    BoomerangThrowingAbility.entities.add(entity);
                    //Miapi.LOGGER.info("found" + entity.getDisplayName().getString());
                });
            }
        }));
        ClientTickEvent.CLIENT_PRE.register((client)-> {

            if (client != null && client.player != null && client.player.getWorld() != null) {
                Stream<Entity> entityStream = BoomerangThrowingAbility.getLookingEntity(client.player, 200, client.getTickDelta(), 0.045);
                Miapi.LOGGER.info("Entities: " + entityStream.count());

                BoomerangThrowingAbility.getLookingEntity(client.player, 200, client.getTickDelta(), 0.045).forEach(entity -> {
                    MinecraftClient.getInstance().world.addParticle(new DustParticleEffect(new Vector3f(1, 1, 1), 0),entity.getX(),entity.getY(),entity.getZ(),0,0,0);
                });
            }
        });
        ClientTickEvent.CLIENT_PRE.register(client -> {
            BoomerangThrowingAbility.entities.forEach(entity -> {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20));
                }
            });
        });
    }
}
