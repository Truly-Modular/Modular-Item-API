package smartin.miapi.modules.abilities.gun;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

/**
 * This property defines core gun attributes such as fire rate, damage, magazine size, and range.
 * It enables dynamic modification based on modules and supports merging strategies.
 *
 * @header Gun Context Property
 * @path /data_types/properties/gun/gun_context
 * @description_start
 * The Gun Context Property is used to define fundamental shooting mechanics in modular weapons.
 * It controls attributes such as:
 * - **Fire Rate**: Determines how quickly the gun fires.
 * - **Base Damage**: The default damage output per shot.
 * - **Magazine Size**: The total number of bullets before needing a reload.
 * - **Range**: Maximum effective shooting distance.
 * @description_end
 *
 * @data fire_rate: Fire rate of the weapon (Default: 1.0).
 * @data base_damage: Base damage per shot (Default: 5.0).
 * @data magazine_size: Number of shots before reloading (Default: 10).
 * @data range: Maximum hit detection range (Default: 100).
 */

public class GunContextProperty extends CodecProperty<GunContextProperty.GunContext> {
    public static final ResourceLocation KEY = Miapi.id("gun_context");
    public static GunContextProperty property;

    public GunContextProperty() {
        super(GunContext.CODEC);
        property = this;
    }

    public static GunContext getGunContext(ModuleInstance moduleInstance) {
        return property.getData(moduleInstance).orElse(new GunContext());
    }

    @Override
    public GunContext merge(GunContext left, GunContext right, MergeType mergeType) {
        return new GunContext(
                DoubleOperationResolvable.merge(left.fireRate(), right.fireRate(), mergeType),
                DoubleOperationResolvable.merge(left.baseDamage(), right.baseDamage(), mergeType),
                DoubleOperationResolvable.merge(left.magazineSize(), right.magazineSize(), mergeType),
                DoubleOperationResolvable.merge(left.range(), right.range(), mergeType)
        );
    }

    @Override
    public GunContext initialize(GunContext property, ModuleInstance context) {
        return property.initialize(context);
    }

    public record GunContext(
            DoubleOperationResolvable fireRate,
            DoubleOperationResolvable baseDamage,
            DoubleOperationResolvable magazineSize,
            DoubleOperationResolvable range
    ) {
        public static final Codec<GunContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DoubleOperationResolvable.CODEC.optionalFieldOf("fire_rate", new DoubleOperationResolvable(1.0)).forGetter(GunContext::fireRate),
                DoubleOperationResolvable.CODEC.optionalFieldOf("base_damage", new DoubleOperationResolvable(5.0)).forGetter(GunContext::baseDamage),
                DoubleOperationResolvable.CODEC.optionalFieldOf("magazine_size", new DoubleOperationResolvable(10.0)).forGetter(GunContext::magazineSize),
                DoubleOperationResolvable.CODEC.optionalFieldOf("range", new DoubleOperationResolvable(100.0)).forGetter(GunContext::range)
        ).apply(instance, GunContext::new));

        public GunContext() {
            this(new DoubleOperationResolvable(1.0), new DoubleOperationResolvable(5.0), new DoubleOperationResolvable(10.0), new DoubleOperationResolvable(100.0));
        }

        public GunContext initialize(ModuleInstance moduleInstance) {
            return new GunContext(
                    fireRate.initialize(moduleInstance),
                    baseDamage.initialize(moduleInstance),
                    magazineSize.initialize(moduleInstance),
                    range.initialize(moduleInstance)
            );
        }
    }
}
