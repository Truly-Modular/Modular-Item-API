@header DoubleResolveable
@path  /data_types/double_resolvable
@keywords Double Resolvable, Doubleresolveable, DoubleResolvable

The `DoubleResolvable` system allows defining numeric values in multiple flexible ways, supporting both static values and dynamically computed results through math expressions and stat resolution.

## Basic Forms

A `DoubleResolvable` can take several forms depending on the desired complexity and source of the value:

### 1. Constant Number
```json
"durability": 5
```
A fixed numeric value.

### 2. Simple Expression String
```json
"durability": "[material.durability]*0.3"
```
An expression that references other stats and performs arithmetic operations. Expressions must be strings and support:
- Most math operations should work, *,+,-,/,% log, sq and so one, we use [Eval Ex](https://ezylang.github.io/EvalEx/references/functions.html) to parse them.

### 3. Operation-Based Object
```json
"durability": {
  "operation": "**",
  "value": "-0.15"
}
```
This form represents a transformation to apply to an already resolved base value. The available operations mirror typical math symbols:
- `"+"` addition
- `"*"` additive multiplication (all multiplier are added together and then multiplied)
- `"**"` multiplicative (each multiplier multiplies with the previous number)
This system is basicly exactly how vanilla attributes also work.
  

### 4. List of Operations (Chained)
```json
"armor_pen": [
  {
    "operation": "**",
    "value": "[module.some.stat]/20"
  },
  {
    "operation": "+",
    "value": "5+[module.some.stat]*2"
  }
]
```
A sequential application of multiple operations.
The order doesn't matter, ass additive, then additive multiplicative and then total multiplicative are executed.

---

## Resolvers

Resolvers are special placeholders in expressions (inside square brackets: `[]`) that dynamically fetch values from item context. These are the currently supported resolvers:

### `material`
```json
"[material.someStat]"
```
- Resolves the value of `someStat` defined in the material.
- Defaults to `0` if the stat is not set.
- See the Materials for available stats.

### `module`
```json
"[module.someStat]"
```
- Resolves a stat from the current module.
- Defaults to `0` if undefined.
- Uses ModuleStat property

### `module-material` / `material-module`
```json
"[module-material.durability]"
"[material-module.durability]"
```
- Attempts to resolve from the first prefix (`module` or `material`), and if the stat isn't found, falls back to the second.

### `count`
Resolves to numeric counts related to item structure:

- `[count.module]` – total number of modules on the item
- `[count.submodules]` – total number of submodules, relative from the current module
- `[count.unique_materials]` – number of distinct materials used
- `[count.root_material_matches]` – number of materials across the item matching the first module's material
- `[count.material_matches]` – number of materials matching the current module's material

### `slot`
- `[count.slot_name]` switches the context module to the one in the slot if available. otherwise defaults to 0
example: `[count.blade.material.hardness]` will check the submodule in the blade slot for its material hardness

### `collect`
Supports aggregation across multiple modules.

#### Variants:
- `collect.add`
- `collect.max`
- `collect.min`
- `collect.average`

Each can be followed by any valid resolver path:

```json
"[collect.max.material.durability]"
```
- Gathers the target stat from all relevant items (e.g., materials) and computes the max.

---

## Notes
- Unresolvable stats return `0` by default.
- You can chain and nest operations using list syntax for complex value computation.

---

By leveraging `DoubleResolvable`, you can configure your data with both simplicity and power — from hardcoded constants to dynamic, stat-based calculations.
