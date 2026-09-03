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