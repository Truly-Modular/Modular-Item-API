package smartin.miapi.client.gui.crafting.crafter.replace;

import smartin.miapi.modules.ItemModule;

import java.util.Map;

public record CraftOption(ItemModule module, Map<String, String> data) {
}
