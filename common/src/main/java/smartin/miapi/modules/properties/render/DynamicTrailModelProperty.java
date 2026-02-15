package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.codec.misc.ColorCodec;
import com.redpxnda.nucleus.util.Color;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.module.dynamic.trail.DynamicTrailModel;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

public class DynamicTrailModelProperty
        extends CodecProperty<List<DynamicTrailModelProperty.TrailData>> {

    public static final ResourceLocation KEY = Miapi.id("trail");
    public static DynamicTrailModelProperty property;

    public static final Codec<List<TrailData>> CODEC =
            Miapi.toListOrSimple(TrailData.CODEC);

    public DynamicTrailModelProperty() {
        super(CODEC);
        property = this;

        if (Platform.getEnv() == EnvType.CLIENT) {
            clientSetup();
        }
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup() {
        MiapiItemModel.modelSuppliers.add((key, displayMode, moduleInstance, stack) -> {
            List<MiapiModel> models = new ArrayList<>();

            getData(moduleInstance).ifPresent(list -> {
                for (TrailData data : list) {
                    ColorProvider colorProvider = ColorProvider.getProvider(data.colorProvider, stack, moduleInstance, TrimRenderer.TrimMode.NONE);
                    models.add(new DynamicTrailModel(
                            data.maxPoints,
                            data.pointLifetime,
                            data.sampleInterval,
                            data.thickness,
                            data.color,
                            data.debug,
                            data.texture,
                            data.transform,
                            colorProvider,
                            moduleInstance,
                            data.whiteList
                    ));
                }
            });

            return models;
        });
    }

    @Override
    public boolean load(ResourceLocation moduleKey, JsonElement data, boolean isClient) {
        return true;
    }

    @Override
    public List<TrailData> merge(
            List<TrailData> left,
            List<TrailData> right,
            MergeType mergeType
    ) {
        return MergeAble.mergeList(left, right, mergeType);
    }


    public static class TrailData {

        public static final Codec<TrailData> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.optionalFieldOf("maxPoints", 255)
                                .forGetter(d -> d.maxPoints),
                        Codec.FLOAT.optionalFieldOf("pointLifetime", 0.5f)
                                .forGetter(d -> d.pointLifetime),
                        Codec.FLOAT.optionalFieldOf("sampleInterval", 0.0f)
                                .forGetter(d -> d.sampleInterval),
                        Codec.FLOAT.optionalFieldOf("thickness", 0.02f)
                                .forGetter(d -> d.thickness),
                        ColorCodec.INSTANCE.optionalFieldOf("color", Color.WHITE)
                                .forGetter(d -> d.color),
                        Transform.CODEC.optionalFieldOf("transform", Transform.IDENTITY)
                                .forGetter(d -> d.transform),
                        ResourceLocation.CODEC.optionalFieldOf("texture", Miapi.id("item/trail"))
                                .forGetter(d -> d.texture),
                        Codec.BOOL.optionalFieldOf("debug", false)
                                .forGetter(d -> d.debug),
                        Codec.STRING.optionalFieldOf("color_provider", "material")
                                .forGetter((modelData) -> modelData.colorProvider),
                        ItemDisplayContext.CODEC.listOf().optionalFieldOf("display_context", List.of(
                                ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                                        ItemDisplayContext.GROUND
                                        ))
                                .forGetter((modelData) -> modelData.whiteList)

                ).apply(instance, TrailData::new));

        public int maxPoints;
        public float pointLifetime;
        public float sampleInterval;
        public float thickness;
        public com.redpxnda.nucleus.util.Color color;
        public Transform transform;
        public ResourceLocation texture;
        public boolean debug;
        public String colorProvider;
        public List<ItemDisplayContext> whiteList;

        public TrailData(
                int maxPoints,
                float pointLifetime,
                float sampleInterval,
                float thickness,
                com.redpxnda.nucleus.util.Color color,
                Transform transform,
                ResourceLocation texture,
                boolean debug,
                String colorProvider,
                List<ItemDisplayContext> whiteList
        ) {
            this.maxPoints = maxPoints;
            this.pointLifetime = pointLifetime;
            this.sampleInterval = sampleInterval;
            this.thickness = thickness;
            this.color = color;
            this.transform = transform;
            this.texture = texture;
            this.debug = debug;
            this.colorProvider = colorProvider;
            this.whiteList = whiteList;
        }
    }
}
