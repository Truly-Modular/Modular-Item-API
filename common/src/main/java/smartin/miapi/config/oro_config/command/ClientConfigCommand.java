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

package smartin.miapi.config.oro_config.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandSource;
import smartin.miapi.config.oro_config.Config;
import smartin.miapi.config.oro_config.ConfigItemGroup;
import smartin.miapi.config.oro_config.screen.ConfigScreen;

import java.util.function.Predicate;

/**
 * A way to add client only commands for your config
 *
 * @param <S> A client command source
 */
public class ClientConfigCommand<S extends CommandSource> extends ConfigCommand<S> {
    public static Screen openScreen;

    /**
     * Creates a new ConfigCommand with the config
     *
     * @param config The config
     */
    public ClientConfigCommand(Config config) {
        super(config);

    }

    @Override
    public void register(CommandDispatcher<S> dispatcher, Predicate<S> usable) {
        LiteralArgumentBuilder<S> literalArgumentBuilder = LiteralArgumentBuilder.<S>literal(config.getID()).requires(usable).executes(this::listConfigGroups);

        for (ConfigItemGroup group : config.getConfigs()) {
            parseConfigItemGroupCommand(literalArgumentBuilder, group);
        }

        literalArgumentBuilder.then(LiteralArgumentBuilder.<S>literal("gui").executes(context -> {
            if(Platform.isModLoaded("cloth-config")){
                openScreen = new ConfigScreen(config) {
                }.createScreen(MinecraftClient.getInstance().currentScreen);
                return 1;
            }
            return 0;
        }));

        dispatcher.register(literalArgumentBuilder);
    }
}
