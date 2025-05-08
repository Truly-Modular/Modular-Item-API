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
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class UpgradeEditView extends InteractAbleWidget {
    private final ScrollList scrollList;
    private final List<InteractAbleWidget> upgradeButtons = new ArrayList<>();
    private final EditOption.EditContext context;
    private final Consumer<UpgradeSelection> onChange;
    private final Consumer<UpgradeSelection> onCraft;
    private UpgradeSelection lastSelected = null;

    private int availableXP = 0;
    private int usedLevels = 0;
    int maxToNextLevel = 0;
    private int availablePoints = 0;
    int currentLVLXp = 0;

    public UpgradeEditView(int x, int y, int width, int height, EditOption.EditContext context,
                           Consumer<UpgradeSelection> onChange,
                           Consumer<UpgradeSelection> onCraft) {
        super(x, y, width, height, Component.empty());
        this.context = context;
        this.onChange = onChange;
        this.onCraft = onCraft;

        this.scrollList = new ScrollList(x, y, width, height - 24, upgradeButtons);
        this.addChild(scrollList);

// Fetch current item XP and upgrade count
        this.availableXP = getItemXP(context.getItemstack());
        this.usedLevels = UpgradeEditOption.getTotalUpgradeLevel(context.getItemstack());

// Determine how many upgrade "points" the player can afford
        int xp = availableXP;
        int simulatedLevel = usedLevels;
        int upgradePoints = 0;
        int upgradeCost = Upgrade.xpCost(simulatedLevel);
        while (xp >= upgradeCost && xp > 0) {
            xp -= upgradeCost;
            simulatedLevel++;
            upgradePoints++;
            upgradeCost = Upgrade.xpCost(simulatedLevel);
        }
        final int points = upgradePoints;
        final int xpToNextPoint = Upgrade.xpCost(simulatedLevel) - xp;
        maxToNextLevel = xpToNextPoint;
        availablePoints = points;
        currentLVLXp = xp;


        // Points display
        this.addChild(new InteractAbleWidget(x + 4, y + height - 13, 100, 12, Component.literal("")) {
            @Override
            public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
                drawContext.drawString(
                        Minecraft.getInstance().font,
                        "Points: " + points,
                        getX(),
                        getY(),
                        0xFFFFFF,
                        false
                );
            }
        });
        int simulatedFinal = simulatedLevel;
        // XP progress bar toward next point
        this.addChild(new InteractAbleWidget(x + width - 84, y + height - 15, 30, 12, Component.literal("")) {
            @Override
            public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
                int costForNext = Upgrade.xpCost(simulatedFinal);
                int progress = (int) (((costForNext - xpToNextPoint) / (double) costForNext) * 100);
                int barWidth = (int) (progress / 100.0 * 30);
                drawContext.fill(getX(), getY(), getX() + 30, getY() + 10, 0xFF555555);
                drawContext.fill(getX(), getY(), getX() + barWidth, getY() + 10, 0xFF00FF00);
                drawContext.drawString(Minecraft.getInstance().font, progress + "%", getX() + 5, getY() + 2, 0xFFFFFFFF, false);
            }
        });


        populateUpgrades(upgradePoints);

        SimpleButton<Void> applyButton = new SimpleButton<>(x + width - 45, y + height - 18, 40, 16,
                Component.translatable("miapi.ui.apply"), null, callback -> {
            if (lastSelected != null) {
                this.onCraft.accept(lastSelected);
            }
        });

        this.addChild(applyButton);
    }

    private void populateUpgrades(int points) {
        upgradeButtons.clear();
        ItemStack itemStack = context.getItemstack();
        List<ModuleInstance> modules = ItemModule.getModules(itemStack).allSubModules();

        for (ModuleInstance instance : modules) {
            List<InteractAbleWidget> buttonsForModule = new ArrayList<>();
            ConditionManager.ConditionContext ctx = ConditionManager.playerContext(instance, context.getPlayer(), instance.properties);

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
                    && upgrade.isAllowed(existingUpgrades)
                    && TagProperty.getTags(instance).contains(upgrade.moduleTag())) {

                    UpgradeSelection selection = new UpgradeSelection(instance, upgradeId);
                    SimpleButton<UpgradeSelection> button = new SimpleButton<>(
                            getX() + 10, 0, getWidth() - 16, 16,
                            upgrade.name().copy().append(" ").append(Component.translatable("miapi.upgrade.cost", upgrade.cost())),
                            selection,
                            sel -> {
                                lastSelected = sel;
                                onChange.accept(sel);
                            }
                    ) {
                        public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
                            super.renderHover(drawContext, mouseX, mouseY, delta);
                            if (isMouseOver(mouseX, mouseY) && scrollList.isMouseOver(mouseX, mouseY)) {
                                drawContext.renderTooltip(
                                        Minecraft.getInstance().font,
                                        points >= upgrade.cost() ?
                                                List.of(upgrade.description()) :
                                                List.of(Component.translatable("miapi.upgrade.cannot.afford"), upgrade.description()),
                                        Optional.empty(),
                                        mouseX,
                                        mouseY);
                            }
                        }
                    };
                    button.isEnabled = points >= upgrade.cost();
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

    // Dummy methods for XP (to be implemented later)
    private int getItemXP(ItemStack stack) {
        return stack.getOrDefault(Upgrade.COMPONENT, 0);
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.renderWidget(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.renderHover(drawContext, mouseX, mouseY, delta);
        if (isMouseOver(mouseX, mouseY) && !scrollList.isMouseOver(mouseX, mouseY) && (getX() + getHeight() - 20) > mouseX && (getY() + getWidth() - 35) > mouseY) {
            drawContext.renderTooltip(
                    Minecraft.getInstance().font,
                    List.of(
                            Component.translatable("miapi.upgrade.hover.1", currentLVLXp, currentLVLXp + maxToNextLevel),
                            Component.translatable("miapi.upgrade.hover.2", availablePoints),
                            Component.translatable("miapi.upgrade.hover.3", usedLevels),
                            Component.translatable("miapi.upgrade.hover.4", usedLevels),
                            Component.translatable("miapi.upgrade.hover.5", usedLevels)
                    ),
                    Optional.empty(),
                    mouseX,
                    mouseY);
        }
    }
}
