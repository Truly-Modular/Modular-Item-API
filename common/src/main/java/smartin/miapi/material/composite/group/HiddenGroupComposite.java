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

public record HiddenGroupComposite(boolean clear, List<String> remove, List<String> toAdd) implements Composite {
    public static ResourceLocation ID = Miapi.id("hidden_group");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Miapi.FIXED_BOOL_CODEC.fieldOf("clear").forGetter((composite -> {
                        if (composite instanceof HiddenGroupComposite materialCopyComposite) {
                            return materialCopyComposite.clear();
                        }
                        return null;
                    })),
                    StatProvidersMap.Codec.STRING.listOf().fieldOf("to_add").forGetter((composite -> {
                        if (composite instanceof HiddenGroupComposite materialCopyComposite) {
                            return materialCopyComposite.remove();
                        }
                        return null;
                    })),
                    StatProvidersMap.Codec.STRING.listOf().fieldOf("to_remove").forGetter((composite -> {
                        if (composite instanceof HiddenGroupComposite materialCopyComposite) {
                            return materialCopyComposite.toAdd();
                        }
                        return null;
                    }))
            ).apply(instance, HiddenGroupComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            public List<String> getGroups(){
                return adjust(parent.getGroups());
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
