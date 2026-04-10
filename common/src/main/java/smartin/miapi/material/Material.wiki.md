@header Material-Related
@path /data_types/material/properties

Many Properties are directly related to control and interface with materials and their logic.
- ``"material_property"`` a list of allowed Material Properties for the module
- ``"allowed_material"`` a list of allowed materials and material groups
- ``inscribe_data_on_craft`` inscribes the used material ingredient onto the module
- ``inscribe_on_craft`` inscribes the material ingredient onto the item (used for potion arrows)
- ``material_overwrite`` allows for semi-dynamic merging with a manual material (used my cosmetics to inject additional rendering)
- ``material`` allows you setting a fallback material, but since miapi 2.0.0 the material inscribed in module data is used over this.
- ``material_indication`` controls a small tooltip informing the player about used material stats