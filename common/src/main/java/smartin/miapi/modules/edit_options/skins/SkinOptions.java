package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SkinOptions implements EditOption {

    public static Map<ItemModule, Map<String, Skin>> skins = new HashMap<>();
    public static Map<String, SkinTab> tabMap = new HashMap<>();
    public static SkinTab defaultTab = new SkinTab();

    public SkinOptions() {
        defaultTab = SkinTab.fromJson(null);
        PropertyResolver.register(Miapi.id("skin"), (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                Optional<Skin> foundSkin = Skin.getSkin(moduleInstance);
                if (foundSkin.isPresent()) {
                    oldMap = foundSkin.get().propertyHolder.applyHolder(oldMap);
                }
            }
            return oldMap;
        }, List.of(Miapi.id("synergy")));
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/skins/module", skins, (isClient, path, data) -> {
            load(data);
        }, 1);
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/skins/tab", tabMap, (isClient, path, data) -> {
            loadTabData(data);
        }, 1);
        ReloadEvents.END.subscribe((isClient -> {
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

    public static void load(String data) {
        JsonObject element = Miapi.gson.fromJson(data, JsonObject.class);
        Skin.fromJson(element).forEach(skin -> {
            Map<String, Skin> skinMap = skins.computeIfAbsent(skin.module, (module) -> new HashMap<>());
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
        String skin = buffer.readUtf();
        Skin.writeSkin(context.getInstance(), skin);
        context.getInstance().getRoot().writeToItem(context.getItemstack());
        context.getInstance().clearCaches();
        return context.getItemstack();
    }

    @Override
    public boolean isVisible(EditContext context) {
        if(context.getInstance()!=null){
            ItemModule module = context.getInstance().module;
            var foundSkins = skins.get(module);
            return foundSkins != null;
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
