package net.karashokleo.extraloot;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ExtraLoot implements ModInitializer
{
    public static ExtraLootConfig CONFIG = new ExtraLootConfig();
    public static final LootConditionType ENTITY_TIER = Registry.register(Registries.LOOT_CONDITION_TYPE, new Identifier("extra-loot", "entity_tier"), new LootConditionType(new TierCondition.Serializer()));

    @Override
    public void onInitialize()
    {
        AutoConfig.register(ExtraLootConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ExtraLootConfig.class).getConfig();
        ExtraLootEvent.extraLoot();
        ExtraLootEvent.extraXp();
    }
}
