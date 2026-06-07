# CrystalModelProperty

A property that enables or disables crystal model rendering for modular items.

## Purpose

Provides a simple toggle to render or hide crystal models. When enabled, a crystal model is rendered as part of the modular item. Useful for creating crystal formations, decorative crystals, or crystal-based items.

## Behavior

- When set to true, renders a `CrystalModel` as part of the item
- When set to false (default), no crystal is rendered
- The crystal model is created and rendered on the client side
- Does not affect item functionality

## JSON Structure

```json5
{
	enabled: 'boolean (optional, default = false)'
}
```

### Fields

- **`enabled`** _(boolean, optional, default = false)_:
  If true, renders a crystal model. If false, no crystal is rendered.

## Examples

### Enable Crystal

```json5
{
	enabled: true
}
```
