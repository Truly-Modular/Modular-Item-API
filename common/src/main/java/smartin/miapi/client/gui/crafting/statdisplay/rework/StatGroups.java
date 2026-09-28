package smartin.miapi.client.gui.crafting.statdisplay.rework;

import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.StatGroup;

public class StatGroups {
    public static final StatGroup MELEE_GROUP = StatGroup.getOrRegister(Miapi.id("melee"));
    public static final StatGroup MELEE_MODIFIER_GROUP = StatGroup.getOrRegister(Miapi.id("melee_modifier"));

    public static final StatGroup ON_HIT = StatGroup.getOrRegister(Miapi.id("on_hit"));

    public static final StatGroup PROJECTILE_WEAPON = StatGroup.getOrRegister(Miapi.id("on_hit"));

    public static final StatGroup THROW = StatGroup.getOrRegister(Miapi.id("throw"));

    public static final StatGroup PROJECTILE = StatGroup.getOrRegister(Miapi.id("projectile"));

    public static final StatGroup ARMOR = StatGroup.getOrRegister(Miapi.id("armor"));

    public static final StatGroup MISC = StatGroup.getOrRegister(Miapi.id("misc"));

    public static final StatGroup MINING_GROUP = StatGroup.getOrRegister(Miapi.id("mining"));
}
