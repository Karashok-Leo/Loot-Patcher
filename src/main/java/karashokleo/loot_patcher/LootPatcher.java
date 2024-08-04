package karashokleo.loot_patcher;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LootPatcher implements ModInitializer
{
    public static LootPatcherConfig CONFIG = new LootPatcherConfig();

    @Override
    public void onInitialize()
    {
        AutoConfig.register(LootPatcherConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(LootPatcherConfig.class).getConfig();

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
        {
            for (LootPatcherConfig.Patch patch : LootPatcher.CONFIG.patches)
                for (String targetRegex : patch.target_tables)
                    if (matches(id.toString(), targetRegex))
                    {
                        tablePools(tableBuilder, patch.extra_tables);
                        return;
                    }
        });
    }

    private static boolean matches(String subject, @Nullable String nullableRegex)
    {
        if (subject == null)
            subject = "";
        if (nullableRegex == null || nullableRegex.isEmpty())
            return true;
        Pattern pattern = Pattern.compile(nullableRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }

    private static void tablePools(LootTable.Builder tableBuilder, List<String> tableIds)
    {
        tableBuilder.pools(
                tableIds.stream()
                        .map(
                                tableId -> LootPool
                                        .builder()
                                        .rolls(ConstantLootNumberProvider.create(1))
                                        .with(LootTableEntry.builder(new Identifier(tableId)))
                                        .build()
                        )
                        .toList()
        );
    }
}
