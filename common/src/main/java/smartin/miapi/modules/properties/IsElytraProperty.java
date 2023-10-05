package smartin.miapi.modules.properties;

import net.minecraft.client.network.ClientPlayerEntity;
import smartin.miapi.modules.properties.util.BooleanProperty;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 */
public class IsElytraProperty extends BooleanProperty {
    public static final String KEY = "isElytra";
    public static IsElytraProperty property;

    public IsElytraProperty() {
        super(KEY);
        property = this;
        ClientPlayerEntity player;
    }
}