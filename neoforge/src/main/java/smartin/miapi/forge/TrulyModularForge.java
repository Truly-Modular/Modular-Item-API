package smartin.miapi.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.ArmorModelManager;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.forge.compat.epic_fight.EpicFightCompat;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.render.baked.ModelManager;
import smartin.miapi.registries.DatapackHolder;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.events.MiapiEvents.GET_ITEM_SHIELD_COOLDOWN;

@Mod(MOD_ID)
public class TrulyModularForge {
    public static int test = 0;
    public static final IEventBus BUS = ModLoadingContext.get().getActiveContainer().getEventBus();

    public TrulyModularForge() {
        NeoForge.EVENT_BUS.register(new ServerEvents());
        Miapi.init();
        loadCompat("epicfight", () -> {
            EpicFightCompat.setup();
        });

        //use explicit classpath to prevent accidental class loading
        loadCompat("quark", smartin.miapi.forge.compat.QuarkCompat::setup);
        loadCompat("apotheosis", smartin.miapi.forge.compat.ApotheosisCompat::setup);
        //loadCompat("minecolonies", smartin.miapi.forge.compat.minecolonies.MineColoniesCompat::setup);


        LifecycleEvent.SERVER_STARTING.register((instance -> setupAttributes()));
        ReloadEvents.START.subscribe((isClient, access, worker) -> setupAttributes());
        if (Platform.getEnv() == Dist.CLIENT) {
            //KEY_BINDINGS.addCallback((KeyBindingRegistryImpl::registerKeyBinding));
            ClientLifecycleEvent.CLIENT_STARTED.register(new ClientLifecycleEvent.ClientState() {
                @Override
                public void stateChanged(Minecraft instance) {
                    RegistryInventory.MODULAR_ITEMS.getFlatMap().values().forEach(item -> {
                        var methods = item.getClass().getDeclaredMethods();
                        //item.canEquip(null,null,null);
                        //item.canPerformAction(item.getDefaultInstance(), ItemAbilities.AXE_DIG);
                        //Block block;
                        //block.getToolModifiedState(null,null,null,null);
                    });
                }
            });
        }

        GET_ITEM_SHIELD_COOLDOWN.register(new MiapiEvents.CooldownAttackingWeaponGatherEvent() {
            @Override
            public void durability(MutableInt cooldown, ItemStack attacking, ItemStack shield, LivingEntity defender, Entity attacker) {
                if (
                        cooldown.getValue() == 0 &&
                        attacker instanceof LivingEntity livingAttacker &&
                        (attacking.canDisableShield(shield, defender, livingAttacker) ||
                         attacking.canDisableShield(Items.SHIELD.getDefaultInstance(), defender, livingAttacker))) {
                    cooldown.setValue(100);
                }
            }
        }, -1);
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
        public static void register(RegisterEvent event) {
            event.register(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.key(), Miapi.id("global_loot_mod"), () -> MiapiGlobalLootModifier.CODEC);
        }

        @SubscribeEvent
        static void register(RegisterGameTestsEvent event) {

        }


        @SubscribeEvent
        public static void addPackFinders(AddPackFindersEvent event) {
            RegistryInventory.LOADABLE_DATAPACK_REGISTRY.getFlatMap().forEach((id, data) -> {
                if (event.getPackType() == PackType.SERVER_DATA) {
                    ResourceLocation packLocation = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "resourcepacks/" + id.getPath());
                    Optional<? extends ModContainer> info = ModList.get().getModContainerById(packLocation.getNamespace());
                    if (info.isEmpty()) {
                        Miapi.LOGGER.error("could not find " + id.getNamespace() + " mod container");
                        return;
                    }

                    IModInfo modInfo = info
                            .orElseThrow(() -> new IllegalArgumentException("Mod not found: " + packLocation.getNamespace())).getModInfo();

                    var resourcePath = modInfo.getOwningFile().getFile().findResource(packLocation.getPath());

                    var version = modInfo.getVersion();

                    var pack = Pack.readMetaAndCreate(
                            new PackLocationInfo(packLocation.toString(), data.forgeName(), data.defaultEnabled() ? PackSource.DEFAULT : PackSource.FEATURE, Optional.of(new KnownPack("neoforge", "mod/" + packLocation, version.toString()))),
                            BuiltInPackSource.fromName((path) -> new PathPackResources(path, resourcePath)),
                            PackType.SERVER_DATA,
                            new PackSelectionConfig(false, Pack.Position.TOP, false));
                    if (pack != null) {
                        event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
                    } else {
                        Miapi.LOGGER.error("could not register internal optional datapack from miapi!");
                    }
                }
            });
            ReloadEvents.MOD_IDS_TO_SCAN.forEach(targetModId -> {
                var modOpt = ModList.get().getModContainerById(targetModId);

                if (modOpt.isEmpty()) {
                    Miapi.LOGGER.warn("Mod {} not found for datapack scan", targetModId);
                    return;
                }

                var modInfo = modOpt.get().getModInfo();
                var modFile = modInfo.getOwningFile().getFile();

                var root = modFile.findResource("resourcepacks");

                if (!Files.exists(root)) return;
                try (Stream<Path> paths = Files.list(root)) {
                    paths.forEach(packDir -> {
                        if (!Files.isDirectory(packDir)) return;
                        String packName = packDir.getFileName().toString();
                        ResourceLocation packId = ResourceLocation.fromNamespaceAndPath(targetModId, packName);
                        var packInfo = new PackLocationInfo(
                                DatapackHolder.getDefaultName(packDir, packId.toString()),
                                Component.literal(packName),
                                DatapackHolder.shouldEnableByDefault(packDir) ? PackSource.BUILT_IN : PackSource.FEATURE,
                                Optional.of(new KnownPack("neoforge", "mod/" + packId, modInfo.getVersion().toString()))
                        );
                        var resources = BuiltInPackSource.fromName(path -> new PathPackResources(path, packDir));
                        if (Files.exists(packDir.resolve("data"))) {
                            var pack = Pack.readMetaAndCreate(
                                    packInfo,
                                    resources,
                                    PackType.SERVER_DATA,
                                    new PackSelectionConfig(false, Pack.Position.TOP, false)
                            );

                            if (pack != null) {
                                event.addRepositorySource(consumer -> consumer.accept(pack));
                            }
                        }
                        if (Files.exists(packDir.resolve("assets"))) {
                            var pack = Pack.readMetaAndCreate(
                                    packInfo,
                                    resources,
                                    PackType.CLIENT_RESOURCES,
                                    new PackSelectionConfig(false, Pack.Position.TOP, false)
                            );
                            if (pack != null) {
                                event.addRepositorySource(consumer -> consumer.accept(pack));
                            }
                        }
                    });
                } catch (IOException e) {
                    Miapi.LOGGER.error("Failed scanning resourcepacks for mod {}", targetModId, e);
                }
            });
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
            List<ModelResourceLocation> ids = RegistryInventory.MODULAR_ITEMS.getFlatMap().keySet().stream().map(ModelResourceLocation::inventory).toList();
            ModelManager.textureGetter = registerAdditional.getTextureGetter();
            ids.forEach(id -> {
                registerAdditional.getModels().put(id, new ItemBakedModelReplacement() {
                    @Override
                    public boolean isGui3d() {
                        return false;
                    }

                    public boolean usesBlockLight() {
                        return false;
                    }
                });
            });
            setupAttributes();
        }

        @SubscribeEvent
        public static void registerKeybinds(RegisterKeyMappingsEvent registerAdditional) {
            MiapiClient.KEY_BINDINGS.addCallback(registerAdditional::register);
        }

        @SubscribeEvent
        public static void setupClientItem(RegisterClientExtensionsEvent itemExtention) {
            RegistryInventory.MODULAR_ITEMS.addCallback((item -> {
                itemExtention.registerItem(new IClientItemExtensions() {
                    public Map<ItemStack, ModelWithHumanModel> cache = new WeakHashMap<>();

                    @Override
                    public @Nullable Font getFont(ItemStack stack, FontContext context) {
                        return IClientItemExtensions.super.getFont(stack, context);
                    }


                    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                                Minecraft.getInstance().getEntityModels()) {
                            public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
                                //
                                //Lighting.setupFor3DItems();
                                //Lighting.setupForFlatItems();
                                if (displayContext.equals(ItemDisplayContext.GUI)) {
                                    //Lighting.setupForFlatItems();
                                    //packedLight = 15728880;
                                    //test += 100;
                                    //packedLight = test;
                                    //packedLight = (int)Long.parseLong("0F0080",16);
                                    //packedOverlay = OverlayTexture.NO_OVERLAY;
                                    //poseStack.translate(0.0f, 0.0F, 0.0F);
                                    //poseStack.last().transformNormal(new Vector3f(-1, -1, -1), new Vector3f(0, -1, 0));
                                }
                                //Lighting.setupForFlatItems();
                                MiapiModel model = MiapiItemModel.getItemModel(stack);
                                if (model != null) {
                                    model.render(new MiapiModel.RenderContext(
                                            null,
                                            poseStack,
                                            stack,
                                            displayContext,
                                            0.0f,
                                            buffer,
                                            null,
                                            packedLight,
                                            packedOverlay
                                    ));
                                }
                                if (buffer instanceof MultiBufferSource.BufferSource multiBufferSource) {
                                    //multiBufferSource.endBatch();
                                }
                                //Lighting.setupFor3DItems();
                                //Lighting.setupFor3DItems();
                            }
                        };
                    }


                    public Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                        if (VisualModularItem.isVisualModularItem(itemStack)) {
                            //Miapi.LOGGER.info("rendering armor model " + equipmentSlot.getName());
                            cache.computeIfAbsent(itemStack, (i) -> new ModelWithHumanModel((a) -> RenderType.armorEntityGlint()) {
                                @Override
                                public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
                                    if (getHumanoidModel() != null && ForgeModel.source != null) {
                                        ArmorModelManager.renderArmorPiece(
                                                poseStack,
                                                ForgeModel.source,
                                                packedLight,
                                                equipmentSlot,
                                                itemStack,
                                                livingEntity,
                                                this.getHumanoidModel(),
                                                this.getHumanoidModel());
                                    }
                                }
                            });
                            var model = cache.get(itemStack);
                            if (model != null) {
                                model.humanoidModel = original;
                                return model;
                            }
                        }
                        return IClientItemExtensions.super.getGenericArmorModel(livingEntity, itemStack, equipmentSlot, original);
                    }

                    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                        if (VisualModularItem.isVisualModularItem(itemStack)) {
                            //Miapi.LOGGER.info("rendering armor model " + equipmentSlot.getName());
                            cache.computeIfAbsent(itemStack, (i) -> new ModelWithHumanModel((a) -> RenderType.armorEntityGlint()) {
                                @Override
                                public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
                                    if (getHumanoidModel() != null && ForgeModel.source != null) {
                                        ArmorModelManager.renderArmorPiece(
                                                poseStack,
                                                ForgeModel.source,
                                                packedLight,
                                                equipmentSlot,
                                                itemStack,
                                                livingEntity,
                                                this.getHumanoidModel(),
                                                this.getHumanoidModel());
                                    }
                                }
                            });
                            var model = cache.get(itemStack);
                            if (model != null) {
                                model.humanoidModel = original;
                                return model.humanoidModel;
                            }
                        }
                        return IClientItemExtensions.super.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
                    }
                }, item);
            }));
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
            PlayerEntity playerEntity = MinecraftClient.getInstRance().player;
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
            if (MiapiConfig.getClientConfig().shieldingArmor.respectHealth) {
                startY -= (healthAbsorptionTotal - 1) * numHearts;
            }
            if (MiapiConfig.getClientConfig().shieldingArmor.respectArmor && playerEntity.getArmor() > 0) {
                startY -= 10;
            }
            startY -= MiapiConfig.getClientConfig().shieldingArmor.otherOffests * 10;
            startY -= MiapiConfig.getClientConfig().shieldingArmor.attributesSingleLine.stream()
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