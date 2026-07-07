# Alex's Mobs — Server-Side Folia Reimplementation

Réimplémentation **100% server-side** d'Alex's Mobs pour serveurs **Folia**, utilisant **MythicMobs** + **BetterModel** + **CraftEngine**.

> Ce n'est **pas** un port Forge. C'est une réimplémentation native qui reproduit fidèlement chaque comportement du mod original via les APIs des plugins ci-dessus. Le code source de [Alex's Mobs](https://github.com/AlexModGuy/AlexsMobs) est étudié pour comprendre chaque mécanique, mais **aucun code n'est copié**.

---

## Pourquoi ce projet ?

Alex's Mobs est un mod client+serveur. Les joueurs doivent l'installer côté client pour voir les modèles 3D et animations. Ce projet élimine ce besoin :

- **Aucun mod client requis** — tout passe par le resource pack généré par BetterModel + CraftEngine
- **100% Folia** — profite du multithreading par région
- **Scalable** — pensé pour des milliers de créatures et des centaines de joueurs
- **Modulaire** — chaque créature est un dossier indépendant

---

## Stack technique

| Plugin | Rôle | Priorité |
|--------|------|----------|
| **MythicMobs** | IA, skills, drops, spawners, variantes | 🔴 Primaire |
| **BetterModel** | Modèles 3D Blockbench + animations | 🔴 Primaire |
| **CraftEngine** | Items/blocks custom, nourriture, décorations | 🟡 Secondaire |
| **Nexo** | Resource pack merge (dernier recours) | 🟢 Tertiaire |
| **PlaceholderAPI** | Variables exposées aux autres plugins | 🟡 Support |
| **WorldGuard/WorldEdit** | Protection, spawning zones | 🟡 Support |
| **ProtocolLib** | Packets custom si nécessaire | 🟢 Optionnel |
| **PacketEvents** | Détection packets avancée | 🟢 Optionnel |

---

## Architecture du projet

```
alexsmobs/
├── grizzly_bear/          # Chaque créature = 1 dossier totalement indépendant
│   ├── mob.yml            # Définition MythicMobs (type, health, display, etc.)
│   ├── skills.yml         # Toutes les compétences (attaques, capacités,特效)
│   ├── ai.yml             # AIGoals + ThreatTable
│   ├── drops.yml          # Tables de loot
│   ├── spawn.yml          # Conditions de spawn (biomes, lumière, groupes)
│   ├── sounds.yml         # Banque de sons (.ogg → resource pack)
│   ├── variables.yml      # Variables persistantes (honeyed, snowy, etc.)
│   └── model/             # Référence BetterModel (nom du modèle, animations)
│       └── model.yml      # Mapping animation → état MythicMobs
│
├── alligator_snapping_turtle/
├── elephant/
├── ...
│
_core/                     # Infrastructure partagée
├── globals.yml            # Skills/conditions globaux réutilisables
├── placeholders.yml       # PAPI placeholders
├── sounds_registry.yml    # Registry global des sons
├── items_registry.yml     # Registry global des items CraftEngine
└── biome_tags.yml         # Tags de biomes pour spawns

_docs/                     # Documentation
├── CONVENTIONS.md         # Standards de codage du projet
├── CREATURE_TEMPLATE.md   # Template pour créer une nouvelle créature
├── FOLIA_OPTIMIZATION.md  # Patterns d'optimisation Folia
├── CREATURE_CATALOG.md    # Catalogue des 120 créatures avec priorités
├── MYTHICMOBS_PATTERNS.md # Patterns MythicMobs réutilisables
└── STACK_NOTES.md         # Notes sur le stack technique
```

---

## Créatures — État d'avancement

Voir [`_docs/CREATURE_CATALOG.md`](_docs/CREATURE_CATALOG.md) pour le catalogue complet des 120 créatures.

### Niveaux de priorité

| Niveau | Description | Critères |
|--------|-------------|----------|
| **P0** | Critique (signature mobs) | GrizzlyBear, Elephant, Crocodile, CrimsonMosquito, VoidWorm |
| **P1** | Haute (gameplay-defining) | Mobs avec mécaniques uniques majeures |
| **P2** | Standard | Mobs terrestres/aquatiques standards |
| **P3** | Simple | Mobs avec peu de mécaniques (passeifs décoratifs) |

### Niveaux de complexité

| Niveau | Description | Exemples |
|--------|-------------|----------|
| **C1** | Simple AI (wander + melee) | Gazelle, Raccoon |
| **C2** | AI modérée (states, breeding, tame) | GrizzlyBear, Crow |
| **C3** | AI complexe (multipart, special attacks) | Anaconda, GiantSquid |
| **C4** | AI très complexe (boss-tier) | VoidWorm, Murmur, CachalotWhale |

---

## Installation serveur

### Prérequis

1. **Folia** (recommandé Canvas/Folia 26.1.x)
2. **MythicMobs** 5.12+
3. **BetterModel** 3.2+
4. **CraftEngine** (dernière version)
5. **PlaceholderAPI** (dernière version)

### Déploiement

1. Copier le contenu de `alexsmobs/` dans `plugins/MythicMobs/Mobs/`, `plugins/MythicMobs/Skills/`, etc.
2. Copier les modèles BetterModel dans le dossier resource pack
3. Copier les définitions CraftEngine dans `plugins/CraftEngine/`
4. `/mm reload`

### Vérification

```
/mm mobs list    # Vérifier que toutes les créatures sont chargées
/mm i info grizzly_bear    # Tester une créature
```

---

## Optimisation Folia

Ce projet est conçu dès le départ pour le modèle multithread de Folia. Voir [`_docs/FOLIA_OPTIMIZATION.md`](_docs/FOLIA_OPTIMIZATION.md) pour les détails.

Principes directeurs :

- ✅ **Événements** déclenchent les skills, pas des boucles globales
- ✅ **Cache local** à chaque entité via MythicMobs variables
- ✅ **Timers indépendants** par entité (pas de scheduler global)
- ✅ **Threat Tables** natives MythicMobs (pas de recherche globale)
- ✅ **Skills conditionnels** — exécutés uniquement quand nécessaire

- ❌ Pas de `BukkitScheduler` (synchrone global)
- ❌ Pas de scan de monde (`getNearbyEntities` aveugle)
- ❌ Pas de pathfinding coûteux non maîtrisé
- ❌ Pas de boucles sur toutes les entités

---

## Crédits

- **Original** : [Alex's Mobs](https://www.curseforge.com/minecraft/mc-mods/alexs-mobs) par AlexModGuy — sans lequel ce projet n'existerait pas
- **Réimplémentation** : Ce projet ne copie aucun code source. Il étudie les comportements et les reproduit via les APIs MythicMobs/BetterModel/CraftEngine.

## Licence

Alex's Mobs est distribué sous **GPL-3.0-only**. Cette licence permet d'inclure et redistribuer les assets originaux (modèles Blockbench, textures, sons) tant que le projet dérivé reste sous GPL-3.0.

**Notre projet inclut donc les assets d'Alex's Mobs** pour permettre les tests en conditions réelles. Dans le projet final, les assets seront séparés dans un dossier dédié (`assets/`) pour faciliter la maintenance et d'éventuelles mises à jour depuis le mod original.

- Le **code** de ce projet (fichiers YAML MythicMobs, configs BetterModel/CraftEngine) est original — aucune ligne du code Java d'Alex's Mobs n'est copiée.
- Les **assets** (modèles, textures, sons) proviennent du mod original et sont utilisés conformément à la GPL-3.0.
- Le projet dérivé est distribué sous **GPL-3.0-only**.
