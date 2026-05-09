package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.BakedMiapiModel;
import smartin.miapi.client.model.module.dynamic.ChainModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.properties.render.baked.ModelData;
import smartin.miapi.modules.properties.render.baked.ModelManager;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

public class ChainModelProperty
        extends CodecProperty<List<ChainModelProperty.ChainModelData>> {

    public static final ResourceLocation KEY = Miapi.id("flail_model");
    public static ChainModelProperty property;

    public static final Codec<List<ChainModelData>> CODEC =
            Miapi.toListOrSimple(ChainModelData.CODEC);

    public ChainModelProperty() {
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
                for (ChainModelData data : list) {
                    data = data.copy();

                    List<ChainModel.ChainEntry> entries = new ArrayList<>();
                    List<ChainModelData.SegmentData> generated = data.generateSegments();

                    for (ChainModelData.SegmentData seg : generated) {

                        List<ModelData> usedModels =
                                (seg.modelData == null || seg.modelData.isEmpty())
                                        ? data.models
                                        : seg.modelData;

                        List<MiapiModel> baked = usedModels.stream()
                                .map(md -> BakedMiapiModel.createBaked(
                                        Objects.requireNonNull(
                                                ModelHolder.bakedModel(moduleInstance, md, stack)
                                        ),
                                        moduleInstance,
                                        stack,displayMode
                                ))
                                .toList();

                        entries.add(new ChainModel.ChainEntry(
                                seg.length.orElse(data.chainLength),
                                seg.collide,
                                seg.locked,
                                seg.gravity,
                                baked
                        ));
                    }

                    ChainModel model = new ChainModel(
                            entries,
                            data.segments,
                            data.transform,
                            data.debug
                    );

                    models.add(model);
                }
            });

            return models;
        });
    }

    @Override
    public boolean load(ResourceLocation moduleKey, JsonElement data, boolean isClient) throws Exception {
        if (isClient) {
            decode(data).forEach(ChainModelData::prepareModels);
        }
        return true;
    }

    @Override
    public List<ChainModelData> merge(
            List<ChainModelData> left,
            List<ChainModelData> right,
            MergeType mergeType
    ) {
        return MergeAble.mergeList(left, right, mergeType);
    }

    /* ------------------------------------------------------------
     *  Data Class
     * ------------------------------------------------------------ */

    public static class ChainModelData {

        public static final Codec<ChainModelData> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.FLOAT.optionalFieldOf("chainLength", 0.75f)
                                .forGetter(d -> d.chainLength),

                        Codec.INT.optionalFieldOf("segments", 8)
                                .forGetter(d -> d.segments),

                        Codec.FLOAT.optionalFieldOf("chainRadius", 0.03f)
                                .forGetter(d -> d.chainRadius),

                        Miapi.toListOrSimple(ModelData.CODEC)
                                .optionalFieldOf("models", List.of())
                                .forGetter(d -> d.models),

                        Codec.list(SegmentData.CODEC)
                                .optionalFieldOf("baseSegments", List.of())
                                .forGetter(d -> d.baseSegments),

                        Codec.unboundedMap(Codec.INT, SegmentData.CODEC)
                                .optionalFieldOf("overrides", Map.of())
                                .forGetter(d -> d.overrides),

                        Transform.CODEC.optionalFieldOf("transform", Transform.IDENTITY)
                                .forGetter(d -> d.transform),
                        Codec.BOOL.optionalFieldOf("debug", false)
                                .forGetter(d -> d.debug)

                ).apply(instance, ChainModelData::new));


        public float chainLength;
        public int segments;
        public float chainRadius;
        public List<ModelData> models;
        public List<SegmentData> baseSegments;
        public Map<Integer, SegmentData> overrides;
        public boolean debug;

        public Transform transform;

        public ChainModelData(
                float chainLength,
                int segments,
                float chainRadius,
                List<ModelData> models,
                List<SegmentData> baseSegments,
                Map<Integer, SegmentData> overrides,
                Transform transform,
                boolean debug
        ) {
            this.chainLength = chainLength;
            this.segments = segments;
            this.chainRadius = chainRadius;

            this.models = new ArrayList<>(models);

            this.baseSegments = baseSegments.isEmpty()
                    ? new ArrayList<>(List.of(new SegmentData()))
                    : new ArrayList<>(baseSegments);

            this.overrides = new HashMap<>(overrides);

            this.transform = transform;

            this.debug = debug;

            generateSegments();
        }

        public List<SegmentData> generateSegments() {
            List<SegmentData> result = new ArrayList<>(segments);

            // Safety fallback
            if (baseSegments == null || baseSegments.isEmpty()) {
                baseSegments = List.of(new SegmentData());
            }

            // Step 1: fill with repeating base pattern
            for (int i = 0; i < segments; i++) {
                SegmentData base =
                        baseSegments.get(i % baseSegments.size()).copy();
                result.add(base);
            }

            // Step 2: apply absolute overrides
            if (overrides != null) {
                for (var entry : overrides.entrySet()) {
                    int index = entry.getKey();
                    if (index < 0 || index >= segments) continue;

                    result.set(index, entry.getValue().copy());
                }
            }

            return result;
        }


        public void prepareModels() {
            prepareModelList(models);

            if (baseSegments != null) {
                for (SegmentData seg : baseSegments) {
                    prepareModelList(seg.modelData);
                }
            }

            if (overrides != null) {
                for (SegmentData seg : overrides.values()) {
                    prepareModelList(seg.modelData);
                }
            }
        }

        private static void prepareModelList(List<ModelData> list) {
            if (list == null) return;

            for (ModelData data : list) {
                if (data == null) continue;
                data.repair();
                ModelManager.loadModelsByPath(data.path);
            }
        }

        public ChainModelData copy() {
            List<SegmentData> baseCopy = new ArrayList<>();
            for (SegmentData s : baseSegments) {
                baseCopy.add(s.copy());
            }

            Map<Integer, SegmentData> overrideCopy = new HashMap<>();
            for (var e : overrides.entrySet()) {
                overrideCopy.put(e.getKey(), e.getValue().copy());
            }

            return new ChainModelData(
                    chainLength,
                    segments,
                    chainRadius,
                    new ArrayList<>(models),
                    baseCopy,
                    overrideCopy,
                    transform.copy(),
                    debug
            );
        }


        /* ------------------------------------------------------------
         *  Segment data
         * ------------------------------------------------------------ */

        public static class SegmentData {

            public static final Codec<SegmentData> CODEC =
                    RecordCodecBuilder.create(instance -> instance.group(
                            Codec.FLOAT.optionalFieldOf("length")
                                    .forGetter(s -> s.length),
                            Codec.BOOL.optionalFieldOf("collide", false)
                                    .forGetter(s -> s.collide),
                            Codec.BOOL.optionalFieldOf("locked", false)
                                    .forGetter(s -> s.locked),
                            Codec.FLOAT.optionalFieldOf("gravity", 1f)
                                    .forGetter(s -> s.gravity),
                            Miapi.toListOrSimple(ModelData.CODEC)
                                    .optionalFieldOf("model", List.of())
                                    .forGetter(s -> s.modelData)
                    ).apply(instance, SegmentData::new));

            public Optional<Float> length;
            public boolean collide;
            public boolean locked;
            public float gravity;
            public List<ModelData> modelData;

            public SegmentData() {
                this(Optional.empty(), false, false, 1f, new ArrayList<>());
            }

            public SegmentData(
                    Optional<Float> length,
                    boolean collide,
                    boolean locked,
                    float gravity,
                    List<ModelData> modelData
            ) {
                this.length = length;
                this.collide = collide;
                this.gravity = gravity;
                this.modelData = new ArrayList<>(modelData);
                this.locked = locked;
            }

            public SegmentData copy() {
                return new SegmentData(
                        length,
                        collide,
                        locked,
                        gravity,
                        new ArrayList<>(modelData)
                );
            }
        }
    }
}
