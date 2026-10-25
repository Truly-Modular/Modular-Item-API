package smartin.miapi.entity.uslot;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public record SlotSelection(
        List<UEquipmentSlot> blacklist,
        Optional<List<UEquipmentSlot>> whitelist
) implements MergeAble<SlotSelection> {
    public static final Codec<UEquipmentSlot> SINGLE_CODEC = Codec.STRING.xmap(StringKey::resolve, UEquipmentSlot::getString);
    public static final Codec<List<UEquipmentSlot>> LIST_CODEC = Miapi.toListOrSimple(SINGLE_CODEC);
    public static final Codec<SlotSelection> OBJECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    LIST_CODEC.optionalFieldOf("blacklist", List.of())
                            .forGetter(SlotSelection::blacklist),
                    LIST_CODEC.optionalFieldOf("whitelist")
                            .forGetter(SlotSelection::whitelist)
            ).apply(instance, SlotSelection::new));

    public static final Codec<SlotSelection> CODEC = Codec.either(OBJECT_CODEC, LIST_CODEC).xmap(
            result -> result.map(selection -> selection,
                    slots -> new SlotSelection(List.of(), Optional.of(slots))),
            selection -> selection.blacklist().isEmpty() && selection.whitelist().isPresent()
                    ? Either.right(selection.whitelist().get())
                    : Either.left(selection));

    public static final SlotSelection ALL = new SlotSelection(List.of(), Optional.empty());
    public static final SlotSelection NONE = new SlotSelection(List.of(), Optional.of(List.of()));

    public SlotSelection {
        blacklist = List.copyOf(blacklist);
        whitelist = whitelist.map(List::copyOf);
    }

    public static void scanItems(LivingEntity entity, Consumer<UEquipmentSlot.EquipedItem> items) {
        UEquipmentSlot.PROVIDERS.getFlatMap().values()
                .forEach(provider -> provider.findItems(entity, items));
    }

    public static List<UEquipmentSlot.EquipedItem> scanItems(LivingEntity entity){
        List<UEquipmentSlot.EquipedItem> items = new ArrayList<>();
        scanItems(entity, items::add);
        return items;
    }

    public boolean matches(UEquipmentSlot slot) {
        return whitelist.map(slots -> slots.stream().anyMatch(s -> s.matches(slot))).orElse(true)
               && blacklist.stream().noneMatch(s -> s.matches(slot));
    }

    @Override
    public SlotSelection merge(SlotSelection left, SlotSelection right, MergeType mergeType) {
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return right;
        }

        List<UEquipmentSlot> nextBlacklist =
                MergeAble.mergeList(left.blacklist(), right.blacklist(), mergeType);

        Optional<List<UEquipmentSlot>> nextWhitelist;

        if (MergeType.REMOVE.equals(mergeType)) {
            if (left.whitelist().isEmpty()) {
                List<UEquipmentSlot> next = new ArrayList<>(nextBlacklist);
                right.whitelist().ifPresent(next::addAll);
                nextBlacklist = next;
                nextWhitelist = Optional.empty();
            } else if (right.whitelist().isPresent()) {
                nextWhitelist = Optional.of(MergeAble.mergeList(
                        left.whitelist().get(), right.whitelist().get(), mergeType
                ));
            } else {
                nextWhitelist = left.whitelist();
            }
        } else if (left.whitelist().isEmpty()) {
            nextWhitelist = right.whitelist();
        } else if (right.whitelist().isEmpty()) {
            nextWhitelist = left.whitelist();
        } else {
            nextWhitelist = Optional.of(MergeAble.mergeList(
                    left.whitelist().get(), right.whitelist().get(), mergeType
            ));
        }

        return new SlotSelection(nextBlacklist, nextWhitelist);
    }
}