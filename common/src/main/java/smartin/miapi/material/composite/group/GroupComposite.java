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
 * This Composite modifies the groups of a material by adding and removing specific groups.
 *
 * @header Group Modification Composite
 * @description_start
 * The GroupComposite allows you to modify the groups of a material by either clearing the existing groups or adjusting them by removing and adding specific groups.
 * You can choose to clear the groups, then remove certain groups and add others. If no groups are specified, the existing groups will be used.
 * @description_end
 * @path /data_types/composites/group_modification
 * @data clear: A boolean value that determines if the existing groups should be cleared before applying the modifications.
 * @data remove: A list of group names (String) to be removed from the existing groups.
 * @data toAdd: A list of group names (String) to be added to the existing groups.
 */
public record GroupComposite(boolean clear, List<String> remove, List<String> toAdd) implements Composite {
    public static ResourceLocation ID = Miapi.id("normal_group");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Miapi.FIXED_BOOL_CODEC.fieldOf("clear").forGetter((composite -> {
                        if (composite instanceof GroupComposite materialCopyComposite) {
                            return materialCopyComposite.clear();
                        }
                        return null;
                    })),
                    StatProvidersMap.Codec.STRING.listOf().fieldOf("to_add").forGetter((composite -> {
                        if (composite instanceof GroupComposite materialCopyComposite) {
                            return materialCopyComposite.remove();
                        }
                        return null;
                    })),
                    StatProvidersMap.Codec.STRING.listOf().fieldOf("to_remove").forGetter((composite -> {
                        if (composite instanceof GroupComposite materialCopyComposite) {
                            return materialCopyComposite.toAdd();
                        }
                        return null;
                    }))
            ).apply(instance, GroupComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            public List<String> getGroups(){
                return adjust(parent.getGroups());
            }
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
