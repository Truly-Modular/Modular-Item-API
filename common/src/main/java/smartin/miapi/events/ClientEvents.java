package smartin.miapi.events;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.SpriteColorer;

public class ClientEvents {
    public static final Event<HudRender> HUD_RENDER = PrioritizedEvent.createLoop();
    public static final Event<Register> STAT_WIDGET_REGISTRATION = PrioritizedEvent.createLoop();
    public static final Event<ClientTick> CLIENT_TICK = PrioritizedEvent.createLoop();
    public static final Event<ResolveColorProvider> RESOLVE_COLOR_PROVIDER = PrioritizedEvent.createLoop();

    public interface HudRender {
        void render(GuiGraphics drawContext, float deltaTick);
    }

    public interface Register {
        void register();
    }

    public interface ClientTick {
        void register(Minecraft client);
    }

    public interface ResolveColorProvider {
        void adjustColorProvider(MutableValue<SpriteColorer> reference, Material material, ItemStack stack, ItemDisplayContext displayMode);
    }

    public static final class MutableValue<T> {
        private T value;

        public MutableValue(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}