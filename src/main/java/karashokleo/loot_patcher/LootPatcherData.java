package karashokleo.loot_patcher;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class LootPatcherData
{
    public static List<LootPatch> getPatches()
    {
        return List.copyOf(PATCHES);
    }

    public static void load()
    {
        LootPatcher.LOGGER.info("[Loot-Patcher] Patches loading begins!");
        PATCHES.clear();
        doLoad();
        if (LootPatcher.isClothLoaded())
            PATCHES.addAll(LootPatcherConfig.getPatches());
        LootPatcher.LOGGER.info("[Loot-Patcher] Patches loading ends!");
    }

    private static final List<LootPatch> PATCHES = new ArrayList<>();

    private static Path getConfigPath()
    {
        return FabricLoader.getInstance().getConfigDir().resolve(LootPatcher.MOD_ID);
    }

    private static List<Path> getConfigPaths()
    {
        Path configPath = getConfigPath();
        try
        {
            if (!Files.isDirectory(configPath))
                Files.createDirectory(configPath);
        } catch (Exception e)
        {
            LootPatcher.LOGGER.error("[Loot-Patcher] Error while creating patch directory!", e);
        }
        try (Stream<Path> pathStream = Files.walk(configPath))
        {
            return pathStream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .toList();
        } catch (Exception e)
        {
            LootPatcher.LOGGER.error("[Loot-Patcher] Error while getting patch paths!", e);
        }
        return Collections.emptyList();
    }

    private static void doLoad()
    {
        Codec<List<LootPatch>> codec = LootPatch.CODEC.listOf();
        for (Path path : getConfigPaths())
        {
            try (Reader reader = Files.newBufferedReader(path))
            {
                JsonElement jsonRoot = JsonParser.parseReader(reader);
                List<LootPatch> patchList = codec.decode(JsonOps.INSTANCE, jsonRoot).result().orElseThrow().getFirst();
                PATCHES.addAll(patchList);
                LootPatcher.LOGGER.info("[Loot-Patcher] loot patch `{}` successfully loaded!", path.getFileName().toString());
            } catch (Exception e)
            {
                LootPatcher.LOGGER.error("[Loot-Patcher] Error while loading loot patch `{}`!", path.getFileName().toString());
            }
        }
    }
}
