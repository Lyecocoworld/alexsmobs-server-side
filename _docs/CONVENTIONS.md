# Conventions du projet

## 1. Nommage

### Dossiers de créatures

```
snake_case en anglais, singulier ou pluriel selon le mob
alexsmobs/grizzly_bear/
alexsmobs/alligator_snapping_turtle/
alexsmobs/void_worm/
```

### Fichiers par créature

Chaque dossier de créature contient TOUJOURS ces fichiers, même si certains sont presque vides :

| Fichier | Rôle | Obligatoire |
|---------|------|-------------|
| `mob.yml` | Définition MythicMobs de l'entité | ✅ |
| `skills.yml` | Toutes les compétences MythicMobs | ✅ |
| `ai.yml` | AIGoals et ThreatTable | ✅ |
| `drops.yml` | Tables de drop | ✅ |
| `spawn.yml` | Spawners et conditions de spawn | ✅ |
| `sounds.yml` | Sons custom (registry + références) | ✅ |
| `variables.yml` | Variables MythicMobs (state) | ✅ |
| `model/model.yml` | Mapping BetterModel | ✅ |

### IDs MythicMobs

Tous les IDs d'une créature sont préfixés par le nom du mob :

```yaml
# grizzly_bear/skills.yml
grizzly_bear_maul:
  Skills:
    - ...

grizzly_bear_swipe_left:
  Skills:
    - - ...
```

Pourquoi ? Évite les collisions avec les 120 créatures et facilite le débogage (`/mm i info grizzly_bear_maul`).

### Variables MythicMobs

```yaml
# Toujours préfixées par le nom du mob
<grizzly_bear.honeyed>       # booléen — l'ours a mangé du miel
<grizzly_bear.snowy>         # booléen — l'ours est couvert de neige
<grizzly_bear.standing>      # booléen — l'ours est debout
```

---

## 2. Structure YAML

### mob.yml

```yaml
grizzly_bear:
  Type: polar_bear        # Vanilla entity de base (disguise)
  Display: '&7Grizzly Bear'
  Modules:
    ThreatTable: true
    AIGoalSelectors: true
    AITargetSelectors: true
    DamageModifiers: true
   _immunityTable: true
  Health: 55
  Damage: 8
  KnockbackResistance: 0.6
  MovementSpeed: 0.25
  Faction: creature_grizzly  # Pour inter-mobs AI
  Options:
    PreventOtherDrops: false
    Persistent: true           # Survit aux restarts
    FollowRange: 32
    Silent: true               # Pas de sons vanilla, on utilise les customs
    Despawn: true
    Ageable: true              # Peut être baby/adult
  AIGoalSelectors:
    - clear
    - grizzly_bear_ai
  AITargetSelectors:
    - clear
    - grizzly_bear_targets
  DamageModifiers:
    - STING 0.0               # Immunisé au sting (wasp/bee)
    - IN_WALL 0.0             # Immunisé suffocation
  Drops:
    - grizzly_bear_drops
  NBT:
    # Variant snowy automatiquement via skills
    PurpurBlobAltSkins: 1
```

### skills.yml

Découper chaque comportement en petites compétences atomiques. Une compétence = une action.

```yaml
# ❌ MAUVAIS — tout dans un seul skill
grizzly_bear_attack:
  Skills:
    - damage{amount=8}
    - knockback{velocity=0.5}
    - setanimation{id=maul}
    - particle{...}
    - playsound{...}

# ✅ BON — compétences atomiques réutilisables
grizzly_bear_maul_damage:
  Skills:
    - damage{amount=8}

grizzly_bear_maul_knockback:
  Skills:
    - knockback{velocity=0.5}

grizzly_bear_set_maul_animation:
  Skills:
    - setstance{stance=mauling}
```

### skills.yml — Appel de skills

```yaml
# Un orchestrator combine les atomiques
grizzly_bear_maul:
  Skills:
    - skill:grizzly_bear_maul_damage
    - skill:grizzly_bear_maul_knockback
    - skill:grizzly_bear_set_maul_animation
    - skill:grizzly_bear_maul_particles
    - skill:grizzly_bear_maul_sound
```

### Skills conditionnels

```yaml
grizzly_bear_tick:
  # Tick principal — exécuté toutes les 10 ticks via onTick:
  Skills:
    - skill:grizzly_bear_snow_check{} @self
      Conditions:
        - ¢varin{var=grizzly_bear.snowTimer;value=0}
    - skill:grizzly_bear_eat_check{} @self
      Conditions:
        -iteminhand{mat=any;action=HAS}
```

---

## 4. Modèles BetterModel

### model/model.yml

```yaml
model:
  bettermodel_id: "alexsmobs:grizzly_bear"
  type: "zombie"              # Base entity type for BetterModel
  hitbox:
    width: 1.7
    height: 1.8
  animations:
    idle: "animation.grizzly_bear.idle"
    walk: "animation.grizzly_bear.walk"
    # ... etc
  animation_state_mapping:
    # Quand MythicMobs met stance=mauling → BetterModel joue l'animation maul
    mauling: "animation.grizzly_bear.maul"
    sniffing: "animation.grizzly_bear.sniff"
    swiping_left: "animation.grizzly_bear.swipe_left"
    swiping_right: "animation.grizzly_bear.swipe_right"
    standing: "animation.grizzly_bare.stand"
    sitting: "animation.grizzly_bear.sit"
    eating: "animation.grizzly_bear.eat"
    sleeping: "animation.grizzly_bear.sleep"
  variants:
    # Variants de texture (Variants dans le mod original)
    normal:
      texture: "alexsmobs:textures/entity/grizzly_bear/grizzlybear.png"
    snowy:
      texture: "alexsmobs:textures/entity/grizzly_bear/grizzlybearsnow.png"
    freddy:
      texture: "alexsmobs:textures/entity/grizzly_bear/freddy.png"
      condition: "april_fools_flag >= 2"
  scale:
    adult: 1.0
    baby: 0.5
  sounds:
    ambient: "alexsmobs:entity.grizzly_bear.idle"
    hurt: "alexsmobs:resource:grizzly_bear.hurt"
    death: "alexsmobs:entity.grizzly_bear.death"
```

---

## 5. Optimisation Folia — Patterns obligatoires

### ❌ INTERDIT — Patterns qui cassent Folia

```yaml
# Pas de boucle sur toutes les entités du monde
# ❌ getNearbyEntities avec radius énorme
my_bad_skill:
  Skills:
    - potion{type=SPEED;duration=200} @ENOBy{r=200}
    #                        ^^^ scan global = lag sous Folia

# ❌ Scheduler synchrone global
# MythicMobs ne doit pas déclencher de tàches BukkitScheduler
```

### ✅ OBLIGATOIRE — Patterns Folia-safe

```yaml
# Event-driven : le skill se déclenche sur un event, pas sur un timer global
grizzly_bear_on_hurt:
  # Trigger = onAttack by autre entité → region-local, safe
  Skills:
    - skill:grizzly_bear_angry{} @self
      Conditions:
        - ¢var{var=grizzly_bear.honeyed;value=false}

# Cache local : la variable est stockée sur l'entité, pas dans une map globale
grizzly_bear_angry:
  Skills:
    - setstance{stance=angry}
    - setvariable{var=grizzly_bear.angry;value=true;type=FLOAT}
    - effect:particle{...}
```

### Timers via MythicMobs (Folia-safe)

MythicMobs gère ses propres timers **par entité**, ce qui est safe Folia :

```yaml
grizzly_bear:
  # Dans mob.yml — le timer est interne à l'entité
  Options:
    # ticks entre chaque exécution du timer
    # MythicMobs schedule via region scheduler de Folia
    onTimer: 20            # toutes les secondes
    onTimerSkill: grizzly_bear_timer_tick
```

### Threat Table — pas de recherche de cible globale

```yaml
# ✅ La Threat Table gère les cibles localement
grizzly_bear:
  Modules:
    ThreatTable: true
  AITargetSelectors:
    - clear
    - attackers          # Ceux qui l'ont attaqué (via Threat Table)
    - monofocused        # Focus une seule cible
```

---

## 6. Documentation des créatures

Chaque créature DOIT avoir un en-tête commenté dans son `mob.yml` :

```yaml
# ════════════════════════════════════════════════════════════════
# GRIZZLY BEAR (Alex's Mobs)
# Source: EntityGrizzlyBear.java (822 lignes, branch 1.20)
# Classification: C2 (AI modérée), P0 (signature mob)
# ─────────────────────────────────────────────────────────────────
# Comportements reproduits:
#   - [x] Tamable (salmon + honey sequence)
#   - [x] Neutral mob (anger timer system)
#   - [x] 4 animations d'attaque: Maul, SwipeL, SwipeR, Sniff
#   - [ stances: idle, standing, sitting, eating, honeyed, snowy, freddy
#   - [x] Snow variant dynamique (neige → texture snowy)
#   - [x] Bee hive raiding behavior
#   - [x] Mountable (tamed + adult + sneak=false)
#   - [x] Fur drop every 24k ticks (tamed adult)
#   - [x] Immune: sting, in_wall
#   - [x] Command system (cycle 0/1/2)
#   - [x] April Fools freddy mode
# ─────────────────────────────────────────────────────────────────
# Équivalences mod → plugin:
#   AnimationHandler (Citadel) → BetterModel animation states
#   SynchedEntityData → MythicMobs stance/variable
#   registerGoals() → AIGoalSelectors + AITargetSelectors
#   NeutralMob anger → ThreatTable + threat decay
# ─────────────────════════════════════════════════════════════════
```

---

## 7. Variantes et équivalences

| Alex's Mobs (Forge) | Folia Stack |
|---------------------|-------------|
| `SynchedEntityData` (booleans) | MythicMobs `stance` ou `variable` |
| `Animation` (Citadel) | BetterModel animation state |
| `registerGoals()` | `AIGoalSelectors` + `AITargetSelectors` |
| `getTarget()` / `setTarget()` | ThreatTable |
| `NeutralMob` anger | MythicMobs threat + threat decay |
| `addAdditionalSaveData` | MythicMobs `Persistent: true` + variables |
| `mobInteract` (right-click) | MythicMobs `onInteract` trigger |
| `isFood()` | MythicMobs condition `mixinmaterial` |
| `spawnAtLocation` | MythicMobs Drop tables |
| `checkSpawnRules` | MythicMobs Spawner conditions |
| `AMConfig.spawnRolls` | MythicMobs spawner `Chance` |
| Biome dictionary | MythicMobs `biome` condition |
| `hurt()` override | `DamageModifiers` + onDamaged |
| `doHurtTarget()` | onAttack metas + damage |
| `tick()` logic | `onTimer` skills (Folia-safe) |
| `playSound()` custom | MythicMobs `sound` mechanic |
| Item tag predicates | MythicMobs `isftitem` / material conditions |
| `EntityType.create()` | MythicMobs spawner / `/mm m spawn` |
| Forge `@OnlyIn(Dist.CLIENT)` | N/A — tout est server-side |
| `getPassengers()` / mounting | MythicMobs mount mechanics |
| `getOwner()` / taming | MythicMobs `owner` variable + PAPI |

---

## 8. Git / Workflow

- Chaque créature = 1 commit au minimum
- Format commit: `feat(creature_name): description`
- Les corrections: `fix(creature_name): description`
- Refactor global: `refactor(core): description`
- Chaque PR/commit ne doit pas casser les créatures existantes
```
