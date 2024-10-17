package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringNBTWriteProperty implements ModuleProperty, CraftingProperty {
    public static StringNBTWriteProperty property;
    public static String KEY = "string-nbt";

    public StringNBTWriteProperty() {
        property = this;
    }


    public Map<String, NbtElement> customNBT(ItemStack itemStack) {
        Map<String, NbtElement> map = new HashMap<>();
        ItemModule.getModules(itemStack).allSubModules().forEach(moduleInstance -> {
            JsonElement data = moduleInstance.getProperties().get(property);
            if (data != null) {
                data.getAsJsonObject().asMap().forEach((key, json) -> {
                    try {
                        String string = resolveStatInString(json.getAsString(), moduleInstance);
                        map.put(key, NbtHelper.fromNbtProviderString(string));
                    } catch (CommandSyntaxException e) {
                        Miapi.LOGGER.warn("could not parse NBT", e);
                    }
                });
            }
        });
        return map;
    }

    public static String resolveStatInString(String input, ItemModule.ModuleInstance moduleInstance) {
        // Define the pattern to match |miapi ... end|
        Pattern pattern = Pattern.compile("\\|miapi(.*?)end\\|");
        Matcher matcher = pattern.matcher(input);

        // Create a StringBuilder to build the final output
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;

        while (matcher.find()) {
            // Append the part before the match
            result.append(input, lastIndex, matcher.start());

            // Get the content inside |miapi and end|
            String statExpression = matcher.group(1);

            // Resolve the statExpression using the StatResolver (this is a placeholder for your actual implementation)
            double resolvedValue = StatResolver.resolveDouble(statExpression, moduleInstance);

            // Append the resolved value to the result
            result.append(resolvedValue);

            // Update the last index to the end of the current match
            lastIndex = matcher.end();
        }

        // Append the remaining part of the input string
        result.append(input.substring(lastIndex));

        return result.toString();
    }

    public boolean shouldExecuteOnCraft(@Nullable ItemModule.ModuleInstance module, ItemModule.ModuleInstance root, ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        Map<String, NbtElement> oldData = customNBT(old);
        Map<String, NbtElement> newData = customNBT(old);
        for (String key : oldData.keySet()) {
            crafting.removeSubNbt(key);
        }
        for (String key : newData.keySet()) {
            crafting.setSubNbt(key, oldData.get(key));
        }

        return crafting;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsJsonObject();
        return true;
    }
}
