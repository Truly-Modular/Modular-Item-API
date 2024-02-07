package smartin.miapi.modules.properties.mining;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.mining.condition.AlwaysMiningCondition;
import smartin.miapi.modules.properties.mining.condition.MiningCondition;
import smartin.miapi.modules.properties.mining.condition.MiningTypeCondition;
import smartin.miapi.modules.properties.mining.mode.InstantMiningMode;
import smartin.miapi.modules.properties.mining.mode.MiningMode;
import smartin.miapi.modules.properties.mining.mode.StaggeredMiningMode;
import smartin.miapi.modules.properties.mining.modifier.MiningModifier;
import smartin.miapi.modules.properties.mining.modifier.SameBlockModifier;
import smartin.miapi.modules.properties.mining.shape.CubeMiningShape;
import smartin.miapi.modules.properties.mining.shape.MiningShape;
import smartin.miapi.modules.properties.mining.shape.VeinMiningShape;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Property Manages the complicated task of Mining Multiple Blocks when only one is mined
 * Area,Vein and other stuff
 */
public class MiningShapeProperty implements ModuleProperty {
    public static String KEY = "mining_shape";
    public static MiningShapeProperty property;
    public static Map<String, MiningCondition> miningConditionMap = new HashMap<>();
    public static Map<String, MiningShape> miningShapeMap = new HashMap<>();
    public static Map<String, MiningMode> miningModeMap = new HashMap<>();
    public static Map<String, MiningModifier> miningModifierMap = new HashMap<>();


    public MiningShapeProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, MiningShapeProperty::getCache);
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (!level.isClient() && !player.isSneaking()) {
                ItemStack miningItem = player.getMainHandStack();
                List<MiningShapeJson> miningShapeJsons = get(miningItem);
                HitResult hitResult = player.raycast(getBlockBreakDistance(player), 0, false);
                if (hitResult instanceof BlockHitResult blockHitResult) {
                    Direction facing = blockHitResult.getSide();
                    miningShapeJsons.stream().filter(miningShapeJson ->
                                    miningShapeJson.miningCondition.canMine(player, level, miningItem, pos, facing)).
                            forEach(miningShapeJson ->
                                    miningShapeJson.execute(pos, level, miningItem, player, facing));
                }
            }
            return EventResult.pass();
        });

        miningModeMap.put("instant", new InstantMiningMode());
        miningModeMap.put("staggered",new StaggeredMiningMode());

        miningModifierMap.put("require_same", new SameBlockModifier());

        miningConditionMap.put("always", new AlwaysMiningCondition());


        MiningLevelProperty.miningCapabilities.keySet().forEach(s -> miningConditionMap.put(s, new MiningTypeCondition(s)));

        miningShapeMap.put("cube", new CubeMiningShape());
        miningShapeMap.put("vein", new VeinMiningShape());
    }

    //TODO:update on port to 1.20.5, wont change for forge cause idk
    public double getBlockBreakDistance(PlayerEntity player) {
        return 10;
    }

    public List<MiningShapeJson> get(ItemStack stack) {
        return ModularItemCache.get(stack, KEY, new ArrayList<>());
    }

    private static List<MiningShapeJson> getCache(ItemStack stack) {
        JsonElement element = ItemModule.getMergedProperty(stack, property);
        if (element == null) {
            return new ArrayList<>();
        }
        return get(element);
    }

    public static List<MiningShapeJson> get(JsonElement element) {
        return element.getAsJsonArray().asList().stream().map(subElement -> new MiningShapeJson(subElement.getAsJsonObject())).toList();
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        get(data);
        return true;
    }

    public static boolean saveBoolean(JsonObject object, String element, boolean defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull() && json.isJsonPrimitive()) {
                return json.getAsBoolean();
            }
        }
        return defaultValue;
    }

    public static int getInteger(JsonObject object, String element, int defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull() && json.isJsonPrimitive()) {
                return json.getAsInt();
            }
        }
        return defaultValue;
    }

}
