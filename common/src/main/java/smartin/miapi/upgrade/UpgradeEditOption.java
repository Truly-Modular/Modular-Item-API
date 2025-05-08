package smartin.miapi.upgrade;

import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.EditOptionIcon;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.network.Networking;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UpgradeEditOption implements EditOption {

    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext editContext) {
        UpgradeSelection selection = UpgradeSelection.STREAM_CODEC.decode(buffer);
        Upgrade upgrade = Upgrade.UPGRADE_MIAPI_REGISTRY.get(selection.upgradeId());
        if (upgrade == null) return editContext.getItemstack();

        ItemStack itemStack = editContext.getItemstack().copy();
        ModuleInstance instance = selection.resolveModule(itemStack).orElse(null);
        if (instance == null) return itemStack;

        ConditionManager.ConditionContext conditionCtx = ConditionManager.playerContext(instance, editContext.getPlayer(), instance.properties);
        if (!upgrade.condition().isAllowed(conditionCtx)) return itemStack;

        instance.clearCaches();
        instance = instance.copy();

        Map<ResourceLocation, Integer> rawUpgradeMap = new HashMap<>();
        if (instance.moduleData.containsKey(Upgrade.upgradeId)) {
            Upgrade.MODULE_UPGRADE_ID_CODEC.decode(JsonOps.INSTANCE, instance.moduleData.get(Upgrade.upgradeId))
                    .result().ifPresent(pair -> rawUpgradeMap.putAll(pair.getFirst()));
        }

        int currentLevel = rawUpgradeMap.getOrDefault(selection.upgradeId(), 0);
        if (currentLevel >= upgrade.max()) return itemStack;

        rawUpgradeMap.put(selection.upgradeId(), currentLevel + 1);

        instance.moduleData.put(
                Upgrade.upgradeId,
                Upgrade.MODULE_UPGRADE_ID_CODEC.encodeStart(JsonOps.INSTANCE, rawUpgradeMap).getOrThrow()
        );

        instance.getRoot().writeToItem(itemStack);
        instance.clearCaches();
        ComponentApplyProperty.updateItemStack(itemStack, editContext.getPlayer().registryAccess());

        return itemStack;
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        return VisualModularItem.isVisualModularItem(editContext.getItemstack()) &&
               MiapiPermissions.hasPerm(editContext.getPlayer(), "upgrade");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        return new UpgradeEditView(x, y, width, height, editContext,
                (selection) -> {
                    FriendlyByteBuf buffer = Networking.createBuffer();
                    UpgradeSelection.STREAM_CODEC.encode(buffer, selection);
                    editContext.preview(buffer);
                },
                (selection) -> {
                    FriendlyByteBuf buffer = Networking.createBuffer();
                    UpgradeSelection.STREAM_CODEC.encode(buffer, selection);
                    editContext.craft(buffer);
                });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339, 220, 512, 512, "miapi.ui.edit_option.hover.upgrade", this);
    }
}
