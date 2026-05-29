package smartin.miapi.modules.properties.onHit;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import dev.architectury.event.EventResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.events.MeleeModularAttackEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ModularAttackCommandProperty extends CodecProperty<List<ModularAttackCommandProperty.CommandContext>> {
    public static ResourceLocation KEY = Miapi.id("damage_command");
    public static Codec<List<CommandContext>> CODEC = Codec.list(AutoCodec.of(CommandContext.class).codec());
    public static ModularAttackCommandProperty property = new ModularAttackCommandProperty();

    protected ModularAttackCommandProperty() {
        super(CODEC);
        MeleeModularAttackEvents.HURT_ENEMY.register((stack, target, attacker) -> {
            hurtEnemyMelee(stack, target, attacker, true);
            return EventResult.pass();
        });
        MeleeModularAttackEvents.HURT_ENEMY_POST.register((stack, target, attacker) -> {
            hurtEnemyMelee(stack, target, attacker, false);
            return EventResult.pass();
        });
        MiapiEvents.LIVING_HURT.register(event -> {
            livingHurt(event, true);
            return EventResult.pass();
        });
        MiapiEvents.LIVING_HURT_AFTER.register(event -> {
            livingHurt(event, false);
            return EventResult.pass();
        });
    }

    private void livingHurt(MiapiEvents.LivingHurtEvent event, boolean before) {
        if (event.attacker instanceof LivingEntity attacker && attacker.level() instanceof ServerLevel serverLevel) {
            if (event.damageSource.isDirect()) {
                event.getCausingItemStackAndArmorOfAttacker().forEach(stack -> {
                    getData(stack).ifPresent(data -> {
                        performCommands(data, event.defender, attacker, serverLevel, c -> c.onMeleeAttack && (c.runBefore == before));
                    });
                });
            } else {
                event.getCausingItemStackAndArmorOfAttacker().forEach(stack -> {
                    getData(stack).ifPresent(data -> {
                        performCommands(data, event.defender, attacker, serverLevel, c -> c.onRangedAttack && (c.runBefore == before));
                    });
                });
            }
        }
    }

    private void hurtEnemyMelee(ItemStack stack, LivingEntity target, LivingEntity attacker, boolean runBefore) {
        if (attacker.level() instanceof ServerLevel serverLevel) {
            getData(stack).ifPresent(data ->
                    performCommands(data, target, attacker, serverLevel,
                            (c) -> c.onMeleeWeapon && !c.onMeleeAttack && (c.runBefore == runBefore)));
        }
    }

    public static void performCommands(List<ModularAttackCommandProperty.CommandContext> context, LivingEntity defender, LivingEntity attacker, ServerLevel serverLevel, Predicate<CommandContext> predicate) {
        context.forEach(commandContext -> {
            if (predicate.test(commandContext)) {
                CommandSourceStack source = serverLevel.getServer().createCommandSourceStack();
                if (commandContext.runAs.equals(CommandContextEntity.ATTACKER)) {
                    source = source.withEntity(attacker);
                } else if (commandContext.runAs.equals(CommandContextEntity.DEFENDER)) {
                    source = source.withEntity(defender);
                }
                if (!commandContext.commandFeedback) {
                    source = source.withSuppressedOutput();
                }

                if (commandContext.runAt.equals(CommandContextEntity.ATTACKER)) {
                    source = source.withPosition(attacker.position()).withEntity(attacker);
                } else if (commandContext.runAt.equals(CommandContextEntity.DEFENDER)) {
                    source = source.withPosition(defender.position()).withEntity(defender);
                }

                for (String cmd : commandContext.command) {
                    serverLevel.getServer().getCommands().performPrefixedCommand(source, cmd);
                }
            }
        });
    }

    @Override
    public List<CommandContext> merge(List<CommandContext> left, List<CommandContext> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }

    public static class CommandContext {
        @SuppressWarnings("unused")
        public static Codec<List<String>> LIST_CODEC = Miapi.toListOrSimple(Codec.STRING);
        @CodecBehavior.Optional
        @CodecBehavior.Override("LIST_CODEC")
        List<String> command = new ArrayList<>();
        @CodecBehavior.Optional
        boolean onMeleeWeapon = false;
        @CodecBehavior.Optional
        boolean commandFeedback = false;
        @CodecBehavior.Optional
        boolean runBefore = false;
        @CodecBehavior.Optional
        boolean onMeleeAttack = false;
        @CodecBehavior.Optional
        boolean onRangedAttack = false;
        @CodecBehavior.Optional
        CommandContextEntity runAt = CommandContextEntity.DEFENDER;
        @CodecBehavior.Optional
        CommandContextEntity runAs = CommandContextEntity.NONE;

    }

    public enum CommandContextEntity {
        DEFENDER,
        ATTACKER,
        NONE,
    }
}
