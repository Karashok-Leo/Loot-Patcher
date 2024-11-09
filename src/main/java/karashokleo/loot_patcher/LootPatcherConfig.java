package karashokleo.loot_patcher;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@Config(name = "loot-patcher")
public class LootPatcherConfig implements ConfigData
{
    public List<Patch> patches = new ArrayList<>();

    public static class Patch
    {
        public List<String> target_tables = new ArrayList<>();
        public List<String> extra_tables = new ArrayList<>();

        public LootPatch toPatch()
        {
            return new LootPatch(
                    target_tables,
                    extra_tables.stream().map(Identifier::new).toList()
            );
        }
    }

    public static List<LootPatch> getPatches()
    {
        return AutoConfig.getConfigHolder(LootPatcherConfig.class).getConfig().toPatches();
    }

    public List<LootPatch> toPatches()
    {
        return patches.stream().map(Patch::toPatch).toList();
    }
}
