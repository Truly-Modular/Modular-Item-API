@header Properties
@path /data_types/properties
@keywords property, properties

- Properties are responsible for providing modules with effects, statistics, and other attributes.
- The API includes an extensive selection of properties.
- Full list of included properties can be found [here](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List).
- Addons can register their properties if so desired. It is adviced for addons to use ModID_property name to avoid collisions  
  They need to register like seen in the [RegistryInventory](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/registries/RegistryInventory.java)
- Each property has its own way of handling JSON components, witch can be viewed in their Java class
