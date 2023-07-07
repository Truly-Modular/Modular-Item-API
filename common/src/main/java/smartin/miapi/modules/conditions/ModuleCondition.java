package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.io.IOException;
import java.util.List;
import java.util.Map;
@JsonAdapter(ModuleCondition.ModuleConditionJsonAdapter.class)
public interface ModuleCondition {

    boolean isAllowed(@Nullable ItemModule.ModuleInstance moduleInstance, @Nullable BlockPos tablePos, @Nullable PlayerEntity player, @Nullable  Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons);

    ModuleCondition load(JsonElement element);

    class ModuleConditionJsonAdapter extends TypeAdapter<ModuleCondition> {

        @Override
        public void write(JsonWriter jsonWriter, ModuleCondition moduleCondition) throws IOException {

        }

        @Override
        public ModuleCondition read(JsonReader jsonReader) throws IOException {
            return ConditionManager.get(Miapi.gson.fromJson(jsonReader,JsonElement.class));
        }
    }
}
