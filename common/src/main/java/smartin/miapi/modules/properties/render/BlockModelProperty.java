package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.BlockRenderModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialIcons;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class BlockModelProperty extends CodecProperty<List<BlockModelProperty.BlockModelData>> {
    public static final ResourceLocation KEY = Miapi.id("block_model");
    public static BlockModelProperty property;
    public static Codec<List<BlockModelData>> CODEC = Codec.list(AutoCodec.of(BlockModelData.class).codec());

    public BlockModelProperty() {
        super(CODEC);
        property = this;
        MiapiItemModel.modelSuppliers.add((key,mode, model, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            getData(model).ifPresent(modelDataList -> {
                modelDataList.forEach(blockModelData -> {
                    Block block = BuiltInRegistries.BLOCK.get(blockModelData.id);
                    BlockState blockState = block.defaultBlockState();
                    if (blockModelData.nbt != null) {
                        blockState = BlockState.CODEC.parse(NbtOps.INSTANCE, blockModelData.nbt).result().orElse(blockState);
                    }
                    BlockRenderModel blockRenderModel = new BlockRenderModel(blockState, blockModelData.transform);
                    if (blockModelData.spin != null) {
                        blockRenderModel.spinSettings = blockModelData.spin;
                    }
                    models.add(blockRenderModel);
                });
            });
            return models;
        });
    }

    @Override
    public List<BlockModelData> merge(List<BlockModelData> left, List<BlockModelData> right, MergeType mergeType) {
        return ModuleProperty.mergeList(left, right, mergeType);
    }

    public class BlockModelData {
        public ResourceLocation id;
        @CodecBehavior.Optional
        public CompoundTag nbt;
        @CodecBehavior.Optional
        public Transform transform = Transform.IDENTITY;
        @CodecBehavior.Optional
        public MaterialIcons.SpinSettings spin = null;
    }
}
