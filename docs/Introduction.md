---
title: "Introduction"
order: 1
published: true
draft: false
---
# Introduction
Welcome to Effectly!

# What Effectly Gives

| Name | What it does | Example |
| --- | --- | --- |
| **Trigger** | Triggers an ability | OnLand |
| **Action** | Instant Effect | Bounce |
| **Ability** | Passive Effect | 1.5x Swim Speed |
| **Condition** | Gates the effect | When in zone 2 |
| **Effect** | Brings it all together | In zone 2, 1.5x swim speed and bounce when hitting the ground |

# What you can do with it

There are four ways to grant effects

| Where | What | Why |
| --- | --- | --- |
| **Command** | Grant effects via commands | Usually testing, quick iterations |
| **Tags** | Grant effects via item tags | Always-enhanced items that grant effects. Think Swim Speed Boots, Shield of Repulsion, etc |
| **Metadata** | Grant effects via item metadata | Dynamic Enhanced Items. This lets you enchant a sword with fire damage, make boots walk faster, etc |
| **Code** | Grant effects via code | Code-level modifications lets addons define their own way of granting effects |

By itself, Effectly does _not_ do anything. No armor changes, no spedcial abilities, etc. This is _by design_ - it is up to another mod to grant these things.  

Check out the `References` section if you want to get a snippet of what the actual implementation looks like

# What it comes with

## Abilities
- **movement_state** - folds movement abilities onto engine state (flight, ground speed, swim speed)
- **dark_vision** - see in the dark
- **resistance** - percentage damage reduction against one damage cause
- **strength** - boosts outgoing melee damage
- **punch_damage** - boosts unarmed damage
- **fall_damage_immunity** - zeroes fall damage
- **invulnerability** - blocks all incoming damage
- **waterbreathing** - prevents drowning
- **oxygen** - extends breath underwater
- **mining_haste** - faster block breaking
- **mining_fortune** - extra drops from mined blocks
- **wall_climb** - climb solid surfaces by walking into them
- **stamina_regen** - faster stamina regeneration
- **health_regen** - passive health regeneration, delayed by taking damage
- **second_chance** - survive an otherwise-lethal hit, then goes on cooldown
- **item_magnet** - pulls nearby dropped items to the player
- **Thorns** - reflects a fraction of incoming damage back at the attacker

## Triggers
- **OnFall** - fires when the player takes fall damage, carrying the mutable damage
- **OnLand** - fires when the player lands from any fall, damaging or not, carrying the fall distance

## Actions
- **Bounce** - launches the holder upward on landing, scaled by fall distance
- **Gust** - pushes nearby entities away from the landing point
- **Burst** - damages nearby entities, scaled by fall distance
- **Heavy** - reduces or cancels the holder's fall damage
- **Vibranium** - absorbs fall damage and converts part of it into healing

## Conditions
- **in_zone** (`zone <id>`) - active in the given world zone(s)
- **Swimming** (`swimming`) - active while swimming
- **In_Liquid** (`in_liquid`) - active while touching liquid
- **in_sunlight** (`sunlight`) - active in open sunlight during daytime
- **Moonlight** (`moonlight`) - active under open night sky
- **health_above** (`health_above [%]`) - your health at or above the threshold
- **health_below** (`health_below [%]`) - your health below the threshold
- **target_health_above** (`target_health_above [%]`) - damage target health at or above the threshold
- **target_health_below** (`target_health_below [%]`) - damage target health below the threshold

## Effects
- **creative_flight**
- **move_speed**
- **swim_speed**
- **resistance_*** - one per damage cause
  - **bludgeoning**
  - **slashing**
  - **physical**
  - **projectile**
  - **fire**
  - **ice**
  - **poison**
  - **elemental**
  - **drowning**
  - **suffocation**
  - **fall**
  - **environment**
  - **environmental**
  - **outofworld**
  - **command**
- **fall_damage_immunity**
- **invulnerability**
- **waterbreathing**
- **oxygen**
- **strength**
- **punch_damage**
- **Thorns**
- **second_chance**
- **mining_haste**
- **mining_fortune**
- **dark_vision**
- **item_magnet**
- **health_regen**
- **stamina_regen**
- **wall_climb**
- **Bouncy** - bounce upward on landing
- **Gusty** - landing pushes nearby entities away
- **Bursty** - landing damages nearby entities
- **Heavy** - absorbs a fraction of fall damage
- **Vibranium** - converts fall damage into healing