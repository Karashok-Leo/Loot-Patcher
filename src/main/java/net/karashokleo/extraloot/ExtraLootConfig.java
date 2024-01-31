package net.karashokleo.extraloot;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "extra-loot")
public class ExtraLootConfig implements ConfigData
{
    @Comment(value = "Max Health Weight")
    double maxHealthWeight = 1;

    @Comment(value = "Armor Weight")
    double armorWeight = 1;

    @Comment(value = "Armor Toughness Weight")
    double armorToughnessWeight = 1;

    @Comment(value = "Attack Damage Weight")
    double attackDamageWeight = 1;

    @Comment(value = "Extra Loot")
    ExtraLoot[] extraLoots = {};

    @Comment(value = "Extra Xp Multiplier")
    double extraXpMultiplier = 1;

    @Comment(value = "Extra Xp Chance")
    double extraXpChance = 0.5;

    static class ExtraLoot
    {
        String target_regex = ".*entities.*";
        String extra_table = "";

        public ExtraLoot()
        {
        }

        public ExtraLoot(String target_regex, String extra_table)
        {
            this.target_regex = target_regex;
            this.extra_table = extra_table;
        }
    }
}
