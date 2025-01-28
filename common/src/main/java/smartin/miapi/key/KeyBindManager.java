package smartin.miapi.key;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.Miapi;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.network.modern.ModernNetworking;
import smartin.miapi.registries.MiapiRegistry;

import static smartin.miapi.Miapi.gson;

public class KeyBindManager {
    public static MiapiRegistry<MiapiBinding> BINDING_REGISTRY = MiapiRegistry.getInstance(MiapiBinding.class);
    public static ResourceLocation PACKET_ID = Miapi.id("c2s_binding_sync");
    public static StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(Miapi.ID_CODEC);


    public static void setup() {
        KeyBindFacet.KEY.cls();
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/key_binding", true, (isClient) -> {
            //we cant remove keybindings
        }, (isClient, id, data, registryAccess) -> {
            if (BINDING_REGISTRY.get(id) == null) {
                BINDING_REGISTRY.register(id, MiapiBinding.decode(id, data));
            }
            MiapiBinding binding = BINDING_REGISTRY.get(id);
            if (binding != null && isClient && !binding.isClientRegistered) {
                MiapiClient.KEY_BINDINGS.register(binding.id, binding.asKeyMapping());
            }
        }, 0.0f);
        ModernNetworking.registerC2SReceiver(PACKET_ID, PACKET_CODEC, (id, player, access) -> {
            if (id.toString().equals("miapi:none")) {
                ItemAbilityManager.serverKeyBindID.remove(player, id);
            } else {
                ItemAbilityManager.serverKeyBindID.put(player, id);
            }
        });
    }

    public static void updateServerId(ResourceLocation id, Player player) {
        ModernNetworking.sendToServer(PACKET_ID, PACKET_CODEC, id, player.level().registryAccess());
    }

    public static class MiapiBinding {
        public ResourceLocation id;
        public String category;
        public int defaultScanCode;
        private boolean isClientRegistered = false;
        @Environment(EnvType.CLIENT)
        private KeyMapping mapping;
        public boolean lastDown = false;
        public boolean blockInteraction = false;
        public boolean entityInteraction = false;
        public boolean itemInteraction = true;
        public InteractionHand[] hands = new InteractionHand[]{InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND};
        private static Codec<MiapiBinding> CODEC = RecordCodecBuilder.create(
                instance ->
                        instance.group(
                                Codec.STRING.fieldOf("category").forGetter(binding -> ""),
                                Codec.INT.optionalFieldOf("scan_code", -1).forGetter(miapiBinding -> miapiBinding.defaultScanCode)
                        ).apply(instance, MiapiBinding::new));

        private MiapiBinding(String category, int scanCode) {
            this.category = category;
            this.defaultScanCode = scanCode;
        }

        public static MiapiBinding decode(ResourceLocation id, String data) {
            id = Miapi.id(id.toString().replace(".json", "")
                    //        .replace("miapi/key_binding/","")
            );
            JsonObject moduleJson = gson.fromJson(data, JsonObject.class);
            MiapiBinding binding = CODEC.decode(JsonOps.INSTANCE, moduleJson).getOrThrow().getFirst();
            binding.id = id;
            return binding;
        }

        @Environment(EnvType.CLIENT)
        public KeyMapping asKeyMapping() {
            if (mapping == null) {
                mapping = new KeyMapping(Miapi.toLangString(id), defaultScanCode, category);
            }
            return mapping;
        }
    }
}
