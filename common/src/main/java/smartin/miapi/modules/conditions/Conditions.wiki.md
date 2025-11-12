@header Conditions  
@path /data_types/condition

# Conditions

Conditions are a **core component** of the system that define logical requirements for various gameplay, data, and configuration scenarios.  
They allow dynamic evaluation of whether certain actions, states, or configurations should be active or valid — giving creators fine-grained control over how different elements interact.

---

## Overview

A **Condition** represents a logical statement that can evaluate to **true** or **false** within a given context.  
When a condition evaluates to `true`, the corresponding behavior, property, or feature becomes active or available.  
If it evaluates to `false`, that behavior may be hidden, disabled, or skipped entirely.

Conditions are used across a variety of systems — not just crafting — and can depend on:

- Module configurations and relationships
- Material properties and categories
- Player advancements
- External mod integrations
- expandable by other java addons

Each condition is defined as a small JSON object, specifying a `"type"` field that determines the logic or check to perform.  
Additional fields depend on the specific condition type.

---

## Behavior and Context

Conditions are always evaluated within a **context**, meaning their checks are relative to where they are used.  
For example, a condition inside a **module context** evaluates using that module’s data, and the condition can be shifted to other modules of the item via some of the conditions.

## Custom Errors

Many condition types support an optional `"error"` field.  
When provided, and the condition evaluates to `false`, the system displays this error message in the relevant interface (e.g., a crafting button tooltip).  
This allows developers to give user-friendly feedback about why an action failed or is unavailable.

```json
{
    "type": "material_group",
    "material_group": "fabric",
    "error": "Requires a fabric material!"
}
