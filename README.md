# Introduction
This mod has added a new loot table predicate ([What is that?](https://minecraft.wiki/w/Predicate)) so that you can configure the loot based on entity attributes.

In addition, you can quickly add new pools to existing loot tables by editing configuration file.
### The initial configuration file looks like:

```
{
  "maxHealthWeight": 1.0,
  "armorWeight": 1.0,
  "armorToughnessWeight": 1.0,
  "attackDamageWeight": 1.0,
  "extraLoots": [],
  "extraXpMultiplier": 1.0,
  "extraXpChance": 0.5
}
```
The first four entries determine the weights of various attributes.
The remaining three entries are used to configure loot and experience drops.

# Example Usage
It is obvious that there should be no ellipses or comments in the JSON format file, and this case is only for the sake of explanation.
### Configuration file:
### ```extra-loot.json```

```
{
  "maxHealthWeight": 1.0,
  "armorWeight": 2.0,
  "armorToughnessWeight": 2.0,
  "attackDamageWeight": 4.0,
  "extraLoots": [
    {
      "target_regex": "^(?!.*xxx).*entities.*", //Support regular expressions.
      "extra_table": "xxx:common_drop"
    }
  ],
  "extraXpMultiplier": 1.0,
  "extraXpChance": 0.1
}
```
### Datapack:
### ```data/xxx/loot_tables/common_drop.json```

```
{
  "pools": [
    {
      "conditions": [
        {
          "condition": "minecraft:killed_by_player"
        },
        {
          "condition": "extra-loot:entity_tier",
          "entity": "this",
          "min": 0,
          "max": 50 //for "max" property, -1 means infinity.
        }
      ],
      "entries": [
      ...
      ]
    }
  ]
}
```
### Consequence
The above configuration indicates that an additional loot table("xxx:common_drop") will be added to all loot tables with identifier containing "entities" but not "xxx".

For instance, when a player kills a zombie, the game will weight and sum the maximum health, armor, armor toughness, and attack damage of the zombie. If this sum is between 0 and 50, then the condition is met.

Besides, when a player kills a mob, there is a certain chance to drop extra experience. The probability is equal to the "extraXpChance" property in the configuration file, and the value of extra experience is equal to the weighted sum of attributes multiplied by the "extraXpMultiplier" property in the configuration file.

# Warning
You should try to avoid infinite loops in the loot table during the configuration process.

~~Even if you don't, there won't be any serious consequences...~~

```
{
...
  "extraLoots": [
    {
      "target_regex": ".*",
      "extra_table": "minecraft:entities/zombie"
    }
  ]
...
}
```
In this case, the target loot tables contain the extra table. If you kill a zombie, the zombie's loot table will attempt to drop itself, which causes infinite recursion.
