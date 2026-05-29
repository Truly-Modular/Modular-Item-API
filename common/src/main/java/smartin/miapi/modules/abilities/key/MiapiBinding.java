package smartin.miapi.modules.abilities.key;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import smartin.miapi.Miapi;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import static smartin.miapi.Miapi.gson;

public class MiapiBinding {
    public ResourceLocation id;
    public ResourceLocation originalID;
    public String category;
    public int defaultScanCode;
    public boolean isClientRegistered = false;
    @Environment(EnvType.CLIENT)
    private KeyMapping mapping;
    public boolean lastDown = false;
    public boolean blockInteraction;
    public boolean entityInteraction;
    public boolean itemInteraction;
    public boolean nonItem = false;
    public InteractionHand[] hands = new InteractionHand[]{InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND};
    public static Codec<MiapiBinding> CODEC = RecordCodecBuilder.create(
            instance ->
                    instance.group(
                            Codec.STRING.fieldOf("category").forGetter(binding -> binding.category),
                            Miapi.FIXED_BOOL_CODEC.optionalFieldOf("block_interaction", false).forGetter(binding -> binding.blockInteraction),
                            Miapi.FIXED_BOOL_CODEC.optionalFieldOf("entity_interaction", false).forGetter(binding -> binding.entityInteraction),
                            Miapi.FIXED_BOOL_CODEC.optionalFieldOf("item_interaction", true).forGetter(binding -> binding.itemInteraction),
                            Miapi.FIXED_BOOL_CODEC.optionalFieldOf("no_item_interaction", false).forGetter(binding -> binding.nonItem),
                            Codec.INT.optionalFieldOf("scan_code", -1).forGetter(miapiBinding -> miapiBinding.defaultScanCode)
                    ).apply(instance, MiapiBinding::new));

    public MiapiBinding(String category, boolean blockInteraction, boolean entityInteraction, boolean itemInteraction,boolean nonItem, int scanCode) {
        this.category = category;
        this.defaultScanCode = scanCode;
        this.blockInteraction = blockInteraction;
        this.entityInteraction = entityInteraction;
        this.itemInteraction = itemInteraction;
    }

    public MiapiBinding(String category, int scanCode) {
        this.category = category;
        this.defaultScanCode = scanCode;
        this.blockInteraction = true;
        this.entityInteraction = true;
        this.itemInteraction = true;
        nonItem = true;
    }

    public static MiapiBinding decode(ResourceLocation id, String data) {
        JsonObject moduleJson = gson.fromJson(data, JsonObject.class);
        MiapiBinding binding = CODEC.decode(JsonOpsBooleanPatched.INSTANCE, moduleJson).getOrThrow().getFirst();
        binding.setID(id);
        return binding;
    }

    public void setID(ResourceLocation resourceLocation) {
        this.id = KeyBindManager.minimizeID(resourceLocation);
        this.originalID = resourceLocation;
    }

    @Environment(EnvType.CLIENT)
    public KeyMapping asKeyMapping() {
        if (mapping == null) {
            mapping = new KeyMapping(Miapi.toLangString(originalID), defaultScanCode, category);
        }
        return mapping;
    }
}
