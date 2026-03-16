package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.EditOptionIcon;
import smartin.miapi.modules.edit_options.skins.gui.SkinGui;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SkinOptions implements EditOption {

    public static Map<ResourceLocation, Map<String, Skin>> skins = new HashMap<>();
    public static Map<String, SkinTab> tabMap = new HashMap<>();
    public static SkinTab defaultTab = new SkinTab();

    public SkinOptions() {
        defaultTab = SkinTab.fromJson(null);
        PropertyResolver.register(Miapi.id("skin"), (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                List<Skin> skins = Skin.getSkins(moduleInstance);
                for (Skin skin : skins) {
                    String[] parts = skin.path.split("/");
                    oldMap = skin.propertyHolder.applyHolder(oldMap,
                            Optional.of(
                                    Component.translatable("miapi.property.source.skin",
                                            Component.translatable(Miapi.MOD_ID + ".skin." + skin.modID + ".name." + parts[parts.length - 1]))));
                }
            }
            return oldMap;
        }, List.of(Miapi.id("synergy")));
        ReloadEvents.END.subscribe(((isClient, registryAccess, worker) -> {
            int size = 0;
            for (Map<String, Skin> skinMap : skins.values()) {
                size += skinMap.size();
            }
            Miapi.LOGGER.info("Loaded " + size + " Skins");
        }));
    }

    public static SkinTab getTag(String path) {
        return tabMap.getOrDefault(path, defaultTab);
    }

    public static void load(ResourceLocation path, String data) {
        JsonObject element = Miapi.gson.fromJson(data, JsonObject.class);
        Skin.fromJson(element).forEach(skin -> {
            skin.modID = path.getNamespace();
            Map<String, Skin> skinMap = skins.computeIfAbsent(skin.module.id(), (module) -> new HashMap<>());
            skinMap.put(skin.path, skin);
        });
    }

    public static void loadTabData(String data) {
        JsonObject element = Miapi.gson.fromJson(data, JsonObject.class);
        SkinTab tab = SkinTab.fromJson(element);
        tabMap.put(tab.path, tab);
    }

    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext context) {
        String skinString = buffer.readUtf();
        Map<String, Skin> moduleSkins = SkinOptions.skins.get(context.getInstance().getModule().id());
        if (moduleSkins != null) {
            Skin skin = moduleSkins.get(skinString);
            if (context.getInstance() != null) {
                if (skin == null || skin.type == null) {
                    Skin.writeSkins(context.getInstance(), List.of());
                }
                List<Skin> skins = new ArrayList<>(
                        Skin.getSkins(
                                        context
                                                .getInstance())
                                .stream()
                                .filter(s -> skin.type.equals("attachment") ||
                                             s != null &&
                                             !Objects.equals(s.type, skin.type)).toList());

                if (skins.contains(skin)) {
                    skins.remove(skin);
                    Skin.writeSkins(context.getInstance(), skins);
                } else {
                    skins.add(skin);
                    Skin.writeSkins(context.getInstance(), skins);
                }
                ItemStack stack = context.getItemstack().copy();

                context.getInstance().getRoot().writeToItem(stack);
                context.getInstance().clearCaches();
                return stack;
            } else {
                Miapi.LOGGER.error("could not set skin, no module found");
            }
        }
        return context.getItemstack();
    }

    @Override
    public boolean isVisible(EditContext context) {
        if (context.getInstance() != null) {
            ItemModule module = context.getInstance().getModule();
            if (module != null) {
                var foundSkins = skins
                        .get(module.id());
                return foundSkins != null;
            }
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext context) {
        return new SkinGui(x, y, width, height, context.getItemstack(), context.getInstance(), context::craft, context::preview);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339 + 32, 25 + 28 * 2, 512, 512, "miapi.ui.edit_option.hover.skin", this);
    }
}
