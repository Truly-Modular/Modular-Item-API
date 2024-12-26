package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.EntityMiapiModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialIcons;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntityModelProperty extends CodecProperty<List<EntityModelProperty.EntityModelData>> {
    public static final ResourceLocation KEY = Miapi.id("entity_model");
    public static EntityModelProperty property;

    public EntityModelProperty() {
        super(Codec.list(EntityModelData.CODEC));
        property = this;
        MiapiItemModel.modelSuppliers.add((key, mode, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            getData(model).ifPresent(entityModelDataList -> {
                entityModelDataList.forEach(entityModelData -> {
                    EntityType entityType = BuiltInRegistries.ENTITY_TYPE.get(entityModelData.id);
                    //this is entirely client side rendering, this *should* be save
                    Entity entity = entityType.create(Minecraft.getInstance().level);
                    if (entityModelData.nbt != null) {
                        entity.load(entityModelData.nbt);
                    }
                    EntityMiapiModel entityMiapiModel = new EntityMiapiModel(entity, entityModelData.transform);
                    entityMiapiModel.doTick = entityModelData.tick;
                    entityMiapiModel.fullBright = entityModelData.fullBright;
                    if (entityModelData.spin.isPresent()) {
                        entityMiapiModel.spinSettings = entityModelData.spin.get();
                    }
                    models.add(entityMiapiModel);
                });
            });
            return models;
        });
    }

    @Override
    public List<EntityModelData> merge(List<EntityModelData> left, List<EntityModelData> right, MergeType mergeType) {
        return ModuleProperty.mergeList(left, right, mergeType);
    }

    public static class EntityModelData {
        public ResourceLocation id;
        public CompoundTag nbt;
        public Transform transform = Transform.IDENTITY;
        public boolean tick = false;
        public boolean fullBright = false;
        public Optional<MaterialIcons.SpinSettings> spin;

        public static Codec<EntityModelData> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                        ResourceLocation.CODEC
                                .fieldOf("id")
                                .forGetter((data) -> data.id),
                        CompoundTag.CODEC.
                                optionalFieldOf("nbt", new CompoundTag())
                                .forGetter((data) -> data.nbt),
                        Transform.CODEC
                                .optionalFieldOf("transform", Transform.IDENTITY)
                                .forGetter((data) -> data.transform),
                        Miapi.FIXED_BOOL_CODEC
                                .optionalFieldOf("tick", false)
                                .forGetter((data) -> data.tick),
                        Miapi.FIXED_BOOL_CODEC
                                .optionalFieldOf("fullBright", false)
                                .forGetter((data) -> data.fullBright),
                        MaterialIcons.SpinSettings.CODEC
                                .optionalFieldOf("spin")
                                .forGetter((data) -> data.spin)
                ).apply(instance, (id, nbt, transform, tick, fullBright, spin) -> {
                    EntityModelData data = new EntityModelData();
                    data.id = id;
                    data.nbt = nbt;
                    data.transform = transform;
                    data.tick = tick;
                    data.fullBright = fullBright;
                    data.spin = spin;
                    return data;
                }));
    }
}
