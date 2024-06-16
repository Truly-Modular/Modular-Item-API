package smartin.miapi.modules.properties.mining;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.condition.MiningCondition;
import smartin.miapi.modules.properties.mining.mode.MiningMode;
import smartin.miapi.modules.properties.mining.modifier.MiningModifier;
import smartin.miapi.modules.properties.mining.shape.MiningShape;

import java.util.ArrayList;
import java.util.List;

public class MiningShapeJson {
    public MiningCondition miningCondition;
    public boolean sameBlock;
    public MiningMode miningMode;
    public MiningShape miningShape;
    public List<MiningModifier> modifiers = new ArrayList<>();

    public MiningShapeJson(JsonObject element, ModuleInstance moduleInstance) {
        JsonObject conditionJson = element.get("condition").getAsJsonObject();
        MiningCondition condition = MiningShapeProperty.miningConditionMap.get(conditionJson.get("type").getAsString());
        if (condition == null) {
            throw new IllegalArgumentException("Mining Condition " + conditionJson.get("type") + " doesn't exist");
        }
        this.miningCondition = condition.fromJson(conditionJson, moduleInstance);


        JsonObject shapeJson = element.get("shape").getAsJsonObject();
        MiningShape shape = MiningShapeProperty.miningShapeMap.get(shapeJson.get("type").getAsString());
        if (shape == null) {
            throw new IllegalArgumentException("Mining Shape " + shapeJson.get("type") + " doesn't exist");
        }
        this.miningShape = shape.fromJson(shapeJson, moduleInstance);


        sameBlock = MiningShapeProperty.getBoolean(element, "sameBlock", true);


        JsonObject modeJson = element.get("collapseMode").getAsJsonObject();
        MiningMode mode = MiningShapeProperty.miningModeMap.get(modeJson.get("type").getAsString());
        if (mode == null) {
            throw new IllegalArgumentException("Mining Mode " + modeJson.get("type") + " doesn't exist");
        }
        this.miningMode = mode.fromJson(modeJson, moduleInstance);

        if (element.has("modifiers")) {
            element.get("modifiers").getAsJsonObject().asMap().forEach((id, jsonElement) -> {
                MiningModifier modifier = MiningShapeProperty.miningModifierMap.get(id);
                if (modifier != null) {
                    this.modifiers.add(modifier.fromJson(jsonElement, moduleInstance));
                } else {
                    Miapi.LOGGER.info("Modifier " + id + " was not found and could not be resolved");
                }
            });
        }
    }

    public void execute(BlockPos pos, World level, ItemStack stack, ServerPlayerEntity player, Direction facing) {
        List<BlockPos> posList = miningCondition.trimList(level, pos, miningShape.getMiningBlocks(level, pos, facing));
        for (MiningModifier modifier : modifiers) {
            posList = modifier.adjustMiningBlock(level, pos, player, stack, posList);
        }
        miningMode.execute(posList, level, player, pos, stack);
    }
}
