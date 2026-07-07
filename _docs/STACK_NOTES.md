# Notes sur le stack technique

## Priorité des outils

### 1. MythicMobs (primaire)

**Rôle** : Cœur du projet. Gère toute l'IA, les skills, les drops, les spawners, les variantes.

**Version** : 5.12+ (requis pour compatibilité Folia)

**Fonctionnalités utilisées** :
- AIGoalSelectors / AITargetSelectors (IA native)
- Threat Tables (targeting region-local)
- Skill System (mécaniques atomiques réutilisables)
- Variables persistantes (state management)
- Stances (animation state pour BetterModel)
- Triggers (onSpawn, onDeath, onTimer, onInteract, etc.)
- Conditions inline (`?condition` / `!condition`)
- DamageModifiers (immunités/résistances)
- ImmunityTable (invulnérabilité frame)
- Spawners (spawn naturel par biome)

**Folia-safe** : MythicMobs 5.x utilise le RegionScheduler/EntityScheduler de Folia pour ses timers et ses opérations sur entités. Pas de main thread.

### 2. BetterModel (primaire)

**Rôle** : Modèles 3D Blockbench + animations, synchronisés avec MythicMobs.

**Version** : 3.2+ (GeckoLib-based)

**Fonctionnalités utilisées** :
- Import direct de modèles `.bbmodel` (Blockbench)
- Animation states mappés aux stances MythicMobs
- Variant switching (texture dynamique selon variable MythicMobs)
- Rider offset (positioning du joueur monté)
- Hitbox custom (width/height par state)

**Intégration** :
- MythicMobs `setmodelstate{state=X}` → BetterModel joue l'animation X
- MythicMobs variable `mob.snowy=true` → BetterModel swap la texture
- Le modèle Blockbench original d'Alex's Mobs est utilisé directement (GPL-3.0)

### 3. CraftEngine (secondaire)

**Rôle** : Items custom, blocks custom, nourriture, décorations.

**Fonctionnalités utilisées** :
- Custom items (bear_fur, animal_dictionary, etc. — 43 items du mod)
- Custom blocks (banana_peel, crystallized_mucus, etc. — 27 blocks du mod)
- Nourriture avec effects (fish_oil, rainbow_jelly, etc.)
- Resource pack auto-généré

**Quand l'utiliser** :
- ✅ Items droppés par les créatures
- ✅ Blocks interactifs (ruches, nids, œufs de reptile)
- ✅ Nourriture spéciale (effets au consommation)
- ✅ Outils/armes custom (dimensional_carver, ghostly_pickaxe)
- ❌ IA (→ MythicMobs)
- ❌ Modèles 3D (→ BetterModel)

### 4. Nexo (tertiaire — dernier recours)

**Rôle** : Resource pack merge, custom blocks/furniture.

**Quand l'utiliser** :
- Uniquement si CraftEngine ne peut pas faire le job
- Merge de resource packs (si BetterModel + CraftEngine conflict)
- Furniture 3D (items posés au sol)

**Éviter si possible** : Nexo ajoute une couche de complexité. CraftEngine + BetterModel suffisent pour 95% des cas.

### 5. PlaceholderAPI (support)

**Rôle** : Exposer les variables MythicMobs aux autres plugins.

**Exemples** :
- `%grizzly_bear.tamed%` → true/false
- `%grizzly_bear.command%` → 0/1/2
- `%grizzly_bear.honeyed%` → true/false

### 6. WorldGuard / WorldEdit (support)

**Rôle** : Protéger les zones de spawn, désactiver le PvP dans certaines zones.

### 7. ProtocolLib (optionnel)

**Rôle** : Packets custom si nécessaire (ex: faux mount, faux damage indicator).

### 8. PacketEvents (optionnel)

**Rôle** : Détection avancée de packets (anti-cheat, animations custom client-side).

---

## Limitations connues

| Mécanique | Possible ? | Alternative |
|-----------|-----------|-------------|
| Custom GUI client | ❌ | Menu via chest GUI (MythicMobs) |
| Custom keybinds | ❌ | Via items (right-click) |
| Custom shaders | ❌ | Particules + resource pack |
| True entity registration | ❌ | Disguise sur entité vanilla |
| Animation blending parfait | ⚠️ | BetterModel lerp (95% fidèle) |
| Client-side input handling | ❌ | Server-side triggers |
| Custom dimensions | ⚠️ | Via datapack/world plugin |

Les ~5% impossibles sans client mod : input handling, true entity registration, animation blending parfait, custom screen layouts, shaders.

---

## Compatibilité Folia — Checklist plugin

| Plugin | Folia-safe ? | Version min |
|--------|-------------|-------------|
| MythicMobs 5.12+ | ✅ | 5.12 |
| BetterModel 3.2+ | ✅ | 3.2 |
| CraftEngine | ✅ | dernière |
| PlaceholderAPI | ✅ | dernière |
| WorldGuard | ⚠️ | nécessite fork Folia |
| ProtocolLib | ✅ | dernière (Paper/Folia) |
| PacketEvents | ✅ | dernière |
