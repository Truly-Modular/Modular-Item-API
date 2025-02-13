package smartin.miapi.modules.abilities.key;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.datapack.ReloadHelpers;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.network.modern.ModernNetworking;
import smartin.miapi.registries.MiapiRegistry;

import java.util.Map;

public class KeyBindManager {
    public static MiapiRegistry<MiapiBinding> BINDING_REGISTRY = MiapiRegistry.getInstance(MiapiBinding.class);
    public static ResourceLocation PACKET_ID = Miapi.id("c2s_binding_sync");
    public static StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(Miapi.ID_CODEC);


    public static void setup() {
        KeyBindFacet.KEY.cls();
        ReloadHelpers.registerReloadHandler(ReloadEvents.MAIN, "miapi/key_binding", true, (isClient) -> {
            //we cant remove keybindings
        }, (isClient, id, data, registryAccess) -> {
            id = minimizeID(id);
            if (BINDING_REGISTRY.get(id) == null) {
                BINDING_REGISTRY.register(id, MiapiBinding.decode(id, data));
            } else {
                MiapiBinding old = BINDING_REGISTRY.get(id);
                MiapiBinding newBinding = MiapiBinding.decode(id, data);
                old.itemInteraction = newBinding.itemInteraction;
                old.entityInteraction = newBinding.entityInteraction;
                old.blockInteraction = newBinding.blockInteraction;
                old.category = newBinding.category;
            }
            MiapiBinding binding = BINDING_REGISTRY.get(id);
            if (clientRegister(isClient, binding)) {
                MiapiConfig.INSTANCE.client.other.bindings.put(binding.id, binding);
            }
        }, 0.0f);
        ModernNetworking.registerC2SReceiver(PACKET_ID, PACKET_CODEC, (id, player, access) -> {
            if (id.toString().equals("miapi:none")) {
                ItemAbilityManager.serverKeyBindID.remove(player, id);
            } else {
                ItemAbilityManager.serverKeyBindID.put(player, id);
            }
        });
        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            if (isClient && MiapiConfig.clientConfigObject != null) {
                MiapiConfig.clientConfigObject.save();
            }
        });
    }

    public static void configLoad(Map<ResourceLocation, MiapiBinding> bindings) {
        bindings.forEach((key, binding) -> {
            binding.setID(key);
            if (BINDING_REGISTRY.get(binding.id) == null) {
                BINDING_REGISTRY.register(binding.id, binding);
            }
            if (clientRegister(Environment.isClient(), binding)) {
                MiapiConfig.INSTANCE.client.other.bindings.put(binding.id, binding);
            }
        });
    }

    public static boolean clientRegister(boolean isClient, MiapiBinding binding) {
        if (binding != null && isClient && !binding.isClientRegistered && MiapiClient.KEY_BINDINGS.get(binding.id) == null) {
            MiapiClient.KEY_BINDINGS.register(binding.id, binding.asKeyMapping());
            binding.isClientRegistered = true;
            return true;
        }
        return false;
    }

    public static void updateServerId(ResourceLocation id, Player player) {
        ModernNetworking.sendToServer(PACKET_ID, PACKET_CODEC, id, player.level().registryAccess());
    }

    public static ResourceLocation minimizeID(ResourceLocation resourceLocation) {
        return Miapi.id(resourceLocation.toString()
                .replace(".json", "")
                .replace("miapi/key_binding/", "")
        );
    }
}
