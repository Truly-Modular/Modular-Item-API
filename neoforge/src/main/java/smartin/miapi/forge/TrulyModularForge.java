package smartin.miapi.forge;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.render.ModelProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.function.Consumer;

import static smartin.miapi.Miapi.MOD_ID;

@Mod(MOD_ID)
public class TrulyModularForge {
    public TrulyModularForge() {
        NeoForge.EVENT_BUS.register(new ServerEvents());
        Miapi.init();

        loadCompat("epicfight", () -> {
            //RegistryInventory.moduleProperties.register(EpicFightCompatProperty.KEY, new EpicFightCompatProperty())
        });

        //use explicit classpath to prevent accidental class loading
        loadCompat("quark", smartin.miapi.forge.compat.QuarkCompat::setup);
        loadCompat("apotheosis", smartin.miapi.forge.compat.ApotheosisCompat::setup);


        LifecycleEvent.SERVER_STARTING.register((instance -> setupAttributes()));
        ReloadEvents.START.subscribe((isClient, access) -> setupAttributes());

        //TODO: why no worky
        //KEY_BINDINGS.addCallback((KeyBindingRegistryImpl::registerKeyBinding));
    }

    public static void setupAttributes() {
        AttributeRegistry.SWIM_SPEED = NeoForgeMod.SWIM_SPEED;
        AttributeProperty.replaceMap.put("miapi:generic.swim_speed", () -> AttributeRegistry.SWIM_SPEED.value());
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID)
    public static class ModEvents {
        @SubscribeEvent
        public static void enqueueIMC(InterModEnqueueEvent event) {
            if (Platform.isModLoaded("treechop")) {
                InterModComms.sendTo("treechop", "getTreeChopAPI", () -> (Consumer<Object>) smartin.miapi.modules.properties.compat.ht_treechop.TreechopUtil::setTreechopApi);
            }
        }

        @SubscribeEvent
        public static void addEntityAttributes(EntityAttributeModificationEvent attributeModificationEvent) {
            AttributeRegistry.entityAttributeMap.forEach((id, attribute) -> {
                attributeModificationEvent.getTypes().forEach(entityType -> {
                    attributeModificationEvent.add(entityType, attribute);
                });
            });
        }
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = Miapi.MOD_ID)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void entityRenderers(ModelEvent.ModifyBakingResult registerAdditional) {
            //dont ask me, but this fixes registration for client
            List<ModelResourceLocation> ids = RegistryInventory.modularItems.getFlatMap().keySet().stream().map(ModelResourceLocation::inventory).toList();
            ModelProperty.textureGetter = registerAdditional.getTextureGetter();
            ids.forEach(id -> {
                registerAdditional.getModels().put(id, new ItemBakedModelReplacement());
            });
            setupAttributes();
        }
    }

    public static class ServerEvents {

        @SubscribeEvent
        public void addReloadListeners(AddReloadListenerEvent addReloadListenerEvent) {
            Miapi.registryAccess = addReloadListenerEvent.getRegistryAccess();
            addReloadListenerEvent.addListener(new MiapiReloadListenerForge(addReloadListenerEvent::getRegistryAccess));
        }
    }

    public static class ClientEvents {
        /*
        @SubscribeEvent
        public void onRenderGameOverlayEventPre(RenderGuiEvent event) {
            DrawContext context = event.getGuiGraphics();
            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
            if (playerEntity.isCreative()) {
                return;
            }
            int heartBars = (int) Math.ceil(playerEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) / 20);
            ShieldingArmorFacet facet = ShieldingArmorFacet.KEY.get(playerEntity);
            // Calculate health and absorption values
            int playerHealth = MathHelper.ceil(playerEntity.getHealth());
            int renderHealthValue = playerHealth + MathHelper.ceil(playerEntity.getAbsorptionAmount());
            int scaledWidth = event.getWindow().getScaledWidth();
            int shieldingArmorCurrentAmount = (int) (facet.getCurrentAmount());
            int scaledHeight = event.getWindow().getScaledHeight();
            float maxHealth = Math.max((float) playerEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH), (float) Math.max(renderHealthValue, playerHealth));
            int absorptionAmount = MathHelper.ceil(playerEntity.getAbsorptionAmount());
            int healthAbsorptionTotal = MathHelper.ceil((maxHealth + (float) absorptionAmount) / 2.0F / 10.0F);
            int numHearts = Math.max(10 - (healthAbsorptionTotal - 2), 3);
            int startY = scaledHeight - 39 - 10;
            if (MiapiConfig.INSTANCE.client.shieldingArmor.respectHealth) {
                startY -= (healthAbsorptionTotal - 1) * numHearts;
            }
            if (MiapiConfig.INSTANCE.client.shieldingArmor.respectArmor && playerEntity.getArmor() > 0) {
                startY -= 10;
            }
            startY -= MiapiConfig.INSTANCE.client.shieldingArmor.otherOffests * 10;
            startY -= MiapiConfig.INSTANCE.client.shieldingArmor.attributesSingleLine.stream()
                              .filter(id -> Registries.ATTRIBUTE.containsId(id))
                              .map(id -> Registries.ATTRIBUTE.get(id))
                              .filter(entityAttribute -> playerEntity.getAttributes().hasAttribute(entityAttribute))
                              .filter(entityAttribute -> playerEntity.getAttributeValue(entityAttribute) > 1)
                              .count() * 10;
            for (
                    int index = 0;
                    index < ((float) facet.getMaxAmount()) / 2.0f; index++) {
                int heartX = scaledWidth / 2 - 91 + (index % 10) * 8;
                int yOffset = (index / 10) * 10;
                int heartTextureIndex = index * 2 + 1;
                if (heartTextureIndex < shieldingArmorCurrentAmount) {
                    context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 430, 96, 9, 9, 512, 512);
                } else if (heartTextureIndex == shieldingArmorCurrentAmount) {
                    context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 439, 96, 9, 9, 512, 512);
                } else {
                    context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 448, 96, 9, 9, 512, 512);
                }
            }
            smartin.miapi.events.ClientEvents.HUD_RENDER.invoker().render(context, MinecraftClient.getInstance().getTickDelta());
        }

         */
    }

    public static void loadCompat(String modId, Runnable onLoaded) {
        try {
            if (Platform.isModLoaded(modId)) {
                onLoaded.run();
            }
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("could not setup compat for " + modId, e);
        }
    }
}