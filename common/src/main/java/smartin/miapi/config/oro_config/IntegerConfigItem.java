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
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A Config item for integers
 */
public class IntegerConfigItem extends ConfigItem<Integer> {
    protected int min = Integer.MIN_VALUE;
    protected int max = Integer.MAX_VALUE;

    public IntegerConfigItem(String name, Integer defaultValue, String details) {
        super(name, defaultValue, details);
    }

    public IntegerConfigItem(String name, Integer defaultValue, String details, @Nullable Consumer<ConfigItem<Integer>> onChange) {
        super(name, defaultValue, details, onChange);
    }

    public IntegerConfigItem(String name, Integer defaultValue, String details, @Nullable Consumer<ConfigItem<Integer>> onChange, int max) {
        super(name, defaultValue, details, onChange);
        this.max = max;
    }

    public IntegerConfigItem(String name, Integer defaultValue, String details, @Nullable Consumer<ConfigItem<Integer>> onChange, int min, int max) {
        super(name, defaultValue, details, onChange);
        this.min = min;
        this.max = max;
    }

    @Override
    public void fromJson(JsonElement element) {
        this.value = element.getAsInt();
    }

    @Override
    public void toJson(JsonObject object) {
        object.addProperty(this.name, this.value);
    }

    @Override
    public <T> boolean isValidType(Class<T> clazz) {
        return clazz == Integer.class;
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(Integer.max(Integer.min(value, max), min));
    }

    @Override
    public String getCommandValue() {
        return this.value.toString();
    }

    /**
     * The min value for the config
     *
     * @return min
     */
    public int getMin() {
        return min;
    }

    /**
     * The max value for the config
     *
     * @return max
     */
    public int getMax() {
        return max;
    }
}
