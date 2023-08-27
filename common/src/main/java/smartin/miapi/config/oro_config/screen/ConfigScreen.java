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

package smartin.miapi.config.oro_config.screen;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import smartin.miapi.config.oro_config.Config;
import smartin.miapi.config.oro_config.ConfigItem;
import smartin.miapi.config.oro_config.ConfigItemGroup;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class allows for the easy addition of a Mod Menu config screen to your
 * mod. The abstract modifier is so that your {@link ConfigScreen} can be
 * used as a entry point for modmenu, as you need to set the config in the
 * constructor for this to work. <br>
 * <br>
 * Add this to your entrypoint list in {@code fabric.mod.json}: <br>
 * <code>
 * "modmenu" : [ <br>
 * &emsp;"your.package.structure.YourModMenuConfigScreen" <br>
 * ]
 * </code>
 *
 * @author Eli Orona
 */
public abstract class ConfigScreen {

    /**
     * The config for the screen
     */
    protected final Config config;

    /**
     * Creates a new {@link ConfigScreen}
     *
     * @param config The config
     */
    public ConfigScreen(Config config) {
        this.config = config;
    }

    protected ConfigCategory createCategory(ConfigBuilder builder, String categoryName) {
        return builder.getOrCreateCategory(Text.translatable(categoryName));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected AbstractConfigListEntry<?> createConfigItem(ConfigItem<?> ci, ConfigEntryBuilder entryBuilder, String superGroupName) {
        if (ci instanceof ConfigItemGroup) {
            List<AbstractConfigListEntry> subItems = ((ConfigItemGroup) ci).getConfigs().stream().map(configItem -> createConfigItem(configItem, entryBuilder, superGroupName + "." + ci.getName())).collect(Collectors.toList());
            SubCategoryBuilder groupCategory = entryBuilder.startSubCategory(Text.translatable(superGroupName + "." + ci.getName()), subItems);
            return groupCategory.build();
        }
        return ConfigScreenBuilders.getEntryBuilder(ci).getConfigEntry((ConfigItem) ci, entryBuilder, config);
    }

    /**
     * Creates a config screen
     *
     * @param parent The parent screen
     * @return A new screen
     */
    @SuppressWarnings("rawtypes")
    public Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("config." + config.getID()));
        builder.setSavingRunnable(config::saveConfigToFile);

        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

        config.getConfigs().forEach(group -> {
            ConfigCategory groupCategory = createCategory(builder, "config." + config.getID() + "." + group.getName());
            group.getConfigs().forEach(configItem -> {
                AbstractConfigListEntry entry = createConfigItem(configItem, entryBuilder, group.getName());
                groupCategory.addEntry(entry);
            });
        });

        return builder.build();
    }
}
