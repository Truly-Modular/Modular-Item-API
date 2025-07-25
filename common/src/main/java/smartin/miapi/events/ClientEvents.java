package smartin.miapi.events;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.Event;
import net.minecraft.client.gui.GuiGraphics;

public class ClientEvents {
    public static final Event<HudRender> HUD_RENDER = PrioritizedEvent.createLoop();
    public static final Event<Register> STAT_WIDGET_REGISTRATION = PrioritizedEvent.createLoop();

    public interface HudRender {
        void render(GuiGraphics drawContext, float deltaTick);
    }

    public interface Register {
        void register();
    }
}
