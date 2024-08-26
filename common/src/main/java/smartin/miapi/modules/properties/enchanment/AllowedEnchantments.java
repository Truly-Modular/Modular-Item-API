package smartin.miapi.modules.properties.enchanment;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.NamedAccessor;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Optional;

public class AllowedEnchantments extends CodecProperty<AllowedEnchantments.AllowedEnchantsData> {
    public static final ResourceLocation KEY = Miapi.id("enchantments");
    public static AllowedEnchantments property;

    public AllowedEnchantments() {
        super(AllowedEnchantsData.CODEC);
        property = this;
    }

    public static boolean isAllowed(ItemStack itemStack, Holder<Enchantment> enchantment, boolean oldValue) {
        Optional<AllowedEnchantsData> optional = property.getData(itemStack);
        return optional.map(allowedEnchantsData -> allowedEnchantsData.isAllowed(enchantment).orElse(oldValue)).orElse(oldValue);
    }

    @Override
    public AllowedEnchantsData merge(AllowedEnchantsData left, AllowedEnchantsData right, MergeType mergeType) {
        return new AllowedEnchantsData(
                ModuleProperty.mergeList(left.allowed(), right.allowed(), mergeType),
                ModuleProperty.mergeList(left.forbidden(), right.forbidden(), mergeType)
        );
    }

    public record AllowedEnchantsData(List<ResourceLocation> allowed,
                                      List<ResourceLocation> forbidden) {
        public static Codec<Either<Holder<Enchantment>, ResourceLocation>> codec = Codec.either(
                Enchantment.CODEC,
                ResourceLocation.CODEC
        );
        public static Codec<AllowedEnchantsData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                        Codec.list(ResourceLocation.CODEC)
                                .optionalFieldOf("allowed", List.of())
                                .forGetter(AllowedEnchantsData::allowed),
                        Codec.list(ResourceLocation.CODEC)
                                .optionalFieldOf("forbidden", List.of())
                                .forGetter(AllowedEnchantsData::allowed)
                )
                .apply(instance, AllowedEnchantsData::new));


        Optional<Boolean> isAllowed(Holder<Enchantment> enchantment) {
            if (contains(enchantment, forbidden())) {
                return Optional.of(false);
            }
            if (contains(enchantment, allowed)) {
                return Optional.of(true);
            }
            return Optional.empty();
        }

        private boolean contains(Holder<Enchantment> enchantment, List<ResourceLocation> ids) {
            for (ResourceLocation id : ids) {
                if(enchantment.is(id)){
                    return true;
                }
                if (enchantment.value().definition().supportedItems() instanceof HolderSet.Named<Item> set) {
                    ResourceLocation tagID = ((NamedAccessor) set).getKey().location();
                    if (tagID.equals(id)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
