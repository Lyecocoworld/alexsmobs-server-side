# Mod World Gen + Gameplay : Analyse de Port Folia Server-Side

> **Objectif**: Identifier le meilleur mod Minecraft de world generation + gameplay à réimplémenter
> server-side pour Folia, en synergie avec le projet Alex's Mobs server-side existant.
>
> **Date**: 2025-07-07
> **Stack actuelle du projet**: MythicMobs + BetterModel + CraftEngine (items/blocks via resource pack)

---

## Table des matières

1. [Comparatif des candidats](#1-comparatif-des-candidats)
2. [Recommandation principale: Twilight Forest](#2-recommandation-principale--twilight-forest)
3. [Description détaillée des features](#3-description-détaillée-des-features)
4. [Synergie avec Alex's Mobs](#4-synergie-avec-alexs-mobs)
5. [Estimateur de faisabilité](#5-estimateur-de-faisabilité)
6. [Stack technique nécessaire](#6-stack-technique-nécessaire)
7. [Approche de port recommandée](#7-approche-de-port-recommandée)
8. [Plan de migration Folia](#8-plan-de-migration-folia)
9. [Alternatives et plans B](#9-alternatives-et-plans-b)

---

## 1. Comparatif des candidats

| Mod | Downloads | License | World Gen | Gameplay | Server-Side | Synergie AM | Score |
|-----|-----------|---------|-----------|----------|-------------|-------------|-------|
| **Twilight Forest** | ~40M+ | **LGPL-3.0** ✅ | Dimension complète (15+ biomes, 20+ structures) | Bosses, progression, items | Réimpl. requise | ★★★★★ | **9.5/10** |
| **Quark** | ~25M+ | **LGPL-3.0** ✅ | Stone variants, cave features, arbres | Mobs, items, fonctionnalités | Réimpl. modulaire | ★★★★☆ | **8.5/10** |
| **Oh The Biomes You'll Go** | ~30M+ | **LGPL-3.0** ✅ | 80+ biomes, blocks | Limited (principalement déco) | Heavy client deps | ★★★☆☆ | 7.0/10 |
| **Alex's Caves** | ~5M+ | ❌ All Rights Reserved | 8 cave biomes | Mobs, structures, items | Réimpl. requise | ★★★★★+ | 7.5/10* |
| **The Undergarden** | ~10M+ | **LGPL-3.0** ✅ | Dimension complète | Mobs, bosses, structures | Réimpl. requise | ★★★☆☆ | 7.0/10 |
| **Blue Skies** | ~5M+ | **LGPL-3.0** ✅ | 2 dimensions | Bosses, items | Heavy client deps | ★★☆☆☆ | 6.0/10 |
| **Aquaculture 2** | ~10M+ | **MIT** ✅ | Non | Fishing overhaul | Très portable | ★★★☆☆ | 6.5/10 |
| **Nature's Compass** | ~15M+ | **MIT** ✅ | Non | Biome finder utility | Trivial à porter | ★★☆☆☆ | 5.5/10 |
| **Supplementaries** | ~10M+ | **MIT** ✅ | Mineur (signs, etc.) | Items, blocks déco | Portable | ★★★☆☆ | 6.0/10 |
| **Terralith** | ~20M+ | Datapack (CC) ✅ | Biomes vanilla | Aucune | **Déjà Folia-safe** | ★★☆☆☆ | 5.0/10** |
| **Tectonic** | ~5M+ | Datapack ✅ | Terrain | Aucune | **Déjà Folia-safe** | ★★☆☆☆ | 4.5/10** |
| **Born in Chaos** | ~5M+ | ❌ Closed source | Mineur | Mobs, dark fantasy | Réimpl. difficile | ★★☆☆☆ | 4.0/10 |
| **Strange** | ~3M+ | ❌ CurseForge-only | Mineur | Exploration scrolls | Réimpl. requise | ★★★☆☆ | 5.0/10 |
| **Macaw's mods** | ~20M+ | **LGPL-3.0** ✅ | Non | Décoration pure | Portable mais peu profond | ★☆☆☆☆ | 4.0/10 |

> \* Alex's Caves: synergie parfaite (même auteur) mais **closed source** → pas de lecture du code.
> \*\* Terralith/Tectonic: déjà des datapacks, aucun travail de port nécessaire → pas intéressant comme projet.

---

## 2. Recommandation principale — Twilight Forest

| Champ | Valeur |
|-------|--------|
| **Nom** | The Twilight Forest |
| **Auteurs** | Ben Dodson (ben_manes) — création originale; Tamaized — maintenance actuelle |
| **License** | **GNU LGPL-3.0** (code source entièrement ouvert) |
| **Source** | https://github.com/TwilightFlower/TwilightForest |
| **CurseForge** | https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest |
| **Downloads** | ~40,000,000+ (top 10 mods CurseForge tous temps) |
| **Versions** | 1.2.5 → 1.21+ (15+ ans de développement) |
| **Mod loader** | Forge / NeoForge (port Fabric non-officiel existe) |

### Pourquoi Twilight Forest est le meilleur candidat

1. **License LGPL-3.0** → code source complet lisible sur GitHub, légal de réimplémenter
2. **40M+ downloads** → communauté massive, contenu bien documenté (wiki, vidéos)
3. **World gen profonde** → dimension complète avec 15+ biomes, structures denses, génération procédurale
4. **Gameplay riche** → 10+ boss uniques avec mécaniques distinctes, système de progression
5. **Thème exploration/nature** → parfait complément à Alex's Mobs (qui est créatures/nature)
6. **Pas de client mod requis dans notre approche** → on réimplémente tout via MythicMobs + BetterModel + CraftEngine + resource pack
7. **Source code lisible** → on peut lire exactement comment chaque boss, structure et mécanique fonctionne

---

## 3. Description détaillée des features

### 3.1 Dimension & World Generation

La Twilight Forest est une dimension dense et sombre, caractérisée par:
- **Canopée permanente** — feuillage dense au-dessus de tout le monde (obscurité constante)
- **15+ biomes uniques**:
  - Twilight Forest (forêt de base, chênes géants)
  - Twilight Swamp (marais, lacs, arbres morts)
  - Fire Swamp (marais de feu, lave,Obsidian)
  - Dark Forest (forêt sombre, houx, pas de lumière)
  - Dark Forest Center (centre protégé)
  - Mushroom Forest (champignons géants)
  - Enchanted Forest (forêt enchantée, glowstones naturels)
  - Stream/Lake biomes (rivières et lacs)
  - Clearing (clairières)
  - Savanna / Spooky Forest
  - Glacier (glaciers, neige)
  - Snowy Forest
  - Highlands (montagnes des highlands)
  - Thornlands (ronces impénétrables)
  - Final Plateau (plateau final)

### 3.2 Structures (World Gen)

Le mod est célèbre pour ses structures procédurales:

| Structure | Description | Boss associé |
|-----------|-------------|--------------|
| **Hollow Tree** | Arbres creux géants avec loot | — |
| **Hedge Maze** | Labyrinthes de haies avec araignées | — |
| **Naga Courtyard** | Cour en ruines avec temple | Naga |
| **Lich Tower** | Tour centrale avec étages | Twilight Lich |
| **Labyrinth** | Labyrinthe souterrain (minotaur) | Minoshroom |
| **Hydra Lair** | Antre de l'Hydre | Hydra |
| **Knight Stronghold** | Forteresse des chevaliers | Knight Phantom |
| **Dark Tower** | Tour sombre (multi-étages) | Ur-Ghast |
| **Yeti Cave** | Caverne glacée | Yeti Alpha |
| **Troll Cave** | Caverne des trolls (clouds) | — |
| **Ice Castle / Aurora Palace** | Palais de glace | Snow Queen |
| **Final Castle** | Château final (toute la progression) | Giants, King |

### 3.3 Bosses & Progression

Le système de progression est la signature du mod — les zones sont verrouillées jusqu'à ce que le boss précédent soit vaincu:

1. **Naga** — Serpent géant, combat dans la cour. Débloque l'accès au centre.
2. **Twilight Lich** — Boss à phases dans sa tour. Débloque les marais.
3. **Minoshroom / Maze Slime** — Dans le labyrinthe. Débloque la fire swamp.
4. **Hydra** — Dragon multi-têtes. Débloque la dark forest.
5. **Knight Phantom** (x6) — Chevaliers fantômes dans la forteresse. Débloque la tour.
6. **Ur-Ghast** — Ghast géant dans la dark tower. Débloque les highlands.
7. **Alpha Yeti** — Yeti boss dans la caverne. Débloque le glacier.
8. **Snow Queen** — Reine des glaces dans le palais. Débloque le plateau final.
9. **Giants / Final Castle** — Contenu endgame.

Chaque boss a des mécaniques uniques:
- **Naga**: segments de corps destructibles, charge, régénération
- **Lich**: clones, boucliers, téléportation, projectiles
- **Hydra**: têtes multiples, attaques de feu explosives
- **Ur-Ghast**: pleurs (larmes explosives), minions ghast
- **Snow Queen**: vol, piliers de glace, dash

### 3.4 Mobs non-boss

| Mob | Type | Notes |
|-----|------|-------|
| Forest Spider | Hostile | Araignée de forêt |
| Skeleton Druid | Hostile | Lance des potions |
| Hostile Wolf | Hostile | Meute dans la dark forest |
| Wraith | Hostile | Vole, fantôme |
| Fire Beetle | Hostile | Marais de feu |
| Slime Beetle | Hostile | Crache slime |
| Minotaur | Hostile | Labyrinthe, charge |
| Mist Wolf | Hostile | Loup brumeux |
| King Spider | Hostile | Araignée géante |
| Towerwood Borers | Hostile | Insectes rongeurs de bois |
| Redcap | Hostile | Gobelin mineur |
| Redcap Sapper | Hostile | Gobelin explosif |
| Boggard | Hostile | Crapaud hostile |
| Termite | Hostile | Dans les structures |
| Tower Golem | Hostile | Golem de tour |
| Penguin | Passif | Pingouin (neige) |
| Squirrel | Passif | Écureuil |
| Bunny | Passif | Lapin (diverses couleurs) |
| Deer | Passif | Cerf |
| Tiny Bird | Passif | Oiseau |
| Raven | Passif | Corbeau |
| Forest Ram | Passif | Bélier |
| Boar | Passif | Sanglier (monture) |
| Questing Ram | Special | Bélier à quête (trouver ses laines) |

### 3.5 Items & Blocks notables

- **Twilight Oak Canopy** — Wood variants (oak, darkwood, mangrove, etc.)
- **Twilight Portal items** — Portail vers la dimension
- **Boss trophies** — Têtes de boss comme trophées
- **Unique weapons**: Glass Sword, Knightly Sword, Ice Sword, Fiery set
- **Unique armor**: Knightly, Fiery, Phantom, Yeti, Arctic
- **Functional blocks**: Munstress (eating), Smoker, Torcherberry bush
- **Transportation**: Magic Map (carte qui montre les boss vaincus)

---

## 4. Synergie avec Alex's Mobs

### 4.1 Complémentarité thématique

| Alex's Mobs apporte | Twilight Forest apporte |
|---------------------|------------------------|
| Créatures du monde naturel (réel et fantastique) | **Dimension d'exploration** avec écosystème unique |
| Spawns dans biomes vanilla | **Nouveaux biomes** où les mobs AM pourraient aussi vivre |
| Items de survie/décoration | **Bosses et progression** (endgame content) |
| Mécaniques individuelles par mob | **Système de progression structuré** |

### 4.2 Cross-over naturels

- **Mobs AM dans TF**: Des mobs comme le Grizzly Bear, Bald Eagle, ou Skreecher pourraient spawn dans les biomes TF correspondants
- **TF mobs dans le monde vanilla**: Le cerf (Deer), sanglier (Boar), ou écureuil (Squirrel) de TF ont des équivalents conceptuels dans AM — mais peuvent coexister
- **Bosses comme extension d'endgame**: Les joueurs qui explorent les dimensions TF trouvent un défi que Alex's Mobs n'offre pas
- **Art style cohérent**: Les deux mods ont un style semi-réaliste compatible

### 4.3 Stack technique partagée

Le projet utilise déjà exactement les bons outils:

| Composant TF | Outil du projet | Notes |
|--------------|-----------------|-------|
| Mobs (forest spider, deer, etc.) | **MythicMobs** (mob.yml, ai.yml, skills.yml) | Même structure YAML |
| Modèles 3D des mobs | **BetterModel** (model/model.yml) | Import depuis Blockbench |
| Items custom (glass sword, etc.) | **CraftEngine** (items_registry.yml) | Déjà en place |
| Blocks custom (twilight wood, etc.) | **CraftEngine** (blocks_registry.yml) | 137 blocks AM déjà mappés |
| Sons | **CraftEngine** sounds + MythicMobs sound{} | Pattern existant |
| Spawns par biome | **MythicMobs spawn.yml** (biome{}) | Pattern existant |
| Dimension | **Datapack** (dimension_type, dimension, biome) | Vanilla, Folia-safe |
| Structures | **Datapack .nbt** (structure features) | Vanilla, Folia-safe |

---

## 5. Estimateur de faisabilité

### 5.1 Features portables facilement (🟢 Faible effort)

| Feature | Méthode | Effort |
|---------|---------|--------|
| **Dimension vide (monde généré)** | Datapack: `dimension_type`, `dimension`, `noise_settings` | 🟢 1-2 jours |
| **Biomes TF** (base: forêt, marais, etc.) | Datapack: `worldgen/biome` avec密度 d'arbres | 🟢 3-5 jours |
| **Portail TF** | MythicMobs custom event + particle effects + block placement | 🟢 1-2 jours |
| **Mobs passifs** (Deer, Boar, Squirrel, Bunny, Penguin, Raven) | MythicMobs (6-8 mobs × 1 jour chacun) | 🟢 1-2 semaines |
| **Mobs hostiles simples** (Forest Spider, Redcap, Wraith, etc.) | MythicMobs (8-10 mobs × 1-2 jours chacun) | 🟢 2-3 semaines |
| **Items** (swords, armor, materials) | CraftEngine items_registry.yml | 🟢 2-3 jours |
| **Blocks** (wood variants, decorative) | CraftEngine blocks_registry.yml | 🟢 3-5 jours |
| **Sounds** | CraftEngine sounds + MythicMobs sound{} | 🟢 1-2 jours |
| **Magic Map** | CraftEngine item + custom display (via MythicMobs skill / Citizen NPC map) | 🟡 3-5 jours |

### 5.2 Features portables avec effort modéré (🟡 Moyen effort)

| Feature | Méthode | Défis |
|---------|---------|-------|
| **Structures** (Naga Courtyard, Lich Tower, Hedge Maze) | Datapack .nbt + structure features (jigsaw) | Besoin d'extraire/reconstruire les .nbt; jigsaw peut être complexe |
| **Boss: Naga** | MythicMobs avec segments (multipart via armor stands / display entities) | Animation des segments, mécanique de régénération |
| **Boss: Twilight Lich** | MythicMobs avec phases (stance system), clones (invoke mobs), shield mechanics | Système de phases avancé |
| **Questing Ram** | MythicMobs quest mechanic (check items, transform) | Logique de quête custom |
| **Canopée dense** (obscurité constante) | Datapack biome config (densité de feuillage) + gamerule | Approximation possible |
| **Boggard / Redcap Sapper** | MythicMobs (explosion mechanics, pathfinding) | Standard |

### 5.3 Features difficiles (🔴 Effort significatif)

| Feature | Méthode | Défis |
|---------|---------|-------|
| **Système de progression** (zones verrouillées) | MythicMobs variables + region-based checks +Citizen/Packet events | Logique complexe; Folia region-safety |
| **Boss: Hydra** | MythicMobs multipart (têtes indépendantes), attaques de feu | Système multipart complexe |
| **Boss: Ur-Ghast** | MythicMobs avec phases volantes, minions, pleurs explosives | AI de vol + spawns |
| **Boss: Snow Queen** | MythicMobs vol + dash + piliers de glace | Mouvement 3D complexe |
| **Dark Tower** (structure multi-étages procédurale) | Jigsaw datapack + loot tables + mob spawners | Jigsaw très complexe |
| **Final Castle** | Idem — structure massive avec mécaniques | Plus grosse structure du mod |
| **Knight Phantom** (6 boss simultanés) | MythicMobs avec multi-spawn + coordination | Synchronisation entre mobs |
| **Effets visuels dimension** (ciel, brouillard) | Particles + display entities (approximation) | Impossible de reproduire exactement sans client mod |

### 5.4 Features non-portables (⚫ Incompatible sans client mod)

| Feature | Raison | Alternative |
|---------|--------|-------------|
| **Ciel custom de la dimension** (couleurs, nuages) | Nécessite rendu client-side | Approximation: gamerule + fog particles |
| **Shaders de brouillard** | Client-side rendering | N/A — fog via particles |
| **Animations de portail custom** | Client-side rendering | Particle effects à la place |
| **Rendu d'araignée custom** (8 pattes animées) | Model custom | BetterModel (resource pack) — fonctionne! |

### 5.5 Résumé de faisabilité

```
TOTAL ESTIMÉ (port complet): 3-6 mois (1 développeur)
TOTAL ESTIMÉ (MVP — dimension + 5 mobs + 2 boss): 4-6 semaines

Répartition:
  🟢 Faible effort (60% du contenu): ~3-4 semaines
  🟡 Moyen effort (25% du contenu): ~4-6 semaines  
  🔴 Effort significatif (15% du contenu): ~6-10 semaines
  ⚫ Non-portable (5% — cosmétique): approximation uniquement
```

---

## 6. Stack technique nécessaire

### 6.1 Stack existante (réutilisée)

| Outil | Rôle | Status dans le projet |
|-------|------|----------------------|
| **MythicMobs** | Définition des mobs, AI, skills, spawns | ✅ En place (pattern YAML établi) |
| **BetterModel** | Modèles 3D + animations (Blockbench → resource pack) | ✅ En place |
| **CraftEngine** | Items custom + blocks custom (resource pack) | ✅ En place (137 blocks, 43 items) |

### 6.2 Stack additionnelle nécessaire

| Outil | Rôle | Pourquoi |
|-------|------|----------|
| **Vanilla Datapack** | Dimension, biomes, structures (.nbt), loot tables | Folia-safe, pas de plugin requis, performance native |
| **Folia** (Paper fork) | Serveur multi-threadé region-based | Déjà la cible du projet |
| **MythicMobs Multipart** (ou Display Entities) | Bosses multi-segments (Naga, Hydra) | Si MythicMobs supporte multipart sur Folia |
| **MythicLib / EliteScript** (optionnel) | Logique de progression complexe | Si les MythicMobs skills ne suffisent pas |

### 6.3 Outils NON nécessaires (à éviter)

| Outil | Raison |
|-------|--------|
| ~~Oraxen / ItemsAdder~~ | CraftEngine déjà utilisé dans le projet |
| ~~ProtocolLib~~ | BetterModel gère le resource pack |
| ~~Citizens~~ | Sauf si besoin d'NPCs pour quêtes |
| ~~WorldGuard~~ | Folia gère les régions nativement |
| ~~Multiverse~~ | Dimension gérée par datapack vanilla |

### 6.4 Structure de fichiers proposée

```
alexsmobs-but-twilight/
├── alexsmobs/                    # Projet existant (Alex's Mobs)
│   ├── _core/
│   │   ├── items_registry.yml
│   │   ├── blocks_registry.yml
│   │   └── ...
│   ├── grizzly_bear/
│   └── ...
├── twilight_forest/              # Nouveau: Twilight Forest
│   ├── _core/
│   │   ├── items_registry.yml    # Items TF (glass sword, armor sets, etc.)
│   │   ├── blocks_registry.yml   # Blocks TF (wood variants, decorative)
│   │   ├── sounds_registry.yml   # Sons TF
│   │   └── portal_mechanics.yml  # Logique du portail TF
│   ├── mobs/                     # Mobs passifs + hostiles
│   │   ├── deer/
│   │   │   ├── mob.yml
│   │   │   ├── ai.yml
│   │   │   ├── skills.yml
│   │   │   ├── drops.yml
│   │   │   ├── spawn.yml
│   │   │   ├── sounds.yml
│   │   │   ├── variables.yml
│   │   │   └── model/model.yml
│   │   ├── boar/
│   │   ├── forest_spider/
│   │   ├── redcap/
│   │   ├── wraith/
│   │   └── ...
│   ├── bosses/                   # Bosses avec mécaniques complexes
│   │   ├── naga/
│   │   ├── lich/
│   │   ├── hydra/
│   │   ├── ur_ghast/
│   │   ├── snow_queen/
│   │   └── ...
│   ├── datapack/                 # Dimension, biomes, structures
│   │   ├── data/
│   │   │   ├── twilight_forest/
│   │   │   │   ├── dimension_type/
│   │   │   │   │   └── twilight_forest.json
│   │   │   │   ├── dimension/
│   │   │   │   │   └── twilight_forest.json
│   │   │   │   ├── worldgen/
│   │   │   │   │   ├── biome/
│   │   │   │   │   │   ├── twilight_forest.json
│   │   │   │   │   │   ├── twilight_swamp.json
│   │   │   │   │   │   ├── dark_forest.json
│   │   │   │   │   │   └── ...
│   │   │   │   │   ├── configured_structure/
│   │   │   │   │   ├── structure_set/
│   │   │   │   │   ├── template_pool/
│   │   │   │   │   └── noise_settings/
│   │   │   │   ├── structures/   # .nbt files
│   │   │   │   │   ├── naga_courtyard.nbt
│   │   │   │   │   ├── lich_tower/
│   │   │   │   │   └── ...
│   │   │   │   └── loot_table/
│   │   └── pack.mcmeta
│   └── _docs/
│       └── ...
└── _docs/
    └── MOD_WORLDGEN_ANALYSIS.md  # Ce document
```

---

## 7. Approche de port recommandée

### 7.1 Stratégie globale: "Vague progressive"

Ne pas attaquer le mod en bloc. Porter par vagues concentriques, chaque vague ajoutant une couche de gameplay:

```
Vague 0: Infrastructure (dimension vide, portail, items de base)
    ↓
Vague 1: Écosystème passif (biomes peuplés, mobs passifs, structures décoratives)
    ↓
Vague 2: Danger de base (mobs hostiles, hedge mazes, hollow trees)
    ↓
Vague 3: Premier boss (Naga → Lich → Minoshroom)
    ↓
Vague 4: Bosses avancés (Hydra → Knight Phantom → Ur-Ghast)
    ↓
Vague 5: Contenu endgame (Yeti → Snow Queen → Final Castle)
    ↓
Vague 6: Système de progression complet (zones verrouillées, magic map)
```

### 7.2 Approche technique par vague

#### Vague 0 — Infrastructure (1 semaine)

1. **Datapack dimension**:
   - Créer `dimension_type/twilight_forest.json` (couleur du ciel, hauteur, etc.)
   - Créer `dimension/twilight_forest.json` (générateur avec noise settings custom)
   - Tester que `/execute in twilight_forest:twilight_forest run tp ...` fonctionne

2. **Portail TF**:
   - Mécanique: placer un bloc d'eau dans un cadre de fleurs/grass → activer
   - Implémentation: MythicMobs custom event listener + particle effects
   - Alternative simplifiée: commande `/twilight` pour téléporter

3. **Items de base** (CraftEngine):
   - Twilight Oak Wood variants (6 types × 5 formes = 30 items)
   - Darkwood variants
   - Materials (twilight oak sapling, etc.)

4. **Blocks de base** (CraftEngine):
   - Wood blocks, leaves, planks, stairs, slabs
   - Portal frame blocks

#### Vague 1 — Écosystème passif (2-3 semaines)

1. **Biomes** (datapack):
   - Commencer avec 4 biomes de base: Twilight Forest, Swamp, Clearing, Lake
   - Configurer la densité d'arbres (canopée)
   - Spawn rules pour les mobs passifs

2. **Mobs passifs** (MythicMobs):
   - Porter en priorité: **Deer**, **Boar** (montable!), **Squirrel**, **Bunny**, **Raven**, **Tiny Bird**
   - Pattern: 1 jour par mob (model BetterModel + mob.yml + spawn.yml + ai.yml)
   - Total: ~6 mobs × 1 jour = 1 semaine

3. **Structures décoratives** (datapack .nbt):
   - Hollow Trees (grands arbres creux avec coffre)
   - Druid Groves (petits autels)

#### Vague 2 — Danger (2 semaines)

1. **Mobs hostiles** (MythicMobs):
   - **Forest Spider** (aime la dark forest, aggressif la nuit)
   - **Redcap** (gobelin, lance des items)
   - **Redcap Sapper** (explosif)
   - **Skeleton Druid** (lance des potions de poison)
   - **Wraith** (vole, intangible)
   - **Hostile Wolf / Mist Wolf** (meute)
   - Total: ~6-8 mobs × 1-2 jours = 2 semaines

2. **Structures dangereuses** (datapack):
   - Hedge Mazes (labyrinthes avec araignées + coffre central)
   - Small ruins

#### Vague 3 — Premiers bosses (3-4 semaines)

1. **Naga** (MythicMobs):
   - Boss serpent multi-segments
   - Mécaniques: charge, régénération, segments destructibles
   - Implémentation: MythicMobs avec `multipart` ou display entities pour les segments
   - Loot: Naga Scale (CraftEngine item) → permet de crafting Naga armor

2. **Twilight Lich** (MythicMobs):
   - Boss à phases dans sa tour
   - Mécaniques: clones (summon), bouclier (invulnérable jusqu'à destruction des clones), projectiles
   - Loot: Lich Trophy, scepters

3. **Structures** (datapack .nbt):
   - Naga Courtyard (structure circulaire en pierre)
   - Lich Tower (tour multi-étages)

#### Vague 4-5 — Bosses avancés (6-8 semaines)

Bosses avec mécaniques plus complexes. Chacun nécessite:
- Étude du code source GitHub (LGPL) pour comprendre les mécaniques exactes
- Implémentation MythicMobs avec skills complexes (phases, summons, special attacks)
- Structure associée en .nbt
- Loot table custom

#### Vague 6 — Système de progression (2-3 semaines)

Le système de progression (zones verrouillées) est la signature du mod:
- **Problème**: En vanilla/Folia, il n'y a pas de mécanique native pour empêcher un joueur d'entrer dans une zone
- **Solution proposée**: 
  - MythicMobs variables globales (variable stockée sur le joueur ou dans un scoreboard)
  - Region-based checks: certains mobs/structures ne spawn que si le boss précédent est vaincu
  - Alternative: barrières de particules qui se dissipent après un boss tué
  - La Magic Map (item custom) affiche les zones débloquées

### 7.3 Lecture du code source

La license LGPL permet de lire le code source pour comprendre les mécaniques exactes:

```bash
# Cloner le repo pour référence
git clone https://github.com/TwilightFlower/TwilightForest.git

# Structure du code source:
# src/main/java/twilightforest/
#   ├── entity/          # Tous les mobs et bosses
#   │   ├── boss/        # Naga, Lich, Hydra, etc.
#   │   ├── passive/     # Deer, Boar, Squirrel, etc.
#   │   └── hostile/     # Spider, Redcap, Wraith, etc.
#   ├── world/           # World generation
#   │   ├── components/  # Structure pieces
#   │   └── structures/  # Structure definitions
#   ├── item/            # Items
#   └── block/           # Blocks
```

**Note légale**: La réimplémentation server-side en MythicMobs/YAML/datapack ne constitue pas un dérivé du code source Java LGPL. On s'inspire des mécaniques (gameplay n'est pas copyrightable en soi) mais on n'inclut pas le code original.

---

## 8. Plan de migration Folia

### 8.1 Compatibilité Folia — Analyse par composant

| Composant | Folia-Safe? | Notes |
|-----------|-------------|-------|
| **Datapack dimension/biomes** | ✅ Oui | Vanilla worldgen, thread-safe nativement |
| **Datapack structures (.nbt)** | ✅ Oui | Vanilla structure features, région-local |
| **MythicMobs spawns** | ✅ Oui | Déjà region-local dans le projet AM (`SpawnRate`, `CheckDistance`) |
| **MythicMobs mob AI** | ✅ Oui | AI s'exécute sur le thread de la région de l'entité |
| **MythicMobs skills** | ⚠️ Attention | Skills cross-region (ex: téléportation entre dimensions) doivent utiliser `Bukkit.getGlobalRegionScheduler()` |
| **BetterModel** | ✅ Oui | Resource pack, pas d'exécution server-side |
| **CraftEngine** | ✅ Oui | Items/blocks, pas de tick server |
| **Portail TF** | ⚠️ Attention | Téléportation inter-dimension = `GlobalRegionScheduler` |
| **Système de progression** | ⚠️ Attention | Variables globales partagées entre régions |

### 8.2 Patterns Folia-safe à respecter

Le projet utilise déjà ces patterns (visible dans `spawn.yml`):

```yaml
# Pattern Folia-safe pour spawns (déjà utilisé dans Alex's Mobs)
naga_spawner:
  Type: naga
  SpawnRate: 4                    # ticks entre checks (region-local)
  MaxMobs: 1                      # 1 boss par structure
  CheckDistance: 64               # region-local check
  Conditions:
    - biome{b=TWILIGHT_FOREST}    # biome custom du datapack
    - structure{stwilight_forest:naga_courtyard}  # structure check
    - playerdistance{d=>32}       # pas trop près des joueurs
```

```yaml
# Pattern Folia-safe pour boss skills
naga_charge:
  Conditions:
    - hasstance{stance=charging} false
  Skills:
    - setstance{stance=charging}
    - jump{velocity=1.5} @self
    - delay 20
    - rush{speed=2.0;duration=40} @targetlocation
    - delay 40
    - setstance{stance=idle}
  # Tout est region-local: @self, @targetlocation sont dans la même région
```

```yaml
# Pattern pour téléportation inter-dimension (Folia: GlobalRegionScheduler)
# Doit être géré via un wrapper plugin, pas directement en MythicMobs skill
# Alternative: utiliser /execute in twilight_forest:twilight_forest run tp ...
```

### 8.3 Étapes de migration Folia

#### Phase 1: Tests de base (1 jour)
1. Vérifier que le datapack dimension se charge sur Folia
2. Téléporter un joueur dans la dimension
3. Vérifier que les chunks se génèrent correctement (multi-threadé)

#### Phase 2: Mobs (1 jour par mob)
1. Tester chaque mob MythicMobs sur Folia
2. Vérifier que l'AI s'exécute sur le bon thread de région
3. Confirmer que les spawns respectent les limites de région

#### Phase 3: Bosses (2-3 jours par boss)
1. Tester les mécaniques complexes (phases, summons, multipart)
2. Vérifier que les display entities (pour multipart) fonctionnent en multi-thread
3. Tester les loot tables

#### Phase 4: Structures (1 jour par structure)
1. Vérifier que les .nbt se génèrent correctement
2. Confirmer que les mobs spawn dans les structures
3. Tester les coffres / loot tables

#### Phase 5: Portail & progression (3-5 jours)
1. Implémenter la téléportation via GlobalRegionScheduler
2. Tester le système de variables de progression
3. Vérifier que les zones verrouillées fonctionnent

### 8.4 Checklist Folia avant release

- [ ] Tous les spawns utilisent `region-local` checks
- [ ] Aucun accès direct à `Bukkit.getServer()` sans scheduler
- [ ] Téléportations inter-dimension via `GlobalRegionScheduler`
- [ ] Variables globales via scoreboard (thread-safe) ou datastore externe
- [ ] Pas de `PlayerTeleportEvent` bloquant (peut causer des deadlocks sur Folia)
- [ ] Structures testées en multi-joueurs (régions concurrentes)
- [ ] Performance: pas de boucle sur toutes les entités du monde (utiliser region queries)

---

## 9. Alternatives et plans B

### 9.1 Si Twilight Forest est trop ambitieux

**Plan B1: Quark (modulaire, quick wins)**

Quark (Vazkii, LGPL-3.0, 25M+ downloads) est idéal pour un port incrémental:
- Porter 1 module à la fois (World, Mobs, Tools, etc.)
- Chaque module est indépendant et apporte de la valeur immédiatement
- World gen: Stone variants (Marble, Limestone), Cave Crystals, Fairy Rings, Blossom Trees
- Mobs: Stonelings (silverfish-like for ores), Foxhounds (nether wolves), Crabs
- Timeline réaliste: 2-3 semaines pour un MVP significatif

**Plan B2: Alex's Caves (synergie maximale, mais pas de source)**

Malgré le manque de source, Alex's Caves (même auteur que AM) serait le complément naturel:
- 8 cave biomes avec mobs, structures et items uniques
- Même style artistique et design philosophy que Alex's Mobs
- Approche: reverse-engineering from gameplay + assets CurseForge
- Risque légal: assets sont All Rights Reserved — ne peut pas réutiliser les textures
- Solution: recréer les textures/modèles en Blockbench (inspiration, pas copie)

**Plan B3: Aquaculture 2 (port simple, valeur immédiate)**

Pour un quick win:
- Fishing overhaul (MIT license, 10M+ downloads)
- Ajoute: 30+ nouveaux poissons, neufs outils de pêche, loot tables étendues
- Port: ~1 semaine (loot tables + CraftEngine items + MythicMobs pour poissons custom)
- Synergie: les mobs aquatiques d'Alex's Mobs + Aquaculture = écosystème maritime complet

### 9.2 Recommandation de sequencing

Si l'équipe a du temps:

```
Étape 1: Aquaculture 2 (1 semaine) → quick win, contenu maritime
Étape 2: Quark World module (2 semaines) → biomes enrichis, cave features
Étape 3: Twilight Forest Vague 0-2 (6 semaines) → dimension + mobs de base
Étape 4: Twilight Forest Vague 3+ (open-ended) → bosses et progression
```

---

## Annexe A: Références

- **Twilight Forest GitHub**: https://github.com/TwilightFlower/TwilightForest
- **Twilight Forest Wiki**: https://twilightforest.fandom.com/wiki/The_Twilight_Forest_Wiki
- **Twilight Forest CurseForge**: https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest
- **Quark GitHub**: https://github.com/VazkiiMods/Quark
- **Folia docs**: https://docs.papermc.io/folia
- **MythicMobs Folia**: https://git.lumine.io/mythiccraft/MythicMobs/-/wikis/Folia-Support
- **BetterModel**: https://www.spigotmc.org/resources/bettermodel.110956/

## Annexe B: Détails techniques — Dimension Datapack

Exemple de structure pour le datapack de la dimension:

```json
// data/twilight_forest/dimension_type/twilight_forest.json
{
  "ultrawarm": false,
  "natural": true,
  "piglin_safe": false,
  "respawn_anchor_works": false,
  "bed_works": true,
  "has_raids": false,
  "has_skylight": false,
  "has_ceiling": false,
  "coordinate_scale": 1.0,
  "ambient_light": 0.1,
  "logical_height": 256,
  "effects": "minecraft:the_end",
  "infiniburn": "#minecraft:infiniburn_overworld",
  "min_y": -64,
  "height": 384,
  "monster_spawn_light_level": {
    "type": "minecraft:uniform",
    "value": { "min_inclusive": 0, "max_inclusive": 7 }
  },
  "monster_spawn_block_light_limit": 0,
  "fixed_time": 13000
}
```

```json
// data/twilight_forest/dimension/twilight_forest.json
{
  "type": "twilight_forest:twilight_forest",
  "generator": {
    "type": "minecraft:noise",
    "settings": "twilight_forest:twilight_forest",
    "biome_source": {
      "type": "minecraft:multi_noise",
      "biomes": [
        { "biome": "twilight_forest:twilight_forest", "parameters": { ... } },
        { "biome": "twilight_forest:twilight_swamp", "parameters": { ... } },
        { "biome": "twilight_forest:dark_forest", "parameters": { ... } }
      ]
    }
  }
}
```

---

*Document généré dans le cadre du projet AlexMobButIt'sServerside — réimplémentation server-side pour Folia.*
