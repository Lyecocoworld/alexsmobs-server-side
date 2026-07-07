# Optimisation Folia — Patterns pour ce projet

## Contexte

Folia supprime le main thread unique de Paper/Spigot. Chaque région du monde tourne sur son propre thread. Les plugins qui assument un main thread unique crashent ou corrompent les données.

MythicMobs 5.x est compatible Folia — ses schedulers passent par le RegionScheduler/EntityScheduler de Folia automatiquement. Mais **les patterns que nous écrivons dans les YAML** peuvent toujours causer des problèmes de performance.

---

## Patterns à respecter

### 1. Event-driven, pas timer-driven

**❌ INTERDIT** — Timer global qui scanne le monde :

```yaml
# Ce skill tourne toutes les 20 ticks sur TOUS les grizzly_bear
# et fait un getNearbyEntities à chaque fois
grizzly_bear_bad_timer:
  Skills:
    - potion{type=SPEED;duration=200} @ENOBy{r=64}
```

**✅ CORRECT** — Le skill ne se déclenche que quand un événement arrive :

```yaml
grizzly_bear:
  # onInteract: trigger quand un joueur fait un clic droit
  # Region-local, pas de scan
  Skills:
    - skill:grizzly_bear_interact{} @self
```

### 2. Threat Table native

MythicMobs gère la Threat Table **par entité**. Pas besoin de scanner pour trouver les ennemis :

```yaml
grizzly_bear:
  Modules:
    ThreatTable: true
  AITargetSelectors:
    - clear
    - attackers
```

La Threat Table est stockée dans l'objet entité, donc region-local. Parfaitement Folia-safe.

### 3. Variables locales à l'entité

MythicMobs variables (parsées) sont stockées par entité via le `Persistent: true` NBT. Pas de map globale, pas de contention :

```yaml
grizzly_bear_tick_snow:
  Skills:
    - setvariable{var=grizzly_bear.snowy;value=true;type=BOOLEAN}
```

### 4. Timers MythicMobs

`onTimer: 20` sur une entité MythicMobs est géré par l'EntityScheduler de Folia. Le timer tourne **sur le thread de la région** de l'entité. Pas de main thread.

```yaml
grizzly_bear:
  # onTimer: 20 → toutes les 20 ticks, region-local
  Skills:
    - onTimer: 20
    - skill:grizzly_bear_timer_tick{} @self
```

### 5. Pathfinding maîtrisé

Le pathfinding vanilla (via AIGoals) est géré par Folia en region-local. Cependant :

- **Ne pas utiliser** `@ENOBy{r=1000}` pour le targeting — la recherche se fait via AITargetSelectors
- **Ne pas滥用 `randomStroll`** sur des mobs qui ont FollowRange énorme — ils vont pathfind trop loin
- Préférer `@EIR` (entities in radius, region-local) pour les interactions proches

### 6. Spawners MythicMobs — Folia-safe

Les spawners MythicMobs tournent en region-local. Ils ne scannent pas le monde entier :

```yaml
grizzly_bear_spawner:
  Type: grizzly_bear
  SpawnRate: 4
  MaxMobs: 3
  CheckTime: true
  Conditions:
    - biome{b=TERRA_FAMILY,PLAINS,TALL_FOREST}
    - height{h=60to150}
    - lightlevel{l=0to7}
```

### 7. Éviter les boucles coûteuses

```yaml
# ❌ Si on a 1000 entités, ça fait 1000 * N scans par tick
my_skill:
  Skills:
    - loop{iterations=100;skill=my_inner_skill} @ENOBy{r=64}

# ✅ Utiliser des conditions MythicMobs qui short-circuit
my_skill:
  Skills:
    - skill:my_inner_skill @ENOBy{r=8}
      Conditions:
        - playerwithin{d=16}     # Short-circuit si pas de joueur à 16 blocs
```

### 8. Region-aware targeting

```yaml
# ✅ Targeting local à l'entité
grizzly_bear_attack_nearest:
  Skills:
    - skill:grizzly_bear_maul @ENOBy{r=4}
      Conditions:
        - stance{s=mauling}
```

### 9. Synchronisation BetterModel

BetterModel joue les animations via packets. Ces packets sont envoyés depuis le thread de la région de l'entité. Le `setstance` de MythicMobs déclenche l'animation BetterModel immocédiatement, dans le bon thread :

```yaml
grizzly_bear_start_maul:
  Skills:
    - setstance{stance=mauling}   # → BetterModel joue animation maul
    - setvariable{var=grizzly_bear.anim_tick;value=0;type=INTEGER}
```

### 10. Cache des conditions coûteuses

Certaines conditions MythicMobs sont coûteuses (biome, structure, etc.). Les cacher via une variable :

```yaml
# Au lieu de vérifier le biome à chaque tick
# Vérifier une fois puis cacher
grizzly_bear_biome_cache:
  Skills:
    - setvariable{var=grizzly_bear.biome_ok;value=true;type=BOOLEAN}
      Conditions:
        - biome{b=TAIGA}

grizzly_bear_timer_tick:
  Skills:
    - skill:grizzly_bear_do_something
      Conditions:
        - variable{var=grizzly_bear.biome_ok;value=true}
```

---

## Checklist Folia pour chaque créature

Avant de marquer une créature comme terminée, vérifier :

- [ ] Pas de `@ENOBy{r=64+}` dans des timers fréquents
- [ ] Tous les timers utilisent `onTimer:` MythicMobs (pas BukkitScheduler)
- [ ] Les variables sont stockées sur l'entité (pas de map globale)
- [ ] Le targeting utilise ThreatTable ou AITargetSelectors
- [ ] Les spawners ont `MaxMobs` et des conditions restrictives
- [ ] Les skills coûteux sont gated par des conditions cheap d'abord
- [ ] Pas de `getNearbyEntities` équivalent avec grand radius
- Folia multithreading: entité = région = thread
- [ ] Pas de communication inter-régions synchrone
```
