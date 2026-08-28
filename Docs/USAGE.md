# Effectly – User & Modder Guide

Effectly is a **library mod** that adds a shared set of player abilities (flight, waterbreathing, resistances, movement modifiers, etc.) and a clean way for **server admins** and **other mods** to grant and manage them.

This document explains:

- How to install and configure the mod as a **server admin**
- How to use the **commands** to grant and inspect abilities
- What the **built‑in abilities** do
- How other mods (like **Orbis Origins**) can **integrate** with Effectly

---

## 1. Installation & Requirements

- Drop the **Effectly** JAR into your Hytale server’s `mods` folder.
- Make sure you are using:
  - **Hytale server 0.5.0 or newer** (Effectly 1.2.0 declares `ServerVersion`: `^0.5.0`)
  - A Java version compatible with your Hytale tooling (Effectly targets Java 25; Gradle handles the toolchain).

Effectly stores nothing of its own on disk. Player abilities live on the player entity as an ECS
component, so they are written into the engine's own player document at
`run/universe/players/<uuid>.json` under `Effectly:Roster`. That means they are universe-global,
survive world transfers, and are preserved even if Effectly is uninstalled and later reinstalled.

Two things are authored rather than generated:

- `Server/Configs/Effectly.json` - server-wide tuning (Configly). Ships with sensible defaults.
- `Server/Effectly/Effects/*.json` - one file per ability, defining its type, range and handler.

No manual configuration is required to get started.

---

## 2. Core Concepts

### 2.1 Abilities and values

Each **ability** has:

- A unique **ID** (e.g. `creative_flight`, `waterbreathing`, `move_speed`)
- A **type**:
  - **Binary** (on/off): value is effectively `true`/`false`
  - **Numeric**: value is a number (double)
- Optional **min/max** and a **description** (for help text and validation)

Abilities are defined declaratively, one JSON file per ability, in `Server/Effectly/Effects/`.
The IDs are stable across servers so other mods can depend on the same IDs and behavior. Setting
`"Enabled": false` in an ability's file stops it being granted without removing the file.

Server owners can retune an ability's `Min`, `Max` and `Default` there - the old hardcoded ranges are
no longer baked into the jar.

### 2.2 Player ability storage

Every granted ability is one entry in a single persistent ECS component, `AbilityComponent`
(component id `Effectly:Roster`), attached to the player entity:

- `abilityId -> { value, conditions }`

That component is the only thing Effectly persists. The engine saves it with the rest of the player
document (autosave every 10s, plus on disconnect and world shutdown), so changes survive restarts
without Effectly writing any file itself.

Individual abilities may attach further *transient* components - those exist only while the ability
is granted and are never written to disk. Having the component is what makes an ability tick;
removing it is what stops it. `SecondChanceComponent` is the exception: it persists its cooldown so a
relog cannot reset it, and removes itself once the cooldown is spent and no ability backs it.

Abilities that write state the engine persists - a stat modifier, for example - must also implement
`reconcile`, which runs on login for every player holding nothing for that handler. Without it a
grant removed while the player was offline would leave the modifier applied forever.

If another mod grants or removes an ability for a player who is **offline**, the change is written
straight into that player's saved document, so it survives a restart and is live the moment they next
join. Offline writes are asynchronous - the call returns before the write completes.

### 2.3 Conditions

Some abilities are only active when **conditions** are met.

Conditions are declared as assets, one JSON file per condition, in `Server/Effectly/Conditions/`.
Each names a `Handler.Id` that implements it plus that handler's tuning, exactly like abilities do -
so several conditions may share one handler with different configuration. Setting `"Enabled": false`
stops a condition being evaluated without removing the file; a grant referencing a disabled or
missing condition never activates.

Ships with four condition handlers:

- `zone` - active in the configured world zone ids
- `sky_light` - active within a sunlight-factor and sky-light range
- `health` - compares health % against a threshold, for either the ability holder or the damage target
- `in_liquid` - active while in a fluid, optionally only while swimming

and nine conditions built on them:

- `in_zone` - active when the player is in one of the given zone IDs
- `in_sunlight` - active when it is daytime and there is open sky above the player
- `In_Liquid` - active while touching liquid
- `Swimming` - active while actually swimming
- `Moonlight` - active at night under open sky
- `health_below` / `health_above` - player health % below, or at or above, the threshold
- `target_health_below` / `target_health_above` - same, for the damage **target**

Conditions attach in two places. An **effect asset** may declare conditions intrinsic to itself,
which gate it for every source that grants it - `swim_speed` uses this to require `Swimming`:

```json
{
  "Handler": { "Id": "movement_state", "Field": "BaseSpeed" },
  "Conditions": [ { "Type": "Swimming" } ],
  "Type": "Numeric", "Min": 0.5, "Max": 3.0
}
```

A **grant** may carry its own on top, AND-ed with the asset's. Either way it is an
`AbilityConditionSpec` referencing a condition asset:

- `Type` (string): the condition asset id
- `Param` (optional int): overrides the condition's configured value - a health percentage (0-100),
  or the sole zone id. When omitted the asset's own configuration applies
- `ZoneIds` (optional list): overrides the zone ids for `in_zone`

Effectly evaluates these on demand via `AbilityConditionUtils` when applying stats or reacting to
events; nothing is cached between ticks.

---

## 3. Commands for Server Admins

All commands are registered under `/ability`. Exact permission integration depends on your server setup; typically only **ops/admins** should have access to these commands for other players.

### 3.1 Granting abilities

Grant a **binary** ability to yourself:

- `/ability add creative_flight`
- `/ability add waterbreathing`

Grant a **numeric** ability to yourself:

- `/ability add oxygen 10`  
→ +10 “seconds” of breath underwater (internally mapped to extra oxygen units)
- `/ability add move_speed 1.5`  
→ 1.5× movement speed
- `/ability add swim_speed 1.3`
- `/ability add strength 0.25`  
→ 25% more damage dealt

Grant an ability to another player:

- `/ability add <abilityId> <value?> <player>`
- Examples:
  - `/ability add creative_flight Steve`
  - `/ability add oxygen 20 Alice`

Values are validated against each ability’s min/max range; the command will reject out‑of‑range values.

### 3.2 Removing abilities

Remove a specific ability from yourself:

- `/ability remove creative_flight`
- `/ability remove strength`

Remove from another player:

- `/ability remove creative_flight Steve`

### 3.3 Inspecting abilities

List your current abilities:

- `/ability list`

List another player’s abilities:

- `/ability list Steve`

This shows each active ability, its value, and any configured conditions.

### 3.4 Listing available ability IDs

See all registered ability IDs and their descriptions:

- `/ability available`

Use this list as the authoritative source when configuring other mods or writing integration code.

---

## 4. Built‑In Abilities (Overview)

Below is a brief summary of the most important built‑in abilities. See `PLAN.md` for a full design document and internal planning details.

### 4.1 Movement & survival

- `**creative_flight` (binary)**  
  - Enables creative‑style flight for the player.
  - Implemented by the shared `movement_state` handler, which folds every movement ability into one
    desired state and writes it once. `creative_flight` contributes to `CanFly`; `move_speed` and
    `swim_speed` multiply `BaseSpeed`. There is no separate reassert loop - the system re-pushes only
    when what is applied differs from what is wanted, and reverts the same way.
- `**waterbreathing` (binary)**  
  - Player can breathe underwater (handled via `BreathingCheckEvent` on Hytale 0.5+).
- `**oxygen` (numeric)**  
  - Extra underwater breath. Each point adds more oxygen units to the player’s max oxygen stat (roughly “seconds” of breath, see code comments for exact scaling).
- `**fall_damage_immunity` (binary)**  
  - Completely negates fall damage.
- `**move_speed` (numeric multiplier)**  
  - Modifies base movement speed. `1.0` = normal; `>1` faster, `<1` slower.
- `**swim_speed` (numeric multiplier)**  
  - Multiplier applied when the player is swimming, gated by the `Swimming` condition declared on the
    asset rather than by handler code.
- `**wall_climb` (binary)**  
  - Allows climbing solid surfaces via the dedicated `WallClimbSystem`.

### 4.2 Combat & damage

- `**punch_damage` (numeric multiplier)**  
  - Multiplier for unarmed/melee damage.
- `**strength` (numeric multiplier)**  
  - Global damage dealt multiplier. Implemented by `AbilityStrengthSystem`:
    - Damage is multiplied by `(1 + value)`.
    - `value > 0` → extra damage; `value < 0` → reduced damage (weakness).
- `**resistance_<type>` (numeric, -1 to 1)**  
  - Per‑damage‑type resistance or weakness:
    - `0` = neutral
    - `<0` = weakness (take more damage)
    - `>0` = resistance (take less damage)
  - The cause is declared per ability under `Handler.DamageCause` and covers causes derived from it (`resistance_elemental` reduces `Fire`). The most specific active resistance wins, including a `0`, which cancels a broader one.
- `**invulnerability` (binary)**  
  - Negates all incoming entity damage (combat, environment, etc.). Does not affect block‑breaking speed (`DamageBlockEvent`).
- `**second_chance` (binary)**  
  - Prevents death once, restoring the player to low health with a cooldown. Used for “Undead” style species or special perks.

### 4.3 Utility & quality of life

- `**dark_vision` (binary)**  
  - Grants improved visibility in darkness via a client‑visible effect. (Not entirely happy with it, use at your own discretion)
- `**mining_haste` (numeric level)**  
  - Faster block breaking. Levels typically map to increasing speed (1–5).
- `**mining_fortune` (numeric level)**  
  - Extra drops from certain blocks (e.g. ores). Behavior is configured in `mining_fortune_blocks.json`; higher levels give more extra roll attempts.
- `**item_magnet` (numeric range multiplier)**  
  - Pulls dropped items in from further away. Higher values increase pickup radius.

---

## 5. Conditions in Practice

While commands can set raw abilities, **conditions** are most useful when another mod configures abilities for players.

Examples of how conditions are used (e.g. by Orbis Origins):

- **Zone‑based stamina regen**  
  - Ability: `stamina_regen` (numeric multiplier)  
  - Condition: `in_zone` with `zoneIds = [7, 8, 9, 10, 11]`  
  - Behavior: stamina regen multiplier only applies when the player is in those zones.
- **Photosynthesis health regen**  
  - Ability: `health_regen` (numeric per‑second value)  
  - Condition: `in_sunlight`  
  - Behavior: passive health regen only while standing in sunlight with open sky.
- **Low‑health berserker strength**  
  - Ability: `strength` (e.g. `0.2` = +20% damage)  
  - Condition: `health_below` with `param = 50`  
  - Behavior: bonus damage only when the player’s health is below 50%.
- **Predator’s instinct vs injured targets**  
  - Ability: `strength`  
  - Condition: `target_health_below` with `param = 50`  
  - Behavior: extra damage only when the target’s health is below 50%.

Conditions are created programmatically by other mods using `AbilityConditionSpec` (see §6).

---

## 6. Integrating Effectly from Other Mods

Effectly is designed to be **consumed by other mods** as a library. The recommended integration layer is the public `AbilityService` facade.

### 6.1 Dependency setup

In your consuming mod’s `build.gradle.kts`:

- If Effectly is a sibling project:

```kotlin
dependencies {
    implementation(project(":Effectly"))
}
```

- If you depend on a built JAR:

```kotlin
dependencies {
    implementation(files("./libs/Effectly-1.3.0.jar"))
}
```

Make sure you **gate all runtime usage** behind the Hytale `PluginManager` so your mod can still run when Effectly is missing:

```java
PluginIdentifier abilityApiId = PluginIdentifier.fromString("Riprod:Effectly");
PluginManager manager = PluginManager.get();
boolean abilityApiPresent = manager != null && manager.getPlugin(abilityApiId) != null;
```

Identifier casing matters - `PluginIdentifier` compares group and name exactly.

> **Existing consumers need no changes.** Effectly moved from Hexvane to Riprod in 1.3.0, but
> `hexvane:AbilityAPI` is still a registered plugin identifier (it ships as a compatibility
> sub-plugin), so the gate above continues to resolve with the old string. The
> `com.hexvane.abilityapi.api.AbilityService` and `com.hexvane.abilityapi.ability.AbilityConditionSpec`
> classes are likewise frozen in place and forward to their `com.riprod` equivalents.

### 6.2 Public API: `AbilityService`

Use `com.riprod.abilityapi.api.AbilityService` from your mod:

```java
import com.riprod.abilityapi.api.AbilityService;
import com.riprod.abilityapi.ability.AbilityConditionSpec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.UUID;
```

Key methods:

- **Grant or update an ability:**

```java
AbilityService.setAbility(playerUuid, "move_speed", 1.3);
AbilityService.setAbility(playerUuid, "creative_flight", Boolean.TRUE);

// value and conditions in one call, so the ability is never briefly live without its conditions
AbilityService.setAbility(playerUuid, "stamina_regen", 1.5, conditions);
```

Note that these calls are **applied on the next world tick**, not synchronously - mutating an entity
has to be deferred onto the world thread. Reading state back in the same tick will still see the old
value.

- **Attach conditions:**

```java
List<AbilityConditionSpec> conditions = List.of(
    new AbilityConditionSpec(AbilityConditionSpec.TYPE_IN_ZONE, 7),
    new AbilityConditionSpec(AbilityConditionSpec.TYPE_IN_SUNLIGHT, 0)
);
AbilityService.setConditions(playerUuid, "stamina_regen", conditions);
```

- **Remove an ability:**

```java
AbilityService.removeAbility(playerUuid, "move_speed");
```

- **Re‑apply everything for a player:**

Rarely needed now - `setAbility` and `removeAbility` apply their own effects, and abilities are
re-applied automatically on login and world change. Use this only to force a full refresh:

```java
AbilityService.applyForPlayer(ref, store, world);
```

Parameters:

- `Ref<EntityStore> ref` - reference to the entity
- `ComponentAccessor<EntityStore> store` - the store/accessor from your system/command context
- `World world` - the world the player is in

### 6.3 Example: Species‑based abilities (Orbis Origins)

Orbis Origins is the primary consumer of Effectly and serves as a practical reference:

- Each species JSON defines an `abilities` array with:
  - `id`, `value`, `condition`, `metadata`, `name`, `description`
- When a species is selected:
  - Old species abilities are removed (`AbilityService.removeAbility`)
  - New species abilities are granted (`AbilityService.setAbility`)
  - Conditions are attached via `AbilityConditionSpec`
  - `AbilityService.applyForPlayer` is invoked so stats/movement update immediately

For more detail, see:

- `OrbisOrigins/src/main/java/com/riprod/orbisorigins/ability/AbilityApiBridge.java`
- `OrbisOrigins/src/main/resources/Species/*.json`

---

## 7. Troubleshooting & Tips

### 7.1 Abilities not applying

- Check `/ability list` to confirm the player actually has the ability.
- Verify that the ability ID is exactly one of the registered IDs from `/ability available`.
- Check the ability's file in `Server/Effectly/Effects/` has not been set `"Enabled": false`.
- For an offline player, check the log for the "Applied '<ability>' to the saved data of offline
  player" line confirming the write landed.
- Inspect `run/universe/players/<uuid>.json` - `Effectly:Roster` is the source of truth.

### 7.2 Conditions not behaving as expected

- Confirm the spec's `Type` names a file that exists in `Server/Effectly/Conditions/`, and that the
  file is not `"Enabled": false`. A grant pointing at a missing or disabled condition never
  activates, so the ability appears granted in `/ability list` but never takes effect.
- Confirm the condition's `Handler.Id` names a registered handler (`zone`, `sky_light`, `health`).
  A wrong id fails asset load with the registered ids listed.
- `target_health_*` conditions only pass inside a damage context, where a target entity exists.
  Anywhere else they fail closed.
- Set this class's log level to FINE to see each condition evaluated and whether it passed:
  `com.riprod.effectly.systems.AbilityConditionService`.

### 7.3 Performance considerations

- Avoid spamming ability changes every tick. Grant/remove abilities on discrete events (login, species selection, equipment change) and let Effectly handle the rest.
- Use conditions rather than constantly toggling abilities for state‑based behavior.
- Abilities cost nothing when nobody has them. Each one only ticks for players who actually hold it,
  and abilities that react to events (resistances, mining, breathing) never tick at all.

---

## 8. Where to Go Next

- **For server admins:**
  - Experiment with `/ability add` and `/ability remove` to give yourself movement or combat perks.
  - Combine Effectly with Orbis Origins to give species‑themed powers.
- **For mod authors:**
  - Use `AbilityService` to centralize any perk/bonus logic instead of re‑implementing movement/health/damage tweaks.
  - Use condition specs to keep your logic data‑driven.

If you extend Effectly or build a mod that uses it, consider mirroring the patterns in Orbis Origins so players get a consistent experience across mods.

If you need more help, have feature requests, or want to share integrations, you can join the Effectly support Discord (see the mod’s download page for an invite link).