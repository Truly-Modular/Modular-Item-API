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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Config is a holder class for a list of {@link ConfigItemGroup}. It's main
 * feature is to save and read from a file.
 *
 * @author Eli Orona
 */
public class Config {
    /**
     * The GSON formatter for the Config
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * The list of ConfigItemGroups for the config
     */
    private final List<ConfigItemGroup> configs;

    /**
     * The file to save and read the config from
     */
    private final File configFile;

    /**
     * The ID (often the mod id) for the config
     */
    private final String id;

    /**
     * Creates a new config
     *
     * @param configs    The list of {@link ConfigItemGroup} for the config
     * @param configFile The file to read and save from
     * @param id         The id of the config, should be the mod id
     */
    public Config(List<ConfigItemGroup> configs, File configFile, String id) {
        this.configs = configs;
        this.configFile = configFile;
        this.id = id;
    }

    /**
     * @return The list of {@link ConfigItemGroup} for this config
     */
    public List<ConfigItemGroup> getConfigs() {
        return configs;
    }

    /**
     * Reads the config from the file. If any changes are made and not saved this
     * will overwrite them.
     */
    public void readConfigFromFile() {
        try (FileInputStream stream = new FileInputStream(configFile)) {
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            String file = new String(bytes);
            JsonObject parsed = new JsonParser().parse(file).getAsJsonObject();
            for (ConfigItemGroup cig : configs) {
                cig.fromJson(parsed.get(cig.getName()));
            }
        } catch (FileNotFoundException e) {
            saveConfigToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets a the value at path
     *
     * @param path  The path to search
     * @param clazz The class of the type to get
     * @param <T>   The type to get
     * @return The value if it exists
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String path, Class<T> clazz) {
        String[] splitPath = path.split("\\.");

        ConfigItem<?> selectedItem = getConfigs().stream().filter(cig -> cig.name.equals(splitPath[0])).findFirst().get();
        try {
            LinkedList<String> paths = new LinkedList<>(Arrays.asList(splitPath));
            while (selectedItem instanceof ConfigItemGroup) {
                paths.removeFirst();
                selectedItem = ((ConfigItemGroup) selectedItem).getConfigs().stream().filter(ci -> ci.name.equals(paths.getFirst())).findFirst().get();
            }
        } catch (NoSuchElementException e) {
            System.err.printf("Path: %s does not exist\n", path);
            return null;
        }

        if (!selectedItem.isValidType(clazz)) {
            throw new IllegalArgumentException("Incorrect type " + clazz.getName() + " for " + path + ". Correct class is " + selectedItem.getValue().getClass().getSimpleName());
        }

        return ((ConfigItem<T>) selectedItem).getValue();
    }

    /**
     * Saves the current config to the file.
     */
    public void saveConfigToFile() {
        JsonObject object = new JsonObject();
        for (ConfigItemGroup c : configs) {
            c.toJson(object);
        }

        try (FileOutputStream stream = new FileOutputStream(configFile)) {
            stream.write(GSON.toJson(object).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a string representation of the config and all the sub configs
     */
    @Override
    public String toString() {
        return configFile.getName() + ": [" + configs.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    /**
     * @return The id of the config
     */
    public String getID() {
        return id;
    }
}
