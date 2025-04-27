package smartin.miapi.mixin.loot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.Miapi;

import java.util.Optional;
import java.util.function.Consumer;

import static smartin.miapi.loot.LootHelper.LOOT_TABLE_PARAM;

@Mixin(LootTable.class)
public class LootTableMixin {
    /*
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParamSet;Ljava/util/Optional;Ljava/util/List;Ljava/util/List;)V",
            at = @At(value = "HEAD"), ordinal = 0)
    private static List<LootItemFunction> miapi$adjustLootTable(List<LootItemFunction> function) {
        List<LootItemFunction> adjusted = new ArrayList<>(function);
        adjusted.add(
                new MaterialSwapLootFunction(
                        Miapi.id("empty"),
                        -3.0,
                        0.5,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        Optional.empty(),
                        Optional.empty()));
        adjusted.add(new ModuleSwapLootFunction(
                Miapi.id("empty"),
                1.0,
                Optional.empty(),
                Optional.empty()));
        return adjusted;
    }

     */

    @Inject(method = "Lnet/minecraft/world/level/storage/loot/LootTable;fill(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/storage/loot/LootParams;J)V", at = @At("HEAD"))
    public void miapi$captureLootTableParamBlock(Container container, LootParams params, long seed, CallbackInfo ci) {
        LootTable lootTable = (LootTable) (Object) this;
        miapiTryGetLootTableID(params.getLevel(), lootTable).ifPresent(id -> {
            ((LootParamsAccessor) params).getParams().put(LOOT_TABLE_PARAM, id);
        });
    }

    @Inject(method = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("HEAD"))
    public void miapi$captureLootTableParamListDirect(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        LootParams params = ((LootContextAccessor) context).getParams();
        LootTable lootTable = (LootTable) (Object) this;
        miapiTryGetLootTableID(params.getLevel(), lootTable).ifPresent(id -> {
            ((LootParamsAccessor) params).getParams().put(LOOT_TABLE_PARAM, id);
        });
    }

    @Inject(method = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    public void miapi$captureLootTableParamListRaw(LootContext context, Consumer<ItemStack> output, CallbackInfo ci) {
        LootParams params = ((LootContextAccessor) context).getParams();
        LootTable lootTable = (LootTable) (Object) this;
        miapiTryGetLootTableID(params.getLevel(), lootTable).ifPresent(id -> {
            ((LootParamsAccessor) params).getParams().put(LOOT_TABLE_PARAM, id);
        });
    }

    private static Optional<ResourceLocation> miapiTryGetLootTableID(ServerLevel level, LootTable lootTable) {
        try {
            var reg = level.getServer().reloadableRegistries().get().registry(Registries.LOOT_TABLE);
            return Optional.ofNullable(reg.get().getKey(lootTable));
        } catch (RuntimeException e) {
            Miapi.LOGGER.info("could not lookup loot table", e);
            LootCommand command;
        }
        try {
        } catch (RuntimeException e) {
            Miapi.LOGGER.info("could not lookup loot table", e);
        }
        return Optional.empty();
    }
}