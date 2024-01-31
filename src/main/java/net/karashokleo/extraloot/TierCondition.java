package net.karashokleo.extraloot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

import java.util.Set;

public class TierCondition implements LootCondition
{
    final LootContext.EntityTarget entity;
    final int min;
    final int max;

    public TierCondition(LootContext.EntityTarget entity, int min, int max)
    {
        this.entity = entity;
        this.min = min;
        this.max = max;
    }

    public static double getTotalValue(MobEntity mob)
    {
        double maxHealth = mob.getMaxHealth();
        double armor = mob.getArmor();
        double armorToughness = mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) ? mob.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) : 0;
        double attackDamage = mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE) ? mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) : 0;
        return maxHealth * ExtraLoot.CONFIG.maxHealthWeight + armor * ExtraLoot.CONFIG.armorWeight + armorToughness * ExtraLoot.CONFIG.armorToughnessWeight + attackDamage * ExtraLoot.CONFIG.attackDamageWeight;
    }

    @Override
    public boolean test(LootContext lootContext)
    {
        Entity entity = lootContext.get(this.entity.getParameter());
        return entity instanceof MobEntity mob && min <= getTotalValue(mob) && (max == -1 || getTotalValue(mob) < max);
    }

    @Override
    public LootConditionType getType()
    {
        return ExtraLoot.ENTITY_TIER;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters()
    {
        return ImmutableSet.of(LootContextParameters.ORIGIN, this.entity.getParameter());
    }

    public static LootCondition.Builder create(LootContext.EntityTarget entity, int min, int max)
    {
        return () -> new TierCondition(entity, min, max);
    }

    public static class Serializer
            implements JsonSerializer<TierCondition>
    {
        @Override
        public void toJson(JsonObject json, TierCondition tierCondition, JsonSerializationContext context)
        {
            json.add("entity", context.serialize(tierCondition.entity));
            json.add("min", context.serialize(tierCondition.min));
            json.add("max", context.serialize(tierCondition.max));
        }

        @Override
        public TierCondition fromJson(JsonObject json, JsonDeserializationContext context)
        {
            return new TierCondition(JsonHelper.deserialize(json, "entity", context, LootContext.EntityTarget.class), JsonHelper.getInt(json, "min"), JsonHelper.getInt(json, "max"));
        }
    }
}
