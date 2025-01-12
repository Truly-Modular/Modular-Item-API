package smartin.miapi.modules.properties.onHit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.EventResult;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.MaceItem;
import smartin.miapi.Miapi;
import smartin.miapi.events.ModularAttackEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.*;

public class PogoAbility extends CodecProperty<PogoAbility.PogoData> {
    public static ResourceLocation KEY = Miapi.id("pogo_ability");
    public static PogoAbility property;

    public PogoAbility() {
        super(PogoData.CODEC);
        property = this;
        MaceItem.createAttributes();
        ModularAttackEvents.HURT_ENEMY.register((stack, target, attacker) -> {
            var optional = getData(stack);
            if (attacker instanceof ServerPlayer serverPlayer && !serverPlayer.onGround()) {
                if (optional.isPresent()) {
                    ServerLevel serverLevel = (ServerLevel) attacker.level();
                    if (serverPlayer.isIgnoringFallDamageFromCurrentImpulse() && serverPlayer.currentImpulseImpactPos != null) {
                        if (serverPlayer.currentImpulseImpactPos.y > serverPlayer.position().y) {
                            serverPlayer.currentImpulseImpactPos = serverPlayer.position();
                        }
                    } else {
                        serverPlayer.currentImpulseImpactPos = serverPlayer.position();
                    }

                    serverPlayer.setIgnoreFallDamageFromCurrentImpulse(true);
                    //serverPlayer.setSpawnExtraParticlesOnFall(true);
                    SoundEvent soundEvent = optional.get().soundEvent();
                    double upwards = optional.get().resolvable().getValue();
                    serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), soundEvent, serverPlayer.getSoundSource(), 1.0F, 1.0F);
                    serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().with(Direction.Axis.Y, 0.2 * upwards));
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }
            }
            return EventResult.pass();
        });
    }

    @Override
    public PogoData initialize(PogoData data, ModuleInstance moduleInstance) {
        return new PogoData(data.resolvable().initialize(moduleInstance), data.soundEvent());
    }

    @Override
    public PogoData merge(PogoData left, PogoData right, MergeType mergeType) {
        return new PogoData(
                left.resolvable().merge(
                        right.resolvable(),
                        mergeType),
                MergeAble.decideLeftRight(
                        left.soundEvent(),
                        right.soundEvent(),
                        mergeType));
    }

    public record PogoData(DoubleOperationResolvable resolvable, SoundEvent soundEvent) {
        public static Codec<PogoData> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                        DoubleOperationResolvable.CODEC
                                .optionalFieldOf("strength", new DoubleOperationResolvable(1))
                                .forGetter(PogoData::resolvable),
                        SoundEvent.DIRECT_CODEC
                                .optionalFieldOf("sound", SoundEvents.EMPTY)
                                .forGetter(PogoData::soundEvent)
                ).apply(instance, PogoData::new));
    }
}
