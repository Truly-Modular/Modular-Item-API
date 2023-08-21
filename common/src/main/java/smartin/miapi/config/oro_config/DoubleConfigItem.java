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
 * A Config item for doubles
 */
public class DoubleConfigItem extends ConfigItem<Double> {
    protected double min = Double.MIN_VALUE;
    protected double max = Double.MAX_VALUE;

    public DoubleConfigItem(String name, Double defaultValue, String details) {
        super(name, defaultValue, details);
    }

    public DoubleConfigItem(String name, Double defaultValue, String details, @Nullable Consumer<ConfigItem<Double>> onChange) {
        super(name, defaultValue, details, onChange);
    }

    public DoubleConfigItem(String name, Double defaultValue, String details, @Nullable Consumer<ConfigItem<Double>> onChange, double max) {
        super(name, defaultValue, details, onChange);
        this.max = max;
    }

    public DoubleConfigItem(String name, Double defaultValue, String details, @Nullable Consumer<ConfigItem<Double>> onChange, double min, double max) {
        super(name, defaultValue, details, onChange);
        this.min = min;
        this.max = max;
    }

    @Override
    public void fromJson(JsonElement element) {
        this.value = element.getAsDouble();
    }

    @Override
    public void toJson(JsonObject object) {
        object.addProperty(this.name, this.value);
    }

    @Override
    public <T> boolean isValidType(Class<T> clazz) {
        return clazz == Double.class;
    }

    @Override
    public void setValue(Double value) {
        super.setValue(Double.max(Double.min(value, max), min));
    }

    @Override
    public String getCommandValue() {
        return this.value.toString();
    }

    /**
     * The max value for the config
     *
     * @return max
     */
    public double getMin() {
        return min;
    }

    /**
     * The min value for the config
     *
     * @return min
     */
    public double getMax() {
        return max;
    }
}
