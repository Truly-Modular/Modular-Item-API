package smartin.miapi.client;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.model.CustomColorProvider;
import smartin.miapi.mixin.ItemRendererAccessor;

public class ClientInit {

    public ClientInit() {
        Miapi.itemRegistry.addCallback(item -> {
            ((ItemRendererAccessor) MinecraftClient.getInstance().getItemRenderer()).color().register(new CustomColorProvider(), item);
        });
        ClientChatEvent.SEND.register((message, component) -> {
            if(Miapi.server!=null){
                Miapi.server.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                    serverPlayer.openHandledScreen(test());
                });
            }
            return EventResult.interrupt(false);
        });

    }

    public static NamedScreenHandlerFactory test() {
        Text text = Text.literal("test");
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return new CraftingScreenHandler(syncId, inventory);
        }, text);
    }
}
