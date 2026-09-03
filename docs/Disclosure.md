---
title: "Disclosure"
order: 3
published: true
draft: false
---
# Disclosure

Effectly was originally a fork of [AbilityAPI](https://www.curseforge.com/hytale/mods/abilityapi) by Hexvane. 

Originally, all AbilityAPI had was **Abilities** and **Conditions**

Effectly has all but upended the original AbilityAPI while remaining fully backwards-compatible - working as a drop-in replacement. 

It also added 3 new systems **Triggers** **Actions** and **Effects** all while making it use LESS server resources and reducing server overhead and improving performance and data accuracy.

Before, AbilityAPI would check per-tick per-player per-ability - meaning with 5 people online, it may be running 30 per-tick events even if nobody had a single ability! 

Effectly now only ticks when you need it. If nobody has an **Ability** active, there is no per-tick event. All triggers are pure ECS events, marker components are used with optimized queries, and persistence is now on the player.

In addition, the entire system is now asset-backed and registry-driven - meaning any other addon can add more abilities, triggers, actions, effects, and conditions without going absolutely insane!

Overall, the mod is hardly the same as it was before, but I wanted to call out that this was originally a fork of an existing mod and Hexvane should get the proper credits for the original idea.