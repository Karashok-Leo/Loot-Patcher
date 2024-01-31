package net.karashokleo.extraloot;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtraLootEvent
{
    public static void extraLoot()
    {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
        {
            for (ExtraLootConfig.ExtraLoot extraLoot : ExtraLoot.CONFIG.extraLoots)
                if (id.toString().equals(extraLoot.extra_table)) return;
            for (ExtraLootConfig.ExtraLoot extraLoot : ExtraLoot.CONFIG.extraLoots)
                if (matches(id.toString(), extraLoot.target_regex))
                {
                    Identifier tableId = new Identifier(extraLoot.extra_table);
                    LootPool.Builder poolBuilder = LootPool
                            .builder()
                            .rolls(ConstantLootNumberProvider.create(1))
                            .with(LootTableEntry.builder(tableId));
                    tableBuilder.pool(poolBuilder);
                }
        });
    }

    public static void extraXp()
    {
        if (ExtraLoot.CONFIG.extraXpMultiplier <= 0) return;
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) ->
        {
            if (world.random.nextDouble() > ExtraLoot.CONFIG.extraXpChance) return;
            if (entity instanceof PlayerEntity && killedEntity instanceof MobEntity mob)
                ExperienceOrbEntity.spawn(world, mob.getPos(), MathHelper.floor(TierCondition.getTotalValue(mob) * ExtraLoot.CONFIG.extraXpMultiplier));
        });
    }

    public static boolean matches(String subject, @Nullable String nullableRegex)
    {
        if (subject == null)
            subject = "";
        if (nullableRegex == null || nullableRegex.isEmpty())
            return true;
        Pattern pattern = Pattern.compile(nullableRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
}
