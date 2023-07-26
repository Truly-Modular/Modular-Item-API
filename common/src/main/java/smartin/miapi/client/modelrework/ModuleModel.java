package smartin.miapi.client.modelrework;

import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public class ModuleModel {

    List<Pair<Matrix4f, MiapiModel>> models;

    Map<String, List<Pair<Matrix4f, MiapiModel>>> otherModels;
}
