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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LootPatcher implements ModInitializer
{
    public static LootPatcherConfig CONFIG = new LootPatcherConfig();
    public static final String MOD_ID = "loot-patcher";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean reloading = false;
    private static final HashSet<LootPatcherConfig.Patch> patchesCache = new HashSet<>();
    private static final HashSet<LootPatcherConfig.Patch> patchesUsed = new HashSet<>();

    @Override
    public void onInitialize()
    {
        AutoConfig.register(LootPatcherConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(LootPatcherConfig.class).getConfig();

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
        {
            if (!reloading)
            {
                reloading = true;
                LOGGER.info("Loot-Patcher patching begins!");
                patchesCache.clear();
                patchesUsed.clear();
                patchesCache.addAll(LootPatcher.CONFIG.patches);
            }

            for (LootPatcherConfig.Patch patch : patchesCache)
            {
                boolean doPatch = false;
                for (String targetRegex : patch.target_tables)
                    if (matches(id.toString(), targetRegex))
                    {
                        doPatch = true;
                        break;
                    }
                if (doPatch)
                {
                    tablePools(tableBuilder, patch.extra_tables);
                    patchesUsed.add(patch);
                }
            }
        });

        LootTableEvents.ALL_LOADED.register((resourceManager, lootManager) ->
        {
            patchesCache.removeAll(patchesUsed);
            if (!patchesCache.isEmpty())
            {
                LOGGER.warn("Found {} unused patches, possibly due to mistakes in the target loot table regex!", patchesCache.size());
                LOGGER.info("Unused patches:");
                for (LootPatcherConfig.Patch patch : patchesCache) logPatch(patch);
            }
            patchesCache.clear();
            patchesUsed.clear();
            LOGGER.info("Loot-Patcher patching ends!");
            reloading = false;
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

    private static void logPatch(LootPatcherConfig.Patch patch)
    {
        LOGGER.info("\t{");
        logTables("target_tables", patch.target_tables);
        logTables("extra_tables", patch.extra_tables);
        LOGGER.info("\t}");
    }

    private static void logTables(String name, List<String> tables)
    {
        LOGGER.info("\t\t\"{}\": [", name);
        for (String table : tables)
            LOGGER.info("\t\t\t\"{}\"", table);
        LOGGER.info("\t\t]");
    }
}
