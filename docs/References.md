---
title: "References"
order: 2
published: true
draft: false
---
# References

## > Commands

```
/ability add <effect_id> [value] [conditions...] [player]
/ability remove <effect_id> [player]
/ability list [player]
/ability available
```

Example Usage
```
/ability add Bouncy 2.5
/ability add move_speed 1.5 zone 3 swimming
/ability add health_regen 2 health_below 25 Steve
/ability remove Bouncy
```

## > JSON-level configurations

### Effects

`Server/Effectly/Effects/<Effect_Id>.json`

```json
{
  "Handler": {
    "Id": "Thorns",
    "DamageCause": "Effectly_Thorns"
  },
  "Type": "Numeric",
  "Default": 1.0,
  "Min": 0.0,
  "Max": 3.0,
  "Description": "Shown by /ability available",
  "Conditions": [
    { "Type": "Swimming" },
    { "Type": "in_zone", "Param": 3 },
    { "Type": "in_zone", "ZoneIds": [1, 3] },
    { "Type": "health_below", "Param": 25 }
  ],
  "Triggers": {
    "OnLand": [
      {
        "Type": "Bounce",
        "Threshold": { "Magnitude": 1.5 },
        "Cooldown": 0.5,
        "Restitution": 4.0
      }
    ],
    "OnFall": [
      {
        "Type": "Heavy",
        "Reduction": 1.0
      }
    ]
  },
  "Enabled": true
}
```

```json
{
  "Triggers": {
    "OnLand": [
      { "Type": "Bounce", "Threshold": { "Magnitude": 1.5 }, "Restitution": 4.0 }
    ]
  },
  "Type": "Numeric",
  "Default": 1.0,
  "Min": 0.0,
  "Max": 3.0
}
```

### Conditions

`Server/Effectly/Conditions/<Condition_Id>.json`

```json
{
  "Handler": {
    "Id": "health",
    "Target": "Self",
    "Comparison": "Below",
    "Threshold": 50
  },
  "Keyword": "health_below",
  "Description": "your health below %"
}
```

```json
{
  "Handler": {
    "Id": "in_liquid",
    "State": "Swimming"
  },
  "Keyword": "swimming",
  "Description": "active while swimming"
}
```

## > Tag-level configurations

### Basic Effect

```json
{
  "Tags": {
    "Effectly:Ability": ["Bouncy"]
  }
}
```

### With Condition

```json
{
  "Tags": {
    "Effectly:Ability": ["Bouncy::swimming"]
  }
}
```

```json
{
  "Tags": {
    "Effectly:Ability": ["Bouncy::zone 3 health_below 25"]
  }
}
```

### With Global Condition

```json
{
  "Tags": {
    "Effectly:Ability": ["Bouncy", "Heavy"],
    "Effectly:Condition": ["zone 3"]
  }
}
```

### With Multiplier

```json
{
  "Tags": {
    "Effectly:Ability": ["Bouncy:2.5"]
  }
}
```

### With All Three

```json
{
  "Tags": {
    "Effectly:Ability": ["Bouncy:2.5:swimming", "Heavy:0.75"],
    "Effectly:Condition": ["zone 3"]
  }
}
```

## > Metadata-level configurations

### Basic Effect

```java
stack = EquipmentAbilityMetadata.write(stack, Map.of("Bouncy", 1.0));
```

### With Condition

### With Global Condition

```java
stack = stack.withMetadata(EquipmentAbilityMetadata.CONDITIONS_KEY,
        Codec.STRING_ARRAY, new String[] { "zone 3", "swimming" });
```

### With Multiplier

```java
stack = EquipmentAbilityMetadata.write(stack, Map.of("Bouncy", 2.5));
```

### With All Three

```java
stack = EquipmentAbilityMetadata.write(stack, Map.of("Bouncy", 2.5, "Heavy", 0.75));
stack = stack.withMetadata(EquipmentAbilityMetadata.CONDITIONS_KEY,
        Codec.STRING_ARRAY, new String[] { "zone 3" });
```

## > Code-level configurations

### Basic Effect

```java
AbilityService.setAbility(playerUuid, "Bouncy", 1.0);
```

### With Condition

```java
AbilityService.setAbility(playerUuid, "Bouncy", 1.0,
        List.of(new AbilityConditionSpec("Swimming")));
```

```java
AbilityService.setAbility(playerUuid, "Bouncy", 1.0, List.of(
        new AbilityConditionSpec("health_below", 25),
        new AbilityConditionSpec("in_zone", null, List.of(1, 3))));
```

### With Global Condition

### With Multiplier

```java
AbilityService.setAbility(playerUuid, "Bouncy", 2.5);
```

### With All Three

```java
AbilityService.setAbility(playerUuid, "Bouncy", 2.5,
        List.of(new AbilityConditionSpec("Swimming")), "mymod:armor_set");
AbilityService.removeAbility(playerUuid, "Bouncy", "mymod:armor_set");
```

## > Registering a new...

### Effect

`Server/Effectly/Effects/My_Effect.json`

```json
{
  "Triggers": {
    "OnLand": [
      { "Type": "Bounce", "Threshold": { "Magnitude": 1.5 }, "Restitution": 4.0 }
    ]
  },
  "Type": "Numeric",
  "Default": 1.0,
  "Min": 0.0,
  "Max": 3.0,
  "Description": "Bounce back upward when landing from a fall"
}
```

### Trigger

```java
TriggerRegistry.register(new Trigger(OnMyThingProducerSystem.TRIGGER,
        registry -> registry.registerSystem(new OnMyThingProducerSystem())));
```

```java
public final class OnMyThingProducerSystem extends EntityTickingSystem<EntityStore> {

    public static final String TRIGGER = "OnMyThing";

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return ActionHolderComponent.getComponentType();
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        var ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;
        var holder = archetypeChunk.getComponent(index, ActionHolderComponent.getComponentType());
        var playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        var transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        var world = store.getExternalData().getWorld();
        if (holder == null || playerRef == null || transform == null || world == null) return;

        double magnitude = 1.0;
        ActionDispatch.fire(TRIGGER, ref, store, commandBuffer, world,
                holder, magnitude, transform.getPosition(), null, null);
    }
}
```

### Action

```java
public final class MyAction extends Action {

    @Nonnull
    public static final String ID = "MyAction";

    @Nonnull
    public static final BuilderCodec<MyAction> CODEC = BuilderCodec
            .builder(MyAction.class, MyAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Strength", Codec.DOUBLE),
                    (action, v) -> action.strength = v,
                    action -> action.strength)
            .add()
            .build();

    private double strength = 1.0;

    @Override
    public boolean execute(@Nonnull ActionContext context) {
        Velocity velocity = context.getStore().getComponent(
                context.getHolderRef(), Velocity.getComponentType());
        if (velocity == null) return false;
        velocity.addInstruction(
                new Vector3d(0, strength * context.getValue() * context.getMagnitude(), 0),
                null, ChangeVelocityType.Add);
        return true;
    }
}
```

```java
ActionRegistry.register(MyAction.ID, MyAction.class, MyAction.CODEC);
```

```json
{
  "Triggers": {
    "OnLand": [
      {
        "Type": "MyAction",
        "Threshold": { "Magnitude": 2.0 },
        "Cooldown": 1.0,
        "Strength": 3.0
      }
    ]
  }
}
```

### Ability

```java
public final class MyAbilityHandler implements AbilityHandler {

    @Nonnull
    public static final String ID = "My_Ability";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        MyAbilityComponent.register(registry);
        registry.registerSystem(new MyAbilitySystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        if (context.getComponents().getComponent(context.getRef(), MyAbilityComponent.getComponentType()) != null) return;
        context.getComponents().putComponent(context.getRef(),
                MyAbilityComponent.getComponentType(), new MyAbilityComponent());
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        if (AbilityHandlerRegistry.holdsAnyFor(context, this)) return;
        context.getComponents().tryRemoveComponent(context.getRef(), MyAbilityComponent.getComponentType());
    }
}
```

```java
AbilityHandlerRegistry.register(new MyAbilityHandler());
```

```java
var active = AbilityConditionUtils.bestActiveForHandler(
        ref, store, world, MyAbilityHandler.ID);
if (active == null) return;
double value = active.value();
```

