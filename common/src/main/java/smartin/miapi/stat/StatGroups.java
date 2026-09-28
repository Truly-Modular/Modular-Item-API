package smartin.miapi.stat;

import smartin.miapi.Miapi;
import smartin.miapi.stat.api.StatGroup;

public class StatGroups {
    public static final StatGroup MELEE_GROUP = StatGroup.getOrRegister(Miapi.id("melee"), 0);
    public static final StatGroup MELEE_MODIFIER_GROUP = StatGroup.getOrRegister(Miapi.id("melee_modifier"), 1);

    public static final StatGroup PROJECTILE_WEAPON = StatGroup.getOrRegister(Miapi.id("projectile_weapon"), 2);
    public static final StatGroup PROJECTILE_MODIFIER = StatGroup.getOrRegister(Miapi.id("projectile"), 3);

    public static final StatGroup THROW = StatGroup.getOrRegister(Miapi.id("throw"), 4);

    public static final StatGroup ON_HIT = StatGroup.getOrRegister(Miapi.id("on_hit"), 5);

    public static final StatGroup MINING_GROUP = StatGroup.getOrRegister(Miapi.id("mining"), 6);

    public static final StatGroup ARMOR = StatGroup.getOrRegister(Miapi.id("armor"), 7);

    public static final StatGroup MISC = StatGroup.getOrRegister(Miapi.id("misc"), 8);

}
