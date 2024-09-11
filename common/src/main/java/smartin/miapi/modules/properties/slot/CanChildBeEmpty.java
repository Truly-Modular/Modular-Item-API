package smartin.miapi.modules.properties.slot;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This property defines whether a module's child slot can be left empty.
 * It ensures flexibility in slot management by allowing or disallowing child slots to be empty when modifying or crafting modules.
 *
 * @header Can Child Be Empty Property
 * @path /data_types/properties/slot/can_child_be_empty
 * @description_start
 * The Can Child Be Empty Property determines whether a child slot in a module can be left empty. This property is
 * useful in crafting systems where some modules may allow their child slots to remain unoccupied, while others require
 * them to be filled for proper functionality.
 * @description_end
 * @data can_child_be_empty: A boolean value indicating whether the child slot can be empty.
 *
 * @see ComplexBooleanProperty
 */

public class CanChildBeEmpty extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("can_child_be_empty");
    public static CanChildBeEmpty property;

    public CanChildBeEmpty() {
        super(KEY, true);
        property = this;
    }
}
