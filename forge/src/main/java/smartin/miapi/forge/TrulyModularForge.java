package smartin.miapi.forge;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.entity.ShieldingArmorFacet;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.forge.compat.ApotheosisCompat;
import smartin.miapi.forge.compat.QuarkCompat;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.compat.ht_treechop.TreechopUtil;
import smartin.miapi.registries.RegistryInventory;

import java.util.function.Consumer;

import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.attributes.AttributeRegistry.SWIM_SPEED;

@Mod(MOD_ID)
public class TrulyModularForge {
    public TrulyModularForge() {
        // Submit our event bus to let architectury register our content on the right time
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Items.DIAMOND_SWORD.asItem();
        IForgeItem item;
        ItemStack itemStack;
        LivingEntity livingEntity;
        EventBuses.registerModEventBus(MOD_ID, bus);
        if (Environment.isClient()) {
            bus.register(new ClientModEvents());
            MinecraftForge.EVENT_BUS.register(new ClientEvents());
        }
        if(Platform.isModLoaded("quark")){
            try{
                QuarkCompat.setup();
            }catch (Exception e){
                Miapi.LOGGER.info("couldnt load quark compat",e);
            }
        }
        bus.register(new ModEvents());
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        Miapi.init();

        try {
            if (Platform.isModLoaded("epicfight")) {
                //RegistryInventory.moduleProperties.register(EpicFightCompatProperty.KEY, new EpicFightCompatProperty());
            }
        } catch (Exception e) {
            Miapi.LOGGER.info("couldnt load epic fight compat");
        }
        if (Platform.isModLoaded("apotheosis")) {
            try {
                ApotheosisCompat.setup();
            } catch (RuntimeException surpressed) {
                Miapi.LOGGER.warn("couldnt load Apotheosis compat", surpressed);
            }
        }

        //if (Platform.isModLoaded("epicfight"))
        //RegistryInventory.moduleProperties.register(EpicFightCompatProperty.KEY, new EpicFightCompatProperty());


        LifecycleEvent.SERVER_STARTING.register((instance -> setupAttributes()));
        ReloadEvents.START.subscribe((isClient -> setupAttributes()));


        //ReloadListenerRegistry.register(
        //        ResourceType.SERVER_DATA,
        //        new MiapiReloadListenerForge(),
        //        new Identifier(MOD_ID, "main_reload_listener"),
        //        List.of(new Identifier("minecraft:tags"), new Identifier("minecraft:recipes")));

        AttributeProperty.replaceMap.put("miapi:generic.reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("forge:block_reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("forge:entity_reach", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("miapi:generic.swim_speed", () -> SWIM_SPEED);

    }

    public static void setupAttributes() {
        AttributeRegistry.REACH = ForgeMod.BLOCK_REACH.get();
        AttributeRegistry.ATTACK_RANGE = ForgeMod.ENTITY_REACH.get();
        AttributeRegistry.SWIM_SPEED = ForgeMod.SWIM_SPEED.get();
        AttributeProperty.replaceMap.put("miapi:generic.reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("forge:block_reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("forge:entity_reach", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", ForgeMod.BLOCK_REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", ForgeMod.ENTITY_REACH);
        AttributeProperty.replaceMap.put("miapi:generic.swim_speed", () -> SWIM_SPEED);
    }

    public static class ModEvents {
        @SubscribeEvent
        public void enqueueIMC(InterModEnqueueEvent event) {
            if (Platform.isModLoaded("treechop")) {
                InterModComms.sendTo("treechop", "getTreeChopAPI", () -> (Consumer<Object>) TreechopUtil::setTreechopApi);
            }
            Item item = RegistryInventory.modularAxe;
            Miapi.LOGGER.info("INJECTION_TEST" + item.canPerformAction(item.getDefaultStack(), ToolActions.AXE_DIG));
        }
    }

    public static class ClientModEvents {
        @SubscribeEvent
        public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            //dont ask me, but this fixes registration for client
            setupAttributes();
        }

        @SubscribeEvent
        public void registerBindings(RegisterKeyMappingsEvent event) {
            MiapiClient.KEY_BINDINGS.addCallback(event::register);
        }
    }

    public static class ServerEvents {
        @SubscribeEvent
        public void damageEvent(LivingHurtEvent hurtEvent) {
            MiapiEvents.LivingHurtEvent event = new MiapiEvents.LivingHurtEvent(hurtEvent.getEntity(), hurtEvent.getSource(), hurtEvent.getAmount());
            MiapiEvents.LIVING_HURT.invoker().hurt(event);
            hurtEvent.setAmount(event.amount);
        }

        @SubscribeEvent
        public void addReloadListeners(AddReloadListenerEvent addReloadListenerEvent) {
            addReloadListenerEvent.addListener(new MiapiReloadListenerForge());
        }
    }

    public static class ClientEvents {
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
    }
}