# Analyse: Nature's Spirit + Larion Worldgen — Port Folia

## Vue d'ensemble

| Mod | Rôle | Downloads | License | Dépendance client |
|-----|------|-----------|---------|-------------------|
| **Nature's Spirit** | Biomes, blocs, items, décorations | 8.32M | Natures Spirit License (custom) | Client+Serveur |
| **Larion Worldgen** | Terrain shape, density functions, caves | — | Apache-2.0 | Client+Serveur |

### Complémentarité

- **Larion** modifie uniquement le **terrain** (density functions, noise) — pas les biomes
- **Nature's Spirit** ajoute des **biomes** via TerraBlender — pas le terrain
- **Combinés** = terrain épique + biomes diversifiés = expérience worldgen complète
- Larion recommande lui-même d'utiliser un datapack de biomes (WWOO) en complément

---

## Nature's Spirit — Analyse de port Folia

### Features

| Feature | Portable ? | Méthode | Faisabilité |
|---------|-----------|---------|-------------|
| **Nouveaux biomes** (Blooming Desert, Coconut Beach, etc.) | ✅ | Datapack worldgen (biome JSON) | 95% |
| **Nouveaux blocs** (sable corallien, plantes, bois) | ✅ | CraftEngine (note block / furniture) | 90% |
| **Nouveaux items** (fruits, matériaux) | ✅ | CraftEngine | 95% |
| **Nouvelles plantes/fleurs** | ✅ | CraftEngine + datapack features | 85% |
| **Génération de structures** | ⚠️ | Datapack (structure NBT + template pools) | 70% |
| **Custom trees** | ✅ | Datapack (configured features, worldgen) | 90% |
| **Custom sounds** | ✅ | Resource pack (assets/sounds) | 100% |

### Biomes identifiés (depuis la description)

Nature's Spirit ajoute des biomes comme:
- Blooming Desert (désert fleuri)
- Coconut Beach (plage de cocotiers)
- Et d'autres biomes diversifiés

### Stack technique recommandée

```
Datapack worldgen (biome JSON, features, structures)
  + CraftEngine (blocs custom, items custom, décorations)
  + Resource pack (textures, sons, modèles)
```

**Aucun plugin lourd requis** — le worldgen vanilla Folia supporte les datapacks nativement.

### Avantage clé pour Folia

Les datapacks worldgen utilisent les **configured features** et **placed features** de Minecraft vanilla. Folia gère le worldgen dans des threads séparés par chunk — les datapacks sont **100% compatibles Folia** sans modification.

---

## Larion Worldgen — Analyse de port Folia

### Features

| Feature | Portable ? | Méthode | Faisabilité |
|---------|-----------|---------|-------------|
| **Terrain shape overhaul** | ✅ | Datapack (density function overrides) | 100% |
| **Cave generation changes** | ✅ | Datapack (noise_router overrides) | 100% |
| **Surface rules** | ✅ | Datapack (surface_rule overrides) | 100% |
| **Mountain generation** | ✅ | Datapack (density functions) | 100% |

### Stack technique recommandée

```
Datapack worldgen UNIQUEMENT
  (density_functions/, worldgen/noise_settings/, worldgen/noise_router/)
```

**Larion est déjà un datapack en essence** — il modifie les density functions qui sont des fichiers JSON vanilla. Aucune dépendance plugin.

### Avantage clé pour Folia

Les density functions sont du pur JSON vanilla. Folia les lit nativement. **Zéro modification nécessaire** — il suffit d'extraire les fichiers du mod et de les mettre dans un datapack.

### Extraction des density functions

Le mod Larion (Apache-2.0) contient:
```
data/minecraft/worldgen/noise_settings/
data/larion/worldgen/density_function/
```

Il suffit de copier ces fichiers dans un datapack:
```
my_datapack/data/minecraft/worldgen/noise_settings/overworld.json
my_datapack/data/larion/worldgen/density_function/*.json
```

---

## Plan de port combiné

### Phase 1: Larion (facile, 1 jour)

1. Extraire les density functions du JAR du mod
2. Créer un datapack avec ces fichiers
3. Tester sur serveur Folia

### Phase 2: Nature's Spirit (moyen, 3-5 jours)

1. Identifier tous les biomes (lire le code source)
2. Créer les biome JSON dans un datapack
3. Créer les blocs/items via CraftEngine
4. Créer les configured features (arbres, plantes)
5. Créer les surface rules
6. Tester

### Phase 3: Intégration Alex's Mobs (1 jour)

1. Ajuster les biome tags des spawners Alex's Mobs pour inclure les nouveaux biomes
2. Configurer les spawns dans les nouveaux biomes

---

## Conclusion

| Critère | Larion | Nature's Spirit | Combiné |
|---------|--------|-----------------|---------|
| Faisabilité port Folia | 100% | 85-95% | 90% |
| Effort requis | Minimal (datapack) | Moyen (datapack + CraftEngine) | Moyen |
| Plugin nécessaire | Aucun | CraftEngine pour blocs/items | CraftEngine |
| Compatibilité Alex's Mobs | Parfaite (ne touche pas aux biomes) | Excellente (plus de biomes = plus de spawns) | Parfaite |

**Recommandation**: Port Larion d'abord (immédiat, datapack pur), puis Nature's Spirit (biomes + CraftEngine). La combinaison avec Alex's Mobs server-side donnera un serveur avec worldgen épique, biomes diversifiés, et 87+ créatures custom — le tout 100% server-side, sans mod client.

---

## Mods vanilla-like recommandés (petites fonctionnalités)

### Faisables en plugin Folia (MythicMobs/CraftEngine)

| Mod/Feature | Description | Méthode | Faisabilité |
|-------------|-------------|---------|-------------|
| **Supplementaries** (parties) | Hanging signs, flower pots, hanging chains | CraftEngine blocks/furniture | 80% |
| **Macaw's Furniture** | Chaises, tables, meubles décoratifs | CraftEngine furniture | 90% |
| **Macaw's Bridges/Doors/Trapdoors** | Ponts, portes, trappes custom | CraftEngine blocks | 90% |
| **Chimes** | Carillons éoliens, cloches | CraftEngine + sounds | 75% |
| **Compostables étendus** | Plus d'items compostables | Datapack tags | 100% |
| **Villager Names** | Noms custom pour villageois | Plugin léger (Folia-safe) | 95% |
| **More Frogs variants** | Variantes de grenouilles | MythicMobs (comme Alex's Mobs) | 85% |

### Non-faisables sans client mod

| Feature | Pourquoi |
|---------|----------|
| Custom shaders | Requiert client-side OpenGL |
| Custom GUI screens | Pas possible via resource pack serveur seul |
| Custom animations de blocs (tick-by-tick) | Limitation du protocol |
| Custom mob models côté client | Sauf via BetterModel/GeckoLib packets |

### Stack vanilla-like recommandée

```
Alex's Mobs Server-Side (87 créatures)
  + Nature's Spirit (biomes via datapack + CraftEngine)
  + Larion (terrain via datapack)
  + CraftEngine items/blocks (Supplementaries subset, Macaw's)
  + MythicMobs (pour les petites features de créatures)
```
