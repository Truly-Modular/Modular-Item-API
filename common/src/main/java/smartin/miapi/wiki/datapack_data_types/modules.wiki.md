@header Modules
@path /datapack_data_types/modules
@keywords module, modules, module instance

- a seperate JSON found in miapi/modules
- The API does not include any pre-built modules.
- Modules are not included within the API itself, but instead are outsourced to the addons to add.
- Modules need to be added to data/miapi/modules/anyPathOrNameFromHere.json
- Examples of modules can be found [here](https://github.com/Truly-Modular/Arsenal/tree/master/arsenal-common/src/main/resources/data/miapi/modules).
- Modules require the field of `name`, names should be unqiue to the module and are used to identify the module
- the rest of the Json is a Map of [Properties](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List)
