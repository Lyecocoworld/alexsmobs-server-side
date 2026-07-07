# Larion + Horizon — Guide d'installation

## Le problème

Larion utilise **6 types de density functions customs** (`larion:div`, `larion:sine`, `larion:signum`, `larion:flat_domain_warp`, `larion:x`, `larion:z`). Ces types ne sont pas connus de Minecraft vanilla et doivent être **enregistrés dans le registry avant le freeze**.

Sur un serveur Paper/Folia vanilla, le registry est gelé avant le chargement des plugins → **Larion ne fonctionne pas**.

## La solution : Horizon

**Horizon** (par CraftCanvasMC) est un **Mixin loader pour Paper/Folia/Canvas**. Il permet aux plugins d'utiliser SpongePowered Mixins pour modifier le code NMS avant le démarrage du serveur.

- **Repo** : https://github.com/CraftCanvasMC/Horizon
- **Docs** : https://docs.canvasmc.io
- **Téléchargement** : https://canvasmc.io/downloads

### Installation d'Horizon

1. Télécharger le JAR Horizon depuis https://canvasmc.io/downloads
2. Placer le JAR Horizon dans le même dossier que `server.jar`
3. Lancer Horizon au lieu du server.jar directement :
   ```bash
   java -jar horizon.jar nogui
   ```
4. Horizon va wrapper le server.jar et injecter les Mixins au démarrage

### Structure du plugin avec Horizon

```
AlexMobsServerSide.jar
├── com/lyecocoworld/alexsmobsserverside/
│   ├── AlexMobsServerSide.class          (plugin principal)
│   ├── ConfigExtractor.class             (extraction configs)
│   ├── DependencyChecker.class           (vérif plugins)
│   └── larion/                           (Larion density functions)
│       ├── LarionDensityFunctions.class  (registration)
│       ├── Division.class                (larion:div)
│       ├── Sine.class                    (larion:sine)
│       ├── Signum.class                  (larion:signum)
│       ├── FlatDomainWarp.class          (larion:flat_domain_warp)
│       └── CoordFunctions.class          (larion:x, larion:z)
├── com/lyecocoworld/alexsmobsserverside/mixin/
│   └── LarionDensityFunctionMixin.class  (Mixin: injecte dans Bootstrap.bootStrap())
├── horizon.plugin.json                   (déclare le plugin Horizon)
├── mixins.larion.json                    (déclare les Mixins Larion)
├── plugin.yml                            (fallback Paper vanilla)
├── alexsmobs/                            (89 créatures MythicMobs)
├── craftengine/                          (301 items + 137 blocks + NS)
├── worldgen_datapack/                    (datapacks worldgen)
└── assets/                               (textures, sons, modèles)
```

### Compilation avec Horizon Gradle Plugin

Pour compiler les classes Larion (qui utilisent NMS), il faut le plugin Gradle Horizon :

```kotlin
// build.gradle.kts
plugins {
    id("io.canvasmc.weaver.userdev")
    id("io.canvasmc.horizon")
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

horizon {
    splitPluginSourceSets()
    accessTransformerFiles.from(file("src/main/resources/widener.at"))
}
```

### Mixin Larion

```java
@Mixin(targets = "net.minecraft.server.Bootstrap")
public class LarionDensityFunctionMixin {
    @Inject(method = "bootStrap", at = @At("TAIL"))
    private static void registerLarionDensityFunctions(CallbackInfo ci) {
        LarionDensityFunctions.registerAll();
    }
}
```

## Alternative sans Horizon : Terralith

Si tu ne veux/pas peux utiliser Horizon, remplace Larion par **Terralith** :
- 100% datapack vanilla (aucun type custom)
- Aucun Mixin requis
- Fonctionne sur Paper/Folia vanilla
- Download : https://modrinth.com/mod/terralith

## Hauteur du monde étendue

Le datapack Larion configure :
- `min_y`: -128 (au lieu de -64)
- `height`: 640 (au lieu de 384)
- **Y max**: 512 (au lieu de 320)

Les montagnes Larion peuvent s'étendre jusqu'à Y=512.
