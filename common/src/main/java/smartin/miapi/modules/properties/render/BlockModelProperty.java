package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.module.BlockRenderModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialIcons;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockModelProperty extends CodecProperty<List<BlockModelProperty.BlockModelData>> {
    public static final ResourceLocation KEY = Miapi.id("block_model");
    public static BlockModelProperty property;

    public static final Codec<BlockModelData> BLOCK_MODEL_DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(BlockModelData::id),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(BlockModelData::nbt),
            Transform.CODEC.optionalFieldOf("transform", Transform.IDENTITY).forGetter(BlockModelData::transform),
            MaterialIcons.SpinSettings.CODEC.optionalFieldOf("spin").forGetter(BlockModelData::spin)
    ).apply(instance, BlockModelData::new));

    public static final Codec<List<BlockModelData>> CODEC = Miapi.toListOrSimple(BLOCK_MODEL_DATA_CODEC);

    public BlockModelProperty() {
        super(CODEC);
        property = this;

        MiapiItemModel.modelSuppliers.add((key, mode, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            getData(model).ifPresent(modelDataList -> {
                modelDataList.forEach(blockModelData -> {
                    Block block = BuiltInRegistries.BLOCK.get(blockModelData.id());
                    BlockState blockState = block.defaultBlockState();
                    if(blockModelData.nbt.isPresent()){
                        var result = BlockState.CODEC.parse(Miapi.BOOL_CORRECTED_OPS, blockModelData.nbt.get());
                        if(result.isSuccess()){
                            blockState = result.result().get();
                        }
                    }
                    BlockRenderModel blockRenderModel = new BlockRenderModel(blockState, blockModelData.transform());
                    blockModelData.spin().ifPresent(spin -> blockRenderModel.spinSettings = spin);
                    models.add(blockRenderModel);
                });
            });
            return models;
        });
    }

    @Override
    public List<BlockModelData> merge(List<BlockModelData> left, List<BlockModelData> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }

    public record BlockModelData(
            ResourceLocation id,
            Optional<CompoundTag> nbt,
            Transform transform,
            Optional<MaterialIcons.SpinSettings> spin
    ) {
    }
}
