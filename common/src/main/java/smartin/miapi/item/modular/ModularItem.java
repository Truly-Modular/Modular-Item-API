package smartin.miapi.item.modular;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.HitResult;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.registries.MiapiRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModularItem extends Item {
    private static final MiapiRegistry<ItemModule> moduleRegistry = MiapiRegistry.getInstance(ItemModule.class);
    protected static final String moduleKey = "modules";
    protected static final String propertyKey = "rawProperties";

    public ModularItem() {
        super(new Item.Settings());
        ModularItemCache.setSupplier(moduleKey, itemStack -> {
            NbtCompound tag = itemStack.getNbt();
            try {
                String modulesString = tag.getString(moduleKey);
                Gson gson = new Gson();
                return gson.fromJson(modulesString, ItemModule.ModuleInstance.class);
            } catch (Exception e) {
                e.printStackTrace();
                Miapi.LOGGER.error("couldn't load Modules");
                return null;
            }
        });
        ModularItemCache.setSupplier(propertyKey, itemStack -> {
            return getUnmergedProperties((ItemModule.ModuleInstance) ModularItemCache.get(itemStack, moduleKey));
        });
        ReloadEvents.START.subscribe((isClient -> {
            moduleRegistry.clear();
        }));
    }

    public static ItemModule.ModuleInstance getModules(ItemStack stack) {
        ItemModule.ModuleInstance moduleInstance = (ItemModule.ModuleInstance) ModularItemCache.get(stack, moduleKey);
        if (moduleInstance == null || moduleInstance.module == null) {
            Miapi.LOGGER.warn("Item has Invalid Module setup - treating it like it has no modules");
            return new ItemModule.ModuleInstance(new ItemModule("empty", new HashMap<>()));
        }
        return moduleInstance;
    }

    protected static Map<String, List<JsonElement>> getUnmergedProperties(ItemModule.ModuleInstance modules) {
        Map<String, List<JsonElement>> unmergedProperties = new HashMap<>();
        for (ItemModule.ModuleInstance module : modules.subModules.values()) {
            module.getProperties().forEach((property, data) -> {
                String key = Miapi.moduleRegistry.findKey(property);
                unmergedProperties.getOrDefault(key, new ArrayList<>()).add(data);
            });
        }
        return unmergedProperties;
    }

    public static class Events {
        protected static List<RenderEvent> renderEvents = new ArrayList<>();
        protected static List<HitEvent> hitEvents = new ArrayList<>();

        private Events() {

        }

        public static void registerRenderEvent(RenderEvent event) {
            renderEvents.add(event);
        }

        public static void invokeRender(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
            renderEvents.forEach(event -> event.onEvent(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model));
        }

        public interface RenderEvent {
            void onEvent(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model);
        }

        public interface HitEvent {
            void onEvent(PlayerEntity attacker, PlayerEntity attacked, HitResult result);
        }
    }
}
