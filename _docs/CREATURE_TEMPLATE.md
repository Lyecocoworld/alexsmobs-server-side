# Template de créature

Utiliser ce template pour chaque nouvelle créature. Remplacer `[mob_name]` par le nom snake_case de la créature.

---

## Analyse préalable (avant de coder)

Pour chaque créature, documenter d'abord dans l'en-tête du `mob.yml` :

```
# Source: Entity<Name>.java (XXX lignes, branch 1.20)
# Classification: CX (complexité), PX (priorité)
# Comportements reproduits:
#   - [x] Comportement 1
#   - [x] Comportement 2
# Équivalences mod → plugin:
#   Pattern Forge → Équivalent MythicMobs/BetterModel/CraftEngine
```

---

## Structure de fichiers

```
alexsmobs/[mob_name]/
├── mob.yml
├── skills.yml
├── ai.yml
├── drops.yml
├── spawn.yml
├── sounds.yml
├── variables.yml
└── model/
    └── model.yml
```

---

## mob.yml (template)

```yaml
# ════════════════════════════════════════════════════════════════
# [MOB DISPLAY NAME] (Alex's Mobs)
# Source: Entity<Name>.java (XXX lignes, branch 1.20)
# Classification: CX (complexité), PX (priorité)
# ─────────────────────────────────────────────────────────────────
# Comportements reproduits:
#   - [x] Comportement clé 1
#   - [x] Comportement clé 2
# ─────────────────────────────────────────────────────────────────
[mob_name]:
  Type: <vanilla_entity>           # Voir table des types vanilla
  Display: '<display_name>'
  Modules:
    ThreatTable: true
    AIGoalSelectors: true
    AITargetSelectors: true
    DamageModifiers: true
    ImmunityTable: true
  Health: <value>
  Damage: <value>
  KnockbackResistance: <value>
  MovementSpeed: <value>
  Faction: creature_[mob_name]
  Options:
    PreventOtherDrops: false
    Persistent: true
    FollowRange: 32
    Silent: true                   # Sons custom via sounds.yml
    Despawn: true
    Ageable:
      BabyAge: <ticks_to_adult>
  AIGoalSelectors:
    - clear
    - [mob_name]_ai
  AITargetSelectors:
    - clear
    - [mob_name]_targets
  DamageModifiers:
    - <damage_type> <multiplier>   # Ex: STING 0.0 = immunisé
  Drops:
    - [mob_name]_drops
  Skills:
    - onTimer:20                    # Tick principal (Folia-safe)
    - skill:[mob_name]_tick{} @self
```

---

## skills.yml (template)

```yaml
#
# [MOB NAME] — Skills
# ─────────────────────────────────────────────────────────────────
# Chaque comportement = 1 skill atomique.
# Les orchestrators combinent les atomiques.
# ─────────────────────────────────────────────────────────────────

# === ORCHESTRATORS ===

[mob_name]_tick:
  # Tick principal — toutes les 20 ticks
  Skills:
    - skill:[mob_name]_state_update @self

# === STATE MANAGEMENT ===

[mob_name]_state_update:
  Skills:
    - skill:[mob_name]_check_snow @self
      Conditions:
        - variable{var=[mob_name].snowTimer;value=0}
    - skill:[mob_name]_check_eating @self
      Conditions:
        - hasitem{m=[food_item]}

# === ATTACKS ===

[mob_name]_attack:
  Skills:
    - damage{amount=<value>} @target
    - effect:particle{p=CRIT;a=10;s=0.5}
    - sound{sound=[mob_name].attack;s=1.0;p=1.0}

# === INTERACTIONS ===

[mob_name]_on_interact:
  Skills:
    - ...
```

---

## ai.yml (template)

```yaml
#
# [MOB NAME] — AI Goals & Targets
# ─────────────────────────────────────────────────────────────────

# === AI GOALS (comportement) ===
# Priorité: 0 (le plus important) → N (le moins important)

[mob_name]_ai:
  Goals:
    # 0 — Survival
    - float{p=0}

    # 1 — Combat
    - melee{p=1;s=1.25}            # speed multiplier
      Conditions:
        - stance{s=!sitting}
    - panic{p=1;s=2.0}
      Conditions:
        - baby{}

    # 2 — Follow
    - followparent{p=2;s=1.25}
    - followowner{p=2;s=1.2;m=5.0;l=2.0}
      Conditions:
        - variable{var=[mob_name].tamed;value=true}

    # 3 — Breeding
    - breed{p=3;s=1.0}

    # 4 — Idle
    - randomstroll{p=4;s=0.75}
    - lookatplayer{p=5;r=6}
    - randomlookaround{p=5}

# === TARGET SELECTORS (cibles) ===

[mob_name]_targets:
  TargetSelectors:
    - clear
    - ownerhurtbytarget{}
    - ownerhurttarget{}
    - hurtbytarget{}
    - attackers
```

---

## drops.yml (template)

```yaml
#
# [MOB NAME] — Drops
# ─────────────────────────────────────────────────────────────────

[mob_name]_drops:
  Conditions:
    - playerkill{}
  Drops:
    # Vanilla drops
    - <vanilla_item> 1-3 0.5

    # Custom drops (CraftEngine items)
    - [mob_name]_pelt 1 0.1
      # CraftEngine item reference

    # Experience
    - exp{amount=5-10} 1
```

---

## spawn.yml (template)

```yaml
#
# [MOB NAME] — Spawn configuration
# ─────────────────────────────────────────────────────────────────

[mob_name]_spawner:
  Type: [mob_name]
  SpawnRate: <rate>
  MaxMobs: <max_per_spawner>
  CheckTime: true
  CheckDistance: 64
  Conditions:
    # Biome
    - biome{b=<biome_list>}

    # Height
    - height{h=<min>to<max>}

    # Light
    - lightlevel{l=<min>to<max>}

    # Block below
    - blocking{m=<block_list>} @origin

    # World
    - worldtype{wt=OVERWORLD}
```

---

## sounds.yml (template)

```yaml
#
# [MOB NAME] — Sons
# ─────────────────────────────────────────────────────────────────
# Sons référencés dans le resource pack.
# Format: alexsmobs_<mob_name>_<type>

[mob_name]_sounds:
  ambient:
    sound: "alexsmobs:[mob_name].ambient"
    volume: 1.0
    pitch_min: 0.8
    pitch_max: 1.2
    interval: 200-400              # ticks entre chaque son ambient
  hurt:
    sound: "alexsmobs:[mob_name].hurt"
    volume: 1.0
    pitch_min: 0.8
    pitch_max: 1.2
  death:
    sound: "alexsmobs:[mob_name].death"
    volume: 1.0
    pitch_min: 0.8
    pitch_max: 1.2
  # Sons spéciaux (attaques, etc.)
  attack:
    sound: "alexsmobs:[mob_name].attack"
    volume: 1.0
    pitch_min: 0.9
    pitch_max: 1.1
```

---

## variables.yml (template)

```yaml
#
# [MOB NAME] — Variables persistantes
# ─────────────────────────────────────────────────────────────────
# Ces variables sont stockées sur l'entité via MythicMobs.
# Persistent: true dans mob.yml assure la sauvegarde NBT.

variables:
  # State flags
  [mob_name].tamed:
    type: BOOLEAN
    default: false
    desc: "L'entite est apprivoisee"

  [mob_name].snowy:
    type: BOOLEAN
    default: false
    desc: "L'entite est couverte de neige"

  # Timers (en ticks)
  [mob_name].snowTimer:
    type: INTEGER
    default: 0
    desc: "Compteur avant verification neige"

  # Données runtime
  [mob_name].salmonThrowerID:
    type: STRING
    default: ""
    desc: "UUID du joueur qui a lance le saumon (pour taming)"
```

---

## model/model.yml (template)

```yaml
#
# [MOB NAME] — BetterModel configuration
# ─────────────────────────────────────────────────────────────────

model:
  bettermodel_id: "alexsmobs:[mob_name]"
  type: "<base_entity>"             # zombie, skeleton, armor_stand...
  hitbox:
    width: <width>
    height: <height>
  animations:
    idle: "animation.[mob_name].idle"
    walk: "animation.[mob_name].walk"
    run: "animation.[mob_name].run"
    # ... toutes les animations nécessaires
  animation_state_mapping:
    # MythicMobs stance → BetterModel animation
    <stance_name>: "animation.[mob_name].<anim>"
  variants:
    normal:
      texture: "alexsmobs:textures/entity/[mob_name]/normal.png"
    # ... variantes
  scale:
    adult: 1.0
    baby: 0.5
```

---

## Types vanilla recommandés par catégorie

| Catégorie | Type vanilla | Pourquoi |
|-----------|-------------|----------|
| Terrestre quadripède | `polar_bear`, `cow`, `pig`, `wolf` | Hitbox, animations, breeding |
| Terrestre bipède | `zombie`, `skeleton` | Pose, items en main |
| Volant | `parrot`, `bat`, `bee`, `phantom` | Move controller volant |
| Aquatique | `cod`, `salmon`, `squid`, `dolphin` | Move controller aquatique |
| Hostile | `zombie`, `skeleton`, `spider` | AI hostile de base |
| Tamable | `wolf`, `cat`, `parrot` | Système de taming |
| Passif | `cow`, `sheep`, `pig` | AI passive de base |
| Boss | `warden`, `ender_dragon`, `wither` | Health, scale |

---

## Checklist de complétion

- [ ] mob.yml — Définition complète avec en-tête documenté
- [ ] skills.yml — Tous les comportements en skills atomiques
- [ ] ai.yml — AIGoals + AITargetSelectors complets
- [ ] drops.yml — Tables de drop (vanilla + custom)
- [ ] spawn.yml — Spawner avec conditions de biome
- [ ] sounds.yml — Tous les sons référencés
- [ ] variables.yml — Variables persistantes documentées
- [ ] model/model.yml — Mapping BetterModel complet
- [ ] Test in-game: `/mm m spawn [mob_name]`
- [ ] Folia check: Pas de timer global, pas de scan monde
