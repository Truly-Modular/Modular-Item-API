package smartin.miapi.upgrade;

import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.TagProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class UpgradeEditView extends InteractAbleWidget {
    private final ScrollList scrollList;
    private final List<InteractAbleWidget> upgradeButtons = new ArrayList<>();
    private final EditOption.EditContext context;
    private final Consumer<UpgradeSelection> onChange;
    private final Consumer<UpgradeSelection> onCraft;
    private UpgradeSelection lastSelected = null;

    public UpgradeEditView(int x, int y, int width, int height, EditOption.EditContext context,
                           Consumer<UpgradeSelection> onChange,
                           Consumer<UpgradeSelection> onCraft) {
        super(x, y, width, height, Component.empty());
        this.context = context;
        this.onChange = onChange;
        this.onCraft = onCraft;

        this.scrollList = new ScrollList(x, y, width, height - 22, upgradeButtons);
        this.addChild(scrollList);

        populateUpgrades();

        SimpleButton<Void> applyButton = new SimpleButton<>(x + width - 50, y + height - 18, 40, 16,
                Component.translatable("miapi.ui.apply"), null, callback -> {
            if (lastSelected != null) {
                this.onCraft.accept(lastSelected);
            }
        });

        this.addChild(applyButton);
    }

    private void populateUpgrades() {
        upgradeButtons.clear();
        ItemStack itemStack = context.getItemstack();
        List<ModuleInstance> modules = ItemModule.getModules(itemStack).allSubModules();

        for (ModuleInstance instance : modules) {
            List<InteractAbleWidget> buttonsForModule = new ArrayList<>();
            ConditionManager.ConditionContext ctx = ConditionManager.playerContext(instance, context.getPlayer(), instance.properties);

            // Prepare upgrade data
            Map<ResourceLocation, Integer> upgradeMap = Map.of();
            List<Upgrade> existingUpgrades = new ArrayList<>();
            if (instance.moduleData.containsKey(Upgrade.upgradeId)) {
                var decodeResult = Upgrade.MODULE_UPGRADE_ID_CODEC
                        .decode(JsonOps.INSTANCE, instance.moduleData.get(Upgrade.upgradeId))
                        .result();
                if (decodeResult.isPresent()) {
                    upgradeMap = decodeResult.get().getFirst();
                    for (ResourceLocation existingId : upgradeMap.keySet()) {
                        Upgrade existingUpgrade = Upgrade.UPGRADE_MIAPI_REGISTRY.get(existingId);
                        if (existingUpgrade != null) {
                            existingUpgrades.add(existingUpgrade);
                        }
                    }
                }
            }

            for (Map.Entry<ResourceLocation, Upgrade> entry : Upgrade.UPGRADE_MIAPI_REGISTRY.getFlatMap().entrySet()) {
                ResourceLocation upgradeId = entry.getKey();
                Upgrade upgrade = entry.getValue();

                int currentLevel = upgradeMap.getOrDefault(upgradeId, 0);

                if (upgrade.condition().isAllowed(ctx)
                    && currentLevel < upgrade.max()
                    && upgrade.isAllowed(existingUpgrades) && TagProperty.getTags(instance).contains(upgrade.moduleTag())) {
                    UpgradeSelection selection = new UpgradeSelection(instance, upgradeId);
                    SimpleButton<UpgradeSelection> button = new SimpleButton<>(
                            getX() + 10, 0, getWidth() - 16, 16,
                            upgrade.name(),
                            selection,
                            sel -> {
                                lastSelected = sel;
                                onChange.accept(sel);
                            }
                    );
                    buttonsForModule.add(button);
                }
            }

            if (!buttonsForModule.isEmpty()) {
                InteractAbleWidget moduleLabel = new InteractAbleWidget(getX() + 2, 0, getWidth() - 4, 16,
                        Component.literal(instance.getModuleName().getString()).withStyle(style -> style.withBold(true))) {
                    @Override
                    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
                        drawContext.drawString(
                                Minecraft.getInstance().font,
                                getMessage(),
                                getX() + 2,
                                getY() + 4,
                                0xFFFFFF,
                                false
                        );
                    }
                };
                upgradeButtons.add(moduleLabel);
                upgradeButtons.addAll(buttonsForModule);
            }
        }
        scrollList.setList(upgradeButtons);
    }


    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.renderWidget(drawContext, mouseX, mouseY, delta);
    }

}
