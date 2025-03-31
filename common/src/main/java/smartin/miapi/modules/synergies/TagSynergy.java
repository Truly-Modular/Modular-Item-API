package smartin.miapi.modules.synergies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.PropertyHolder;
import smartin.miapi.modules.conditions.ConditionManager;

import java.util.ArrayList;

public class TagSynergy extends SynergyManager.Synergy {
    public static final ResourceLocation TYPE = Miapi.id("tag");
    private final String tag;

    public static final MapCodec<TagSynergy> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("tag").forGetter(synergy -> synergy.tag),
            ConditionManager.CONDITION_CODEC_DIRECT.fieldOf("condition").forGetter(synergy -> synergy.condition),
            PropertyHolder.MAP_CODEC.fieldOf("properties").forGetter(synergy -> synergy.holder)
    ).apply(instance, (tag, condition, holder) -> {
        TagSynergy synergy = new TagSynergy(tag);
        synergy.condition = condition;
        synergy.holder = holder;
        return synergy;
    }));

    public TagSynergy(String tag) {
        this.tag = tag;
        this.id = Miapi.id("tag", tag);
    }

    @Override
    protected ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public void register() {
        SynergyManager.tagSynergies.computeIfAbsent(tag, k -> new ArrayList<>()).add(this);
    }
}
