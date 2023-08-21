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
 * A Config item for strings
 */
public class StringConfigItem extends ConfigItem<String> {
    public StringConfigItem(String name, String defaultValue, String details) {
        super(name, defaultValue, details);
    }

    public StringConfigItem(String name, String defaultValue, String details, @Nullable Consumer<ConfigItem<String>> onChange) {
        super(name, defaultValue, details, onChange);
    }

    @Override
    public void fromJson(JsonElement element) {
        this.value = element.getAsString();
    }

    @Override
    public void toJson(JsonObject object) {
        object.addProperty(this.name, this.value);
    }

    @Override
    public <T> boolean isValidType(Class<T> clazz) {
        return clazz == String.class;
    }

    @Override
    public String getCommandValue() {
        return this.value;
    }
}
