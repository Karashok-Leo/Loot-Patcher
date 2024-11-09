package karashokleo.loot_patcher;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
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
    public static final String MOD_ID = "loot-patcher";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean reloading = false;
    private static final HashSet<LootPatch> patchesCache = new HashSet<>();
    private static final HashSet<LootPatch> patchesUsed = new HashSet<>();

    @Override
    public void onInitialize()
    {
        if (isClothLoaded())
            AutoConfig.register(LootPatcherConfig.class, GsonConfigSerializer::new);

        LootPatcherData.load();

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, serverResourceManager) -> LootPatcherData.load());

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
        {
            if (!reloading)
            {
                reloading = true;
                LOGGER.info("[Loot-Patcher] Patching begins!");
                patchesUsed.clear();
                patchesCache.clear();
                patchesCache.addAll(LootPatcherData.getPatches());
            }

            for (LootPatch patch : patchesCache)
            {
                boolean doPatch = false;
                for (String targetRegex : patch.target_tables())
                    if (matches(id.toString(), targetRegex))
                    {
                        doPatch = true;
                        break;
                    }
                if (doPatch)
                {
                    tablePools(tableBuilder, patch.extra_tables());
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
                for (LootPatch patch : patchesCache) logPatch(patch);
            }
            patchesCache.clear();
            patchesUsed.clear();
            LOGGER.info("[Loot-Patcher] Patching ends!");
            reloading = false;
        });
    }

    public static boolean isClothLoaded()
    {
        return FabricLoader.getInstance().isModLoaded("cloth-config");
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

    private static void tablePools(LootTable.Builder tableBuilder, List<Identifier> tableIds)
    {
        tableBuilder.pools(
                tableIds.stream()
                        .map(
                                tableId -> LootPool
                                        .builder()
                                        .rolls(ConstantLootNumberProvider.create(1))
                                        .with(LootTableEntry.builder(tableId))
                                        .build()
                        )
                        .toList()
        );
    }

    private static void logPatch(LootPatch patch)
    {
        LOGGER.info("\t{");
        logTables("target_tables", patch.target_tables());
        logTables("extra_tables", patch.extra_tables().stream().map(Identifier::toString).toList());
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
