package smartin.miapi.events;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.Event;
import net.minecraft.client.gui.DrawContext;

public class ClientEvents {
    public static final Event<HudRender> HUD_RENDER = PrioritizedEvent.createLoop();

    public interface HudRender {
        void render(DrawContext drawContext, float deltaTick);
    }
}
