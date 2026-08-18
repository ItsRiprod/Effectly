# Effectly

Complex Effects made **Simple** and **Universal**. 

Effectly works as a centralized hub for all multiple mods to list, manage, utilize, or extend the same list of gameplay abilities. 

---
Attribution:
<div class="spoiler">
Effectly was forked from the AbilityAPI by Hexvane. It functions as a more performant and extendable not-vibe-coded drop-in replacement. It is 100% backwards compatible with any mod that previously required AbilityAPI
</div>

# Features
- Enchantable Armor - Add Conditional Effects to armor pieces to give them another level of flair
- Asset-Driven - Making new effects or conditions can be done entirely via the asset editor! Tweaking existing ones can be done just as easily
- Performant - You only pay for what is active. 400 unused effects will have the same performance hit as 1 on your server.

# Abilities
1. Creative Flight
2. Dark Vision
3. Fall Damage Immunity
4. Health Regen
5. Invulnerability
6. Item Magnet
7. Mining Fortune
8. Mining Haste
9. Move Speed
10. Oxygen
11. Unarmed Punch Damage
12. Waterbreathing
13. Second Change
14. Stamina Regen
15. Strength
16. Swim Speed
17. Wall Climb
18. Resistance
- Commands
- Drowning
- Elements
- Environments / Environmental
- Fall
- Fire
- Ice
- Void
- Physical
- Poison
- Projectile
- Slashing
- Suffocation

And these are just the defaults! Third-party mods can add more as they need

# Enchanting Armor

There are two ways to enchant armor - and for players, no, this is not accessible in survival. This is for mod owners looking to integrate!

## #1- Predefined in the Tags
```json
{
  "Tags": {
    "Effectly:Ability": [
      "mining_haste:3:sunlight", // conditionally adds haste 3 when in sunlight
      "flight" // adds flight
    ],
    "Effectly:Condition": [
      "zone 3" // gates the entire ability behind being in zone 3
    ]
  }
}
```
This can be done on your item's actual Item JSON file

Or, you can add it via the Asset Editor
![image](https://media.forgecdn.net/attachments/description/null/description_30edd724-a60c-4c65-8ad9-e982c54e1425.png)

It is also recommended to install **Patchly** if you are planning on editing existing items.

## #2 Item Metadata

Add the object MetaData (i.e. crafting, via code, dynamic per-item abilities for if you want individuality)

```json
  "PrimaryOutput": {
        "ItemId": "Armor_Iron_Chest",
        "Quantity": 1,
        "Metadata": {
          "Effectly:Abilities": {
            "creative_flight": 1.0,
            "move_speed": 1.5
          },
          "Effectly:Conditions": ["zone 3"]
        }
      },
```

# Enchanting Weapons
*Currently disabled for the time being due to performance reasons*

While I have the code, I was not happy with the implementation. Waiting on some Hexcode abilities to land before I finish out this implementation because I want to do it right.

# Creating/Configuring Abilities or Conditions

New abilities can be configured via the asset editor (or raw json mutation)
![image](https://media.forgecdn.net/attachments/description/null/description_112c9d0d-485e-4fcd-82ac-db2b7fc647b9.png)

Allowing you full control over what an ability does, how it is configured, and how it behaves. For server owners, this lets you force-disable unwanted abilities, configure power levels, and balance abilities to fit best within your server.

---

*All in all, this was just a fun weekend project! It was sparked from the vibecoded app having performance issues on another server. The Github at the time was licensed CC0 - so I took the liberty of actually doing this idea justice. Please, feel free to reach out to `Riprod` on discord! I'm most places hytale is.*