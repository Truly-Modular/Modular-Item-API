# IconRenderProperty

A property that configures the rendering of module icons in the GUI. Controls which icon type is displayed and applies transformations for proper positioning.

## Purpose

Allows customizing the icon rendering for modular items in the GUI. Supports different icon types and applies matrix transformations for proper positioning relative to other GUI elements.

## Behavior

- The icon type determines what is rendered (default is "item")
- Matrix transformations are applied for proper positioning
- GUI offsets can be used to adjust icon position
- Icons are cached for performance
- Only affects GUI rendering, not item functionality

## JSON Structure

```json5
{
	type: 'string (required)'
}
```

### Fields

- **`type`** _(string, required)_:
  The icon type to render. Valid values:
  - `"item"` - Renders the item icon (default)
  - Other types can be registered for custom icons

## Examples

### Default Item Icon

```json5
{
	type: 'item'
}
```

### Custom Icon Type

```json5
{
	type: 'custom_icon'
}
```

### No Icon (Empty Object)

```json5
{}
```

## Notes

- This property works in conjunction with `GuiOffsetProperty` for positioning
- The icon is rendered using the module instance as the owner
- Transformations from `GuiOffsetProperty` are automatically applied
