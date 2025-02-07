package smartin.miapi.material.composite.group;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.composite.Composite;

import java.util.ArrayList;
import java.util.List;

/**
 * This Composite modifies the GUI groups of a material by adding and removing specific groups.
 *
 * @header GUI Group Modification Composite
 * @description_start
 * The GuiGroupComposite allows you to modify the GUI groups of a material by either clearing the existing groups or adjusting them by removing and adding specific groups.
 * You can choose to clear the GUI groups, then remove certain groups and add others. If no groups are specified, the existing groups will be used.
 * @description_end
 * @path /data_types/composites/gui_group_modification
 * @data clear: A boolean value that determines if the existing GUI groups should be cleared before applying the modifications.
 * @data remove: A list of GUI group names (String) to be removed from the existing GUI groups.
 * @data toAdd: A list of GUI group names (String) to be added to the existing GUI groups.
 */
public record GuiGroupComposite(boolean clear, List<String> remove, List<String> toAdd) implements Composite {
    public static ResourceLocation ID = Miapi.id("gui_group");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Miapi.FIXED_BOOL_CODEC.fieldOf("clear").forGetter((composite -> {
                        if (composite instanceof GuiGroupComposite materialCopyComposite) {
                            return materialCopyComposite.clear();
                        }
                        return null;
                    })),
                    StatProvidersMap.Codec.STRING.listOf().fieldOf("to_add").forGetter((composite -> {
                        if (composite instanceof GuiGroupComposite materialCopyComposite) {
                            return materialCopyComposite.remove();
                        }
                        return null;
                    })),
                    StatProvidersMap.Codec.STRING.listOf().fieldOf("to_remove").forGetter((composite -> {
                        if (composite instanceof GuiGroupComposite materialCopyComposite) {
                            return materialCopyComposite.toAdd();
                        }
                        return null;
                    }))
            ).apply(instance, GuiGroupComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            public List<String> getGuiGroups(){
                return adjust(parent.getGuiGroups());
            }
        };
    }

    public List<String> adjust(List<String> old) {
        if (clear()) {
            old = new ArrayList<>();
        } else {
            old = new ArrayList<>(old);
        }
        old.removeAll(remove());
        old.addAll(toAdd());
        return old;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
