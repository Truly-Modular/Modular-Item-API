package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.ConduitRendererEntity;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class ConduitModelProperty extends CodecProperty<List<ConduitModelProperty.ConduitModelData>> {
    public static final ResourceLocation KEY = Miapi.id("conduit_model");
    public static ConduitModelProperty property;
    public static Codec<List<ConduitModelData>> CODEC = Codec.list(AutoCodec.of(ConduitModelData.class).codec());

    public ConduitModelProperty() {
        super(CODEC);
        property = this;
        MiapiItemModel.modelSuppliers.add((key,mode, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            getData(model).ifPresent(conduitModelData ->
                    conduitModelData.forEach(data ->
                            models.add(new ConduitRendererEntity(data.transform))));
            return models;
        });
    }

    @Override
    public List<ConduitModelData> merge(List<ConduitModelData> left, List<ConduitModelData> right, MergeType mergeType) {
        return ModuleProperty.mergeList(left, right, mergeType);
    }

    public static class ConduitModelData {
        @CodecBehavior.Optional
        public Transform transform = Transform.IDENTITY;

        public ConduitModelData() {

        }
    }
}
