/*
 * MIT License
 *
 * Copyright (c) 2021 OroArmor (Eli Orona)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package smartin.miapi.config.oro_config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 * Extending {@link ConfigItem}, {@link ConfigItemGroup} can store multiple
 * {@link ConfigItem}s
 *
 * @author Eli Orona
 */
public class ConfigItemGroup extends ConfigItem<ConfigItem<?>> {

    /**
     * The list of {@link ConfigItem}
     */
    private final List<ConfigItem<?>> configs;

    /**
     * BAD CONSTRUCTOR ONLY FOR DEFAULT VALUE. DO NOT USE
     */
    private ConfigItemGroup() {
        super(null, null, null);
        configs = new ArrayList<>();
    }

    /**
     * Creates a new {@link ConfigItemGroup} with the list of configs and the name
     *
     * @param configs The list of configs
     * @param name    The name of this group
     */
    public ConfigItemGroup(List<ConfigItem<?>> configs, String name) {
        super(name, new ConfigItemGroup(), "");
        this.configs = configs;
    }

    @Override
    public void fromJson(JsonElement jsonConfigs) {
        JsonObject object = jsonConfigs.getAsJsonObject();
        for (Entry<String, JsonElement> entry : object.entrySet()) {
            for (ConfigItem<?> c : configs) {
                if (c.getName().equals(entry.getKey())) {
                    c.fromJson(entry.getValue());
                }
            }
        }
    }

    /**
     * @return The configs for this group
     */
    public List<ConfigItem<?>> getConfigs() {
        return configs;
    }

    /**
     * Converts the config items into json
     */
    @Override
    public void toJson(JsonObject superObject) {
        JsonObject object = new JsonObject();

        for (ConfigItem<?> c : configs) {
            c.toJson(object);
        }

        superObject.add(this.getName(), object);
    }

    @Override
    public String toString() {
        String string = getName() + ": [";
        StringJoiner joiner = new StringJoiner(", ");
        for (ConfigItem<?> config : configs) {
            String toString = config.toString();
            joiner.add(toString);
        }
        string += joiner.toString();
        return string + "]";
    }

    @Override
    public <T> boolean isValidType(Class<T> clazz) {
        return clazz == this.getClass();
    }

    @Override
    public String getCommandValue() {
        return null;
    }
}
