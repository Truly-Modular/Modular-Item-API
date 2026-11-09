@header Crystal Model Property
@path /data_types/properties/render/crystal

A property that enables end crystal model rendering for modular items.
Yeah, this should be reworked to also include a transform.

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
