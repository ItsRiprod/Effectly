# Changelog

## [1.3.0] - 2026-08-15

### Breaking

- **Migrated ownership of files to the Riprod namespace.** `hexvane:AbilityAPI` and the
  `com.hexvane.abilityapi` API classes are kept as a compatibility layer, so existing integrations do
  not need rebuilding. Remove any older AbilityAPI jar before installing, and note that command
  permission nodes are now `riprod.effectly.command.ability.*`.

- **`player_abilities.json` is gone.** Player abilities are now a persistent ECS component on the
  player entity (`Effectly:Roster`), written into the engine's own player document at
  `run/universe/players/<uuid>.json`. Existing grants are **not** migrated and must be re-issued.
  In exchange, ability data is universe-global, survives world transfers, and is preserved if
  Effectly is uninstalled and later reinstalled.
- **`mining_fortune_blocks.json` is no longer read.** The block list moved to `MiningFortuneBlocks`
  in `Server/Configs/Effectly.json`. A warning is logged at startup if the old file is still present.
- **`AbilityService.setAbility` / `removeAbility` are applied on the next world tick**, not
  synchronously. Reading state back within the same tick will still see the old value.
- **`second_chance` cooldown no longer counts down while the player is offline.** It is now a
  persisted delta-time counter, so it survives relogs and restarts but pauses while away.
- `AbilityConditionSpec` changed from a `record` to a `final class` so it could carry a codec. Its
  public surface - both constructors, `type()`, `param()`, `zoneIds()`, `allowedZoneIds()` and all six
  `TYPE_*` constants - is unchanged and source-compatible.

### Added

- **Resistances now cover derived damage causes.** `DamageCause` is a tree, so `resistance_elemental`
  reduces `Fire` and `resistance_physical` reduces `Slashing`. The most specific active resistance
  wins rather than stacking; a value of `0` counts as active and cancels a broader one.

- **Configly configuration** at `Server/Configs/Effectly.json` - 19 documented, hot-reloadable
  settings that were previously hardcoded constants: the mining-fortune block list, mining haste per
  level, health-regen delay, stamina base rate, oxygen units, second-chance restore/cooldown,
  item-magnet range/speed/threshold, wall-climb probe and velocities, dark-vision effect and check
  interval, flight re-assert interval, and the two sunlight thresholds. Patchly can target it.
- **Declarative abilities** at `Server/Effectly/Abilities/*.json` - one file per ability defining
  its type, default, min, max, description and handler. Server owners can retune ranges or set
  `"Enabled": false` without a code change. Other mods can ship their own ability JSONs.
- **Ability handler registry** - `AbilityHandler` plus `AbilityHandlerRegistry.register(...)`, the
  single extension point for adding an ability. One handler may serve a whole family of ability ids
  via the asset's `Handler` field, which is how all 15 `resistance_*` abilities share one handler.
- **Offline grants and removals are written directly to the player's saved document** via
  `PlayerStorage.update`, so they survive a restart and are live on the player's next join. Previously
  a grant to an offline player was silently dropped.
- `AbilityService.setAbility(uuid, id, value, conditions)` sets both in one call, so an ability is
  never briefly live without its conditions.

### Fixed

- **Abilities were silently broken for all but one player in multiplayer.** `CreativeFlightSystem`,
  `AbilityInitSystem` and `MovementAbilitiesReapplySystem` each incremented a single shared counter
  once *per entity per tick*, so with N players online the interval elapsed N times too fast and only
  whichever entity tripped the boundary was serviced. `DarkVisionSystem` had the same bug via a
  `static` counter shared across every world on the server.
- **`creative_flight` stomped other mods' movement settings.** Its "no ability" branch called
  `refreshDefaultSettings()` + `applyDefaultSettings()`, replacing all 66 fields of `MovementSettings`
  every 20 ticks - and fighting Effectly's own movement-speed writes. It now touches only `canFly`,
  and its periodic check is constructive: it re-enables flight but never disables it, so a second mod
  granting flight is never overridden.
- **Per-damage-type resistances never matched the game's real damage types.** They were enumerated
  from `DamageCause.getAssetMap()` during `setup()`, which runs before assets load, so the hardcoded
  fallback list was always what registered. Resistances are now declared assets loaded after
  `DamageCause`.
- **`"Enabled": false` on an ability had no effect** - the field was documented but never read.
- Removed a `try/catch (Throwable)` around `getQuery()` in the strength and punch-damage systems that
  was masking the plugin query-registration hazard.
- Ability reads no longer resolve a player by UUID on the damage path; condition evaluation reads
  directly from the entity it was already given.
- Public ability reads are now safe to call off the world thread.

### Removed

- `PlayerAbilityStorage`, `HealthRegenDelayStore`, `SecondChanceCooldownStore` - all replaced by
  components. The two stores were static UUID-keyed maps that never evicted and were shared across
  worlds; the second-chance cooldown additionally used wall-clock time that ignored pause.
- `AbilityInitSystem` (poll-based login detection, replaced by a `RefSystem` hook),
  `MovementAbilitiesReapplySystem` (replaced by a swim-state-change check), and
  `ConditionZoneCheckSystem` (its only output was a log line; use `/log Effectly FINE`).

### Performance

- Ticking systems now query the specific component for their ability instead of every player. An
  ability nobody has costs nothing; event-driven abilities (resistances, mining, breathing) no longer
  tick at all. Previously all ten ticking systems visited every player every tick just to discover
  they had nothing to do.
- `item_magnet` was inverted: it now queries players holding the magnet and spatial-queries nearby
  items, rather than querying every item entity in the world and spatial-querying players for each.

## [1.2.2] - 2026-06-15

### Added

- **Help Info** All commands now have better info under --help.

### Fixed

- **Modify Abilities of Others** Fixed issue where the player can't modify the abilities of other players.

## [1.2.1] - 2026-04-1

### Fixed

- **Damage ability ordering** - `FallDamageImmunitySystem`, `InvulnerabilitySystem`, `SecondChanceSystem`, and `HealthRegenDelayRecordSystem` now depend on running **after** `DamageCalculatorSystems.SequenceModifier` and **before** `DamageSystems.ApplyDamage`. Previously they only ran after the filter group with no edge to `ApplyDamage`, so the scheduler could run them **after** health was already subtracted, making immunity and fall protection unreliable.

## [1.2.0] - 2026-03-31

### Added

- **`invulnerability` (binary)** - `InvulnerabilitySystem` sets incoming entity damage to zero when the ability is active (registered after `FallDamageImmunitySystem`, before `SecondChanceSystem`).
- **Mining fortune** - `Ore_Thorium_Mud` added to the fortune block config (`mining_fortune_blocks.json`).

### Changed

- **Hytale 0.5.0** - Targets Hytale server `^0.5.0` (semver `ServerVersion` in manifest; no longer uses the legacy `YYYY.MM.DD-<sha>` pin).
- **Waterbreathing** - Uses `BreathingCheckEvent` so players with the ability can breathe in fluids under the 0.5 `BreathingComponent` / suffocation pipeline (oxygen-stat top-up alone is insufficient).
- **Math types** - `Vector3d` / block positions use `org.joml` types; rotations use `Rotation3fc` / `Rotation3f` where the server API changed.
- **Punch damage** - Uses `InventoryComponent.getItemInHand` instead of the deprecated `Inventory.getItemInHand()`.

## [1.1.0] - 2026-03-26

### Changed

- **Hytale API compatibility** - Updated for the latest Hytale server release.
- **Stat modifiers** - `AbilityStatService` now triggers stat recalculation via `EntityStatMap.getStatModifiersManager().scheduleRecalculate()` instead of the removed `Player.getStatModifiersManager().setRecalculate(...)` API.
- **Imports** - Removed unused `LivingEntityInventoryChangeEvent` import from `EffectlyPlugin` (class no longer exists in the current API). Inventory-related events now use `com.hypixel.hytale.server.core.inventory.InventoryChangeEvent` if you extend the mod with inventory listeners.
