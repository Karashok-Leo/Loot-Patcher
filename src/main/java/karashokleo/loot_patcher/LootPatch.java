package karashokleo.loot_patcher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;

public record LootPatch(
        List<String> target_tables,
        List<Identifier> extra_tables
)
{
    public static final Codec<LootPatch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("target_tables").forGetter(LootPatch::target_tables),
            Identifier.CODEC.listOf().fieldOf("extra_tables").forGetter(LootPatch::extra_tables)
    ).apply(instance, LootPatch::new));
}
