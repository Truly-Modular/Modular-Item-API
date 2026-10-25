package smartin.miapi.client.model.collision;

import org.joml.Matrix4f;
import smartin.miapi.modules.ModuleInstance;

public record ModulePos(ModuleInstance moduleInstance, Matrix4f pos) {
}
