package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.EditOptionIcon;
import smartin.miapi.modules.edit_options.skins.gui.SkinGui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SkinOptions implements EditOption {

    public static Map<ItemModule, Map<String, Skin>> skins = new HashMap<>();
    public static Map<String, SkinTab> tabMap = new HashMap<>();
    public static SkinTab defaultTab = new SkinTab();

    public SkinOptions() {
        defaultTab = SkinTab.fromJson(null);
        PropertyResolver.register(new Identifier("miapi", "skin"), (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                String skinKey = moduleInstance.moduleData.get("skin");
                Map<String, Skin> moduleSkins = skins.get(moduleInstance.module);
                if (skinKey != null && moduleSkins != null && moduleSkins.containsKey(skinKey)) {
                    moduleSkins.get(skinKey).propertyHolder.applyHolder(oldMap);
                }
            }
            return oldMap;
        }, List.of(new Identifier("miapi", "synergy")));
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "skins/module", skins, (isClient, path, data) -> {
            load(data);
        }, 1);
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "skins/tab", tabMap, (isClient, path, data) -> {
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
    public ItemStack preview(PacketByteBuf buffer, EditContext context) {
        String skin = buffer.readString();
        ModularItemCache.clearUUIDFor(context.getItemstack());
        context.getInstance().moduleData.put("skin", skin);
        context.getInstance().getRoot().writeToItem(context.getItemstack());
        ModularItemCache.clearUUIDFor(context.getItemstack());
        return context.getItemstack();
    }

    @Override
    public boolean isVisible(EditContext context) {
        return context.getInstance() != null &&
               skins.get(context.getInstance().module) != null;
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
