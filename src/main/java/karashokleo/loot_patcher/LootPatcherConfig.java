package karashokleo.loot_patcher;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

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
    }
}
