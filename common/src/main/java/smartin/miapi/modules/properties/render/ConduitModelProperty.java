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

public class ConduitModelProperty extends CodecProperty<ConduitModelProperty.ConduitModelData> {
    public static final ResourceLocation KEY = Miapi.id("conduit_model");
    public static ConduitModelProperty property;
    public static Codec<ConduitModelData> CODEC = AutoCodec.of(ConduitModelData.class).codec();

    public ConduitModelProperty() {
        super(CODEC);
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            getData(model).ifPresent(conduitModelData -> {
                models.add(new ConduitRendererEntity(conduitModelData.transform));
            });
            return models;
        });
    }

    @Override
    public ConduitModelData merge(ConduitModelData left, ConduitModelData right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }

    public static class ConduitModelData {
        @CodecBehavior.Optional
        public Transform transform = Transform.IDENTITY;
    }
}
