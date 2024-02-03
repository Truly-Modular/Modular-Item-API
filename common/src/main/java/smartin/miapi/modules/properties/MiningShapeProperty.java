package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
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
                List<Holder> holders = get(miningItem);
                HitResult hitResult = player.raycast(getBlockBreakDistance(player), 0, false);
                if (hitResult instanceof BlockHitResult blockHitResult) {
                    Direction facing = blockHitResult.getSide();
                    holders.stream().filter(holder ->
                                    holder.miningCondition.canMine(player, level, miningItem, pos, facing)).
                            forEach(holder ->
                                    holder.execute(pos, level, miningItem, player, facing));
                }
            }
            return EventResult.pass();
        });

        miningModeMap.put("instant", new MiningMode() {
            @Override
            public MiningMode fromJson(JsonObject object) {
                return this;
            }

            @Override
            public void execute(List<BlockPos> posList, World world, PlayerEntity player) {
                posList.forEach(blockPos -> world.breakBlock(blockPos, !player.isCreative(), player));
            }
        });

        miningConditionMap.put("always", new MiningCondition() {
            @Override
            public MiningCondition fromJson(JsonObject object) {
                return this;
            }

            @Override
            public List<BlockPos> trimList(World level, BlockPos original, List<BlockPos> positions) {
                return positions;
            }

            @Override
            public boolean canMine(PlayerEntity player, World level, ItemStack miningStack, BlockPos pos, Direction face) {
                return true;
            }
        });


        MiningLevelProperty.miningCapabilities.keySet().forEach(s -> {
            miningConditionMap.put(s, new MiningTypeCondition(s));
        });

        miningShapeMap.put("cube", new CubeMiningShape());
    }

    //TODO:update on port to 1.20.5, wont change for forge cause idk
    public double getBlockBreakDistance(PlayerEntity player) {
        return 10;
    }

    public List<Holder> get(ItemStack stack) {
        return ModularItemCache.get(stack, KEY, new ArrayList<>());
    }

    private static List<Holder> getCache(ItemStack stack) {
        JsonElement element = ItemModule.getMergedProperty(stack, property);
        if (element == null) {
            return new ArrayList<>();
        }
        return get(element);
    }

    public static List<Holder> get(JsonElement element) {
        return element.getAsJsonArray().asList().stream().map(subElement -> new Holder(subElement.getAsJsonObject())).toList();
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        get(data);
        return true;
    }

    public static class Holder {
        public MiningCondition miningCondition;
        public boolean sameBlock;
        public MiningMode miningMode;
        public MiningShape miningShape;
        public List<MiningModifier> modifiers = new ArrayList<>();

        public Holder(JsonObject element) {
            JsonObject conditionJson = element.get("condition").getAsJsonObject();
            MiningCondition condition = miningConditionMap.get(conditionJson.get("type").getAsString());
            if (condition == null) {
                throw new IllegalArgumentException("Mining Condition " + conditionJson.get("type") + " doesn't exist");
            }
            this.miningCondition = condition.fromJson(conditionJson);


            JsonObject shapeJson = element.get("shape").getAsJsonObject();
            MiningShape shape = miningShapeMap.get(shapeJson.get("type").getAsString());
            if (shape == null) {
                throw new IllegalArgumentException("Mining Shape " + shapeJson.get("type") + " doesn't exist");
            }
            this.miningShape = shape.fromJson(shapeJson);


            sameBlock = saveBoolean(element, "sameBlock", true);


            JsonObject modeJson = element.get("collapseMode").getAsJsonObject();
            MiningMode mode = miningModeMap.get(modeJson.get("type").getAsString());
            if (mode == null) {
                throw new IllegalArgumentException("Mining Mode " + modeJson.get("type") + " doesn't exist");
            }
            this.miningMode = mode.fromJson(modeJson);

            if (element.has("modifiers")) {
                Map<String, JsonElement> modifiers = new HashMap<>();
                element.get("modifiers").getAsJsonObject().asMap().forEach((id, jsonElement) -> {
                    MiningModifier modifier = miningModifierMap.get(id);
                    if (modifier != null) {
                        this.modifiers.add(modifier.fromJson(jsonElement));
                    } else {
                        Miapi.LOGGER.info("Modifier " + id + " was not found and could not be resolved");
                    }
                });
            }
        }

        public void execute(BlockPos pos, World level, ItemStack stack, PlayerEntity player, Direction facing) {
            List<BlockPos> posList = miningCondition.trimList(level, pos, miningShape.getMiningBlocks(level, pos, facing));
            for (MiningModifier modifier : modifiers) {
                posList = modifier.adjustMiningBlock(level, pos, player, stack, posList);
            }
            miningMode.execute(posList, level, player);
        }
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

    /**
     * This class adds a Condition for applying the Mining. this is supposed to filter for block or tool prior to mining
     */
    public interface MiningCondition {
        MiningCondition fromJson(JsonObject object);

        List<BlockPos> trimList(World level, BlockPos original, List<BlockPos> positions);

        boolean canMine(PlayerEntity player, World level, ItemStack miningStack, BlockPos pos, Direction face);

    }

    /**
     * Mining Modes are supposed to control how sth is mined.
     * They control the actual mining Part
     * They are not meant to filter the mining blocks
     * {@link MiningModifier} is meant to filter blocks from the shape
     */
    public interface MiningMode {
        MiningMode fromJson(JsonObject object);

        void execute(List<BlockPos> posList, World world, PlayerEntity player);

    }

    /**
     * Mining Shape is supposed to be the original scanner for the blocks.
     * Its the Implementation of the Algorythm for scanning blocks
     */
    public interface MiningShape {
        MiningShape fromJson(JsonObject object);

        List<BlockPos> getMiningBlocks(World world, BlockPos pos, Direction face);
    }

    /**
     * This is a post original scan Modifier of the Found Blocks
     * they are meant to filter after the {@link MiningShape} scanned for the block
     * use cautiously
     */
    public interface MiningModifier {
        MiningModifier fromJson(JsonElement object);

        List<BlockPos> adjustMiningBlock(World world, BlockPos pos, PlayerEntity player, ItemStack itemStack, List<BlockPos> blocks);
    }

    /**
     * This class adds a Cube Mining shape with
     * radius height, width, depth.
     * if height, width, depth are not defined they fallback onto radius
     */
    public static class CubeMiningShape implements MiningShape {
        int width;
        int height;
        int depth;

        @Override
        public MiningShape fromJson(JsonObject object) {
            int radius = getInteger(object, "radius", 1);
            CubeMiningShape cube = new CubeMiningShape();
            cube.width = getInteger(object, "width", radius);
            cube.height = getInteger(object, "height", radius);
            cube.depth = getInteger(object, "depth", radius);
            return cube;
        }

        @Override
        public List<BlockPos> getMiningBlocks(World world, BlockPos pos, Direction face) {
            List<Direction.Axis> axisList = new ArrayList<>(List.of(Direction.Axis.values()));
            axisList.remove(face.getAxis());
            Direction.Axis widthDirection = axisList.remove(0);
            Direction.Axis heightDirection = axisList.remove(0);
            List<BlockPos> list = new ArrayList<>(depth * height * width);
            for (int x = 0; x < depth; x++) {
                for (int y = 1; y <= width; y++) {
                    for (int z = 1; z <= height; z++) {
                        BlockPos pos1 = pos.mutableCopy();
                        pos1 = pos1.add(face.getVector().multiply(-x));
                        pos1 = pos1.offset(widthDirection, intHalfInverse(y));
                        pos1 = pos1.offset(heightDirection, intHalfInverse(z));
                        list.add(pos1);
                    }
                }
            }
            return list;
        }

        public static int intHalfInverse(int i) {
            if (i % 2 == 0) {
                return i / 2 * (-1);
            }
            return i / 2;
        }
    }

    public static class MiningTypeCondition implements MiningCondition {
        public String type;

        public MiningTypeCondition(String type) {
            this.type = type;
        }

        @Override
        public MiningCondition fromJson(JsonObject object) {
            return new MiningTypeCondition(type);
        }

        @Override
        public List<BlockPos> trimList(World level, BlockPos original, List<BlockPos> positions) {
            return positions.stream().filter(pos -> level.getBlockState(pos).isIn(MiningLevelProperty.miningCapabilities.get(type))).toList();
        }

        @Override
        public boolean canMine(PlayerEntity player, World level, ItemStack miningStack, BlockPos pos, Direction face) {
            return level.getBlockState(pos).isIn(MiningLevelProperty.miningCapabilities.get(type));
        }
    }
}
