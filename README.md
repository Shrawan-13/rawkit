# Rawkit

**Rawkit** is an Android launcher built around two ideas:

1. **Smallest, fastest launcher possible.** The core APK ships almost nothing it does
   not need. No Compose in the launcher process, no unused framework, a cold start
   budget measured in CI.
2. **Everything about appearance is data.** Colour, type, shape, spacing, motion,
   wallpaper, icon packs and layout presets live in a versioned, portable pack format
   that anyone can author, sign and share. The launcher itself stays tiny because
   the customisation lives outside it.

## Principles

| Principle | Consequence |
|---|---|
| The launcher must be excellent with zero packs installed | The store is a bonus, never a dependency |
| Behaviour is code, appearance is data | Features ship once; looks ship as ~100 KB packs |
| Never execute untrusted code in the launcher process | Plugins render via `RemoteViews` in their own UID |
| Version public formats from day one | `apiVersion` + migrations; shipped packs never break |
| Measure, don't guess | Cold start, APK size and RSS are CI gates, not aspirations |
| Open source | Pack format and plugin API are public and documented |

## Status

Work is done in slices; each one ends in something runnable on a device.

| # | Slice | State |
|---|---|---|
| 1 | Toolchain + skeleton, installs and registers as HOME | done |
| 2 | App list, drawer, search | done |
| 3 | Theme token engine + built-in themes | planned |
| 4 | Home layout model, pages, dock, folders | planned |
| 5 | App widget hosting | planned |
| 6 | Pack format v1 + local install | planned |
| 7 | Store: remote catalog, download, apply | planned |
| 8 | Rawkit Studio: theme editor, pack authoring | planned |
| 9 | Plugin runtime: sideloaded widget plugins | planned |

Current release APK: **~152 KB** (R8 + resource shrinking, no Compose).

## CI

`ci.yml` runs three jobs:

- **Build and test** — assembles debug *and* release (so R8/shrinker breakage is
  caught on every push), runs unit tests, uploads both APKs.
- **Lint** — runs in parallel with the build instead of extending it, reads the
  Gradle cache read-only so the two jobs do not race to reserve the same cache
  key.
- **Prune caches** — runs last, after both other jobs, and deletes every Actions
  cache except the **20 most recently used** ones. Cache entries are cheap to
  rebuild and expensive to accumulate; keeping the set small keeps cache writes
  fast.

Build times are dominated by the first run after a dependency change. With the
Gradle home cache warm, an unchanged build takes ~20-30s and most tasks are
restored from the local build cache rather than re-executed.

> The `gradle-home-cache-includes` list must include the Gradle version
> directory (e.g. `8.10.2`), not just `caches`. That directory holds the
> execution history, and without it every task re-executes instead of reporting
> UP-TO-DATE.

## Building

Requires JDK 17 (Temurin or OpenJDK — **GraalVM's `jlink` cannot build AGP's JDK
image** and will fail the `androidJdkImage` transform).

```bash
export JAVA_HOME=/path/to/jdk-17
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug APK is a launcher: after installing, press Home and choose Rawkit.

### Module layout

```
rawkit/
├── app/        the launcher itself (thin)
├── core/       core-design, core-model, core-theme, core-data, core-pack, core-ui
├── studio/     Rawkit Studio - separate companion app for authoring packs
└── plugin-api/ public API for third-party widget plugins
```

`app` depends on `core/*`; `core/*` never depends on `app`; `studio` and
`plugin-api` depend on `core-model`/`core-design` only.

## Decisions log

Decisions that are expensive to reverse, and why they were made:

- **minSdk 26** — maximum reach for a launcher people install for speed. Adaptive
  icons, notification channels, `LauncherApps` all available.
- **Views + RecyclerView (`PagerSnapHelper`)** — `AppWidgetHostView` is a `View`, so
  Compose buys nothing on the home screen and costs ~2.5 MB plus cold-start time.
  Compose is allowed in the Studio app, which ships separately.
- **Versioned pack format from day one** — a store whose format breaks users is a
  store nobody trusts. `apiVersion` + migrations from the first `.rawpack`.
- **FOSS, GitHub Releases distribution** — no Play policy constraints on a launcher
  that ships plugins, natural sideloading path for plugin APKs.
- **Separate Studio app** — keeps the editor's dependency weight (Compose, Material)
  entirely out of the launcher APK.
- **Plugins render via `RemoteViews`** — the host binds to the plugin's service and
  inflates the returned `RemoteViews` itself. The launcher never loads plugin code,
  so a bad plugin cannot crash or compromise it.

## Licence

Apache-2.0.
