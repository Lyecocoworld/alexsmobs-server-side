# Alex's Mobs Server-Side — Reconstruction Complète

Réimplémentation **100% server-side** d'Alex's Mobs pour serveurs **Folia**, utilisant **MythicMobs** + **BetterModel** + **CraftEngine**.

> **Aucun mod client requis.** Tout passe par le resource pack généré par BetterModel + CraftEngine.

## État du projet

| Composant | Statut | Détail |
|-----------|--------|--------|
| **Créatures** | ✅ 87/89 | Chaque créature a 8 fichiers YAML (mob, skills, ai, drops, spawn, sounds, variables, model) |
| **Items CraftEngine** | ✅ 301 configs | Materials, food, tools, armor, spawn eggs |
| **Blocks CraftEngine** | ✅ 137 configs | Decorations, spawn blocks, building |
| **Assets** | ✅ Importés | 739 textures, 575 sons, 438 modèles 3D |
| **Data** | ✅ Importée | 139 loot tables, 223 tags, 84 recipes |
| **Resource Pack** | ✅ sounds.json | 575 sound events mappés |
| **Worldgen Analysis** | ✅ Document | Nature's Spirit + Larion analysés pour port Folia |

## Stack technique

| Plugin | Rôle |
|--------|------|
| **MythicMobs** 5.12+ | IA, skills, drops, spawners |
| **BetterModel** 3.2+ | Modèles 3D + animations Blockbench |
| **CraftEngine** | Items/blocks custom |
| **PlaceholderAPI** | Variables exposées |
| **WorldGuard/WorldEdit** | Protection |

## Structure

```
alexsmobs/
├── grizzly_bear/          # 8 fichiers par créature
│   ├── mob.yml            # Définition MythicMobs
│   ├── skills.yml         # Compétences (IA, attacks, interactions)
│   ├── ai.yml             # AIGoals + TargetSelectors
│   ├── drops.yml          # Loot tables (vraies données du mod)
│   ├── spawn.yml          # Spawners (biomes, conditions)
│   ├── sounds.yml         # Banque de sons
│   ├── variables.yml      # Variables persistantes
│   └── model/model.yml    # Mapping BetterModel
├── elephant/
├── crimson_mosquito/
├── ... (87 dossiers)

_core/                     # Infrastructure partagée
├── globals.yml            # Skills globaux
├── items_registry.yml     # Registry 301 items
├── blocks_registry.yml    # Registry 137 blocks
├── item_tags.yml          # 124 tags (food/taming groups)
├── drops_mapping.yml      # Loot table parsing
├── recipes.yml            # 84 recipes
├── sounds_registry.yml    # Registry sons
└── biome_tags.yml         # Tags de biomes

craftengine/               # Configs CraftEngine
├── items/                 # 301 fichiers YAML
└── blocks/                # 137 fichiers YAML

assets/                    # Assets (GPL-3.0, Alex's Mobs)
├── textures/              # 739 PNG
├── sounds/                # 575 OGG
├── models/                # 438 JSON
├── sounds.json            # 575 events mappés
└── pack.mcmeta

data/                      # Data (loot tables, tags, recipes)
├── loot_tables/           # 139 fichiers
├── tags/                  # 223 fichiers
└── recipes/               # 84 fichiers

_docs/                     # Documentation
├── CONVENTIONS.md
├── CREATURE_TEMPLATE.md
├── CREATURE_CATALOG.md
├── FOLIA_OPTIMIZATION.md
├── MYTHICMOBS_PATTERNS.md
├── STACK_NOTES.md
├── MOD_WORLDGEN_ANALYSIS.md
└── WORLDGEN_ANALYSIS_NATURES_SPIRIT_LARION.md
```

## Installation

1. Copier `alexsmobs/` dans `plugins/MythicMobs/`
2. Copier `craftengine/` dans `plugins/CraftEngine/`
3. Packager `assets/` comme resource pack
4. `/mm reload`

## Crédits

- **Original**: [Alex's Mobs](https://www.curseforge.com/minecraft/mc-mods/alexs-mobs) par AlexModGuy (GPL-3.0)
- **Réimplémentation**: Aucun code Java copié. Comportements reproduits via MythicMobs/BetterModel/CraftEngine.

## Licence

GPL-3.0-only (héritée d'Alex's Mobs)
