package smartin.miapi.editor;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.editor.syntax.EditorInterface;

import java.util.List;

public class EditorEvents {
    public static final PrioritizedEvent<EditorInterfaceEvent> EDITOR_INTERFACES = PrioritizedEvent.createLoop();

    public interface EditorInterfaceEvent {
        EventResult onGetInterfaces(EditorInterfaceData event);
    }

    public static class EditorInterfaceData {
        public final ResourceLocation resourceLocation;
        public final String filePath;
        public final List<EditorInterface> interfaces;

        public EditorInterfaceData(ResourceLocation resourceLocation, String filePath, List<EditorInterface> interfaces) {
            this.resourceLocation = resourceLocation;
            this.filePath = filePath;
            this.interfaces = interfaces;
        }
    }
} 