# Patterns MythicMobs réutilisables

Document de référence cataloguant tous les patterns MythicMobs 5.x utilisés dans ce projet. Chaque pattern est accompagné d'un exemple concret et du mappage vers le code Alex's Mobs original.

---

## 1. Conditions inline (syntaxe 5.x)

MythicMobs 5.x utilise les conditions inline avec `?` (vrai) et `!` (faux). **Ne pas utiliser** l'ancienne syntaxe `Conditions:` en sub-block.

```yaml
skill_name:
  Skills:
    - mechanic{params} @target
      ?condition{params}     # La condition DOIT être vraie
      !condition{params}     # La condition DOIT être fausse
```

### Conditions couramment utilisées

| Condition | Usage | Exemple |
|-----------|-------|---------|
| `stance{s=name}` | Vérifier la stance actuelle | `?stance{s=idle}` |
| `variable{var=X;value=Y}` | Variable égale à valeur | `?variable{var=mob.tamed;value=true}` |
| `hastarget{}` | A une cible | `?hastarget{}` |
| `isadult{}` / `baby{}` | Âge de l'entité | `?baby{}` |
| `health{h=>50}` | HP comparaison | `?health{h=<50}` |
| `hasitem{m=ITEM;slot=HAND}` | Item en main | `?hasitem{m=SALMON;slot=HAND}` |
| `playersneaking{}` | Joueur sneak | `?playersneaking{}` |
| `biome{b=TAIGA}` | Biome actuel | `?biome{b=FOREST_BIOMES}` |
| `israining{}` | Météo | `?israining{}` |
| `isonfire{}` | En feu | `?isonfire{}` |
| `isinwater{}` | Dans l'eau | `?isinwater{}` |
| `random{chance=0.3}` | Probabilité | `?random{chance=0.3}` |
| `distance{d=<5}` | Distance à la cible | `?distance{d=<5}` |
| `playerwithin{d=16}` | Joueur à proximité | `?playerwithin{d=16}` |
| `isplayerertype{type=PLAYER}` | Type du trigger | `?isplayerertype{type=PLAYER}` |

---

## 2. Stances ↔ BetterModel states

Les stances MythicMobs pilotent les animations BetterModel.

```yaml
# MythicMobs skill change la stance → BetterModel joue l'animation
- setstance{stance=mauling}
- setmodelstate{state=maul}     # pilote BetterModel directement
```

### Convention de nommage

| MythicMobs stance | BetterModel state | Type |
|-------------------|-------------------|------|
| `idle` | `idle` | Boucle (locomotion) |
| `walking` | `walk` | Boucle (locomotion) |
| `running` | `run` | Boucle (locomotion) |
| `swimming` | `swim` | Boucle (locomotion) |
| `standing` | `stand` | Boucle (posture) |
| `sitting` | `sit` | Boucle (posture) |
| `eating` | `eat` | Boucle (action) |
| `mauling` | `maul` | One-shot (attaque) |
| `sniffing` | `sniff` | One-shot (idle) |
| `dead` | `death` | One-shot (mort) |

---

## 3. Variables persistantes (state management)

```yaml
# Définir une variable
- setvariable{var=mob.tamed;value=true;type=BOOLEAN}

# Incrémenter une variable (math)
- setvariable{var=mob.timer;value=<math.(mob.timer + 1)>;type=INTEGER}

# Conditions sur variables
?variable{var=mob.tamed;value=true}
?!variable{var=mob.tamed;value=true}   # Pas tamed
```

### Types de variables

| Type | Usage | Exemple |
|------|-------|---------|
| `BOOLEAN` | Flags (tamed, sitting, etc.) | `true` / `false` |
| `INTEGER` | Compteurs, timers | `0`, `1`, `24000` |
| `FLOAT` | Progressions (lerp) | `0.0`, `5.5` |
| `STRING` | UUIDs, noms | `"NONE"`, `"<trigger.uuid>"` |

### Persistant vs runtime

```yaml
# variables.yml
mob.tamed:
  type: BOOLEAN
  persistent: true       # Survit au restart (NBT)
  desc: "L'entité est apprivoisée"

mob.animTick:
  type: INTEGER
  persistent: false      # Runtime only
  desc: "Tick d'animation courant"
```

---

## 4. Lerp d'animation (progression douce)

Le mod original fait un lerp manuel (`standProgress++`). En MythicMobs, on reproduit ce pattern :

```yaml
mob_standing_lerp:
  Skills:
    # Si standing → incrémenter jusqu'à max (5 ou 10)
    - setvariable{var=mob.standProgress;value=<math.(mob.standProgress + 1)>;type=FLOAT}
      ?variable{var=mob.standing;value=true}
      ?variable{var=mob.standProgress;value=<10}
    # Si pas standing → décrémenter jusqu'à 0
    - setvariable{var=mob.standProgress;value=<math.(mob.standProgress - 1)>;type=FLOAT}
      ?variable{var=mob.standing;value=false}
      ?variable{var=mob.standProgress;value=>0}
```

BetterModel peut interpoler automatiquement entre les states, donc le lerp est souvent optionnel — mais il est utile pour les hitboxes dynamiques.

---

## 5. Timers Folia-safe

MythicMobs gère ses timers via le RegionScheduler de Folia (entity-local, pas global).

```yaml
# mob.yml — onTimer exécute un skill toutes les N ticks
grizzly_bear:
  Skills:
    - onTimer:10              # Toutes les 10 ticks (0.5s)
    - skill:grizzly_bear_tick @self
```

### Pattern: timer de crier avec variable

```yaml
# Décrémenter un timer, agir quand il atteint 0
mob_check_snow:
  Skills:
    - setvariable{var=mob.snowTimer;value=<math.(mob.snowTimer - 1)>;type=INTEGER}
    - skill:mob_do_snow_check @self
      ?variable{var=mob.snowTimer;value=0}
```

---

## 6. Threat Table (targeting region-local)

```yaml
# mob.yml
mob:
  Modules:
    ThreatTable: true
  AITargetSelectors:
    - clear
    - attackers          # Ceux qui l'ont attaqué
    - monofocused        # Focus une seule cible
```

La Threat Table est stockée dans l'objet entité, pas dans une map globale. Parfaitement Folia-safe.

---

## 7. Triggers (event-driven)

Les triggers déclenchent les skills sur des événements, pas sur des timers globaux.

```yaml
# mob.yml
mob:
  Skills:
    - onSpawn:
      - skill:mob_init @self
    - onDeath:
      - skill:mob_death @self
    - onDamaged:
      - skill:mob_on_hurt @self
    - onAttack:
      - skill:mob_on_attack @target
    - onInteract:
      - skill:mob_on_interact @self
    - onTimer:20:
      - skill:mob_tick @self
```

---

## 8. DamageModifiers (immunités/résistances)

```yaml
# mob.yml
mob:
  DamageModifiers:
    - STING 0.0           # Immunisé (0% damage)
    - IN_WALL 0.0         # Immunisé suffocation
    - FALL 0.5            # 50% damage fall
    - FIRE 2.0            # Double damage fire
```

---

## 9. Variantes (textures dynamiques)

```yaml
# model/model.yml
variants:
  snowy:
    texture: snowy
    condition:
      mythicmobs_variable: "mob.snowy"
      value: true
```

Le swap de texture est instantané, piloté par la variable MythicMobs.

---

## 10. Spawners (spawn naturel)

```yaml
# spawn.yml
mob_spawner:
  Type: mob_name
  SpawnRate: 4
  MaxMobs: 3
  Conditions:
    - biome{b=TAIGA,FOREST}
    - height{h=60to150}
    - lightlevel{l=0to7}
    - worldtype{wt=OVERWORLD}
    - playerdistance{d=>24}
```

Les spawners MythicMobs tournent en region-local. Pas de scan monde entier.

---

## 11. AI Goals (sélecteurs natifs)

```yaml
# ai.yml
mob_ai:
  Goals:
    - float{p=0}                    # Priorité 0
    - melee{p=1;speed=1.25}         # Attaque melee
    - panic{p=2;speed=2.0}          # Panique
    - followparent{p=3;speed=1.25}  # Suit le parent
    - breed{p=4;speed=1.0}          # Reproduction
    - randomstroll{p=5;speed=0.75}  # Marche aléatoire
    - lookatplayer{p=6;range=6}     # Regarde le joueur
    - randomlookaround{p=6}         # Regard aléatoire
```

### Conditions sur AI Goals

```yaml
    - melee{p=1;speed=1.25}
      ?!stance{s=sitting}            # Pas pendant sitting
      ?!baby{}                       # Pas les babies
```

---

## 12. Drops (loot tables)

```yaml
# drops.yml
mob_drops:
  Conditions:
    - playerkill{}
  Drops:
    - alexsmobs:bear_fur 1-2 0.3    # 1-2 items, 30% chance
    - SALMON 0-1 0.1                # 0-1 vanilla item, 10%
    - exp{amount=1-3} 1.0           # Experience
```

---

## 13. Équivalences Forge → MythicMobs (référence rapide)

| Code Forge | MythicMobs équivalent |
|------------|----------------------|
| `SynchedEntityData.define()` | `setvariable{}` + `variable{}` condition |
| `Animation` (Citadel) | `setstance{}` → BetterModel state |
| `registerGoals()` | `AIGoalSelectors` + `AITargetSelectors` |
| `getTarget()` / `setTarget()` | ThreatTable native |
| `NeutralMob` anger | ThreatTable + threat decay |
| `addAdditionalSaveData()` | `Persistent: true` + variables persistent |
| `mobInteract()` | `onInteract` trigger |
| `isFood()` | `hasitem{}` condition |
| `spawnAtLocation()` | `dropitem{}` mechanic |
| `checkSpawnRules()` | Spawner conditions |
| `hurt()` override | `DamageModifiers` + `onDamaged` |
| `doHurtTarget()` | `onAttack` trigger + `damage{}` |
| `tick()` logic | `onTimer` skills |
| `playSound()` custom | `sound{}` mechanic |
| `EntityType.create()` | Spawner / `/mm m spawn` |
| `getPassengers()` | `mount{}` mechanic |
| `getOwner()` / taming | Variables `mob.tamed` + `mob.owner` |

---

## 14. Pattern: Créature complète (checklist)

Pour chaque nouvelle créature, suivre cet ordre :

1. **Analyser le code source** (`Entity<Name>.java`)
2. **Lister les behaviors** (AI goals, interactions, animations)
3. **Identifier les SynchedEntityData** → variables MythicMobs
4. **Identifier les Animations Citadel** → BetterModel states
5. **Créer mob.yml** (définition + modules + triggers)
6. **Créer variables.yml** (toutes les variables persistantes)
7. **Créer skills.yml** (behaviors en skills atomiques)
8. **Créer ai.yml** (Goals + TargetSelectors)
9. **Créer drops.yml** (loot table)
10. **Créer spawn.yml** (biome conditions)
11. **Créer sounds.yml** (sound registry)
12. **Créer model/model.yml** (BetterModel mapping)
13. **Valider YAML** (lint)
14. **Test in-game** (`/mm m spawn <name>`)
