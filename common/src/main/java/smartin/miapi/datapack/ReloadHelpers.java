package smartin.miapi.datapack;

import smartin.miapi.Miapi;
import smartin.miapi.editor.DocPage;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.codec.CodecMaterial;
import smartin.miapi.material.codec.CodecMaterialExtension;
import smartin.miapi.material.composite.material.DatapackComposite;
import smartin.miapi.modules.CodecModuleInheritance;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ItemModuleExtension;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.properties.inventory.InventoryType;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.registries.RegistryInventory;

public class ReloadHelpers {
    /**
     * these need to be registered before most other things
     */
    public static void registerReloadHandlers() {

        HierarchicalReloadBuilder
                .builder("miapi/materials", CodecMaterial.CODEC, CodecMaterialExtension.CODEC)
                .clear(MaterialProperty.MATERIAL_REGISTRY::clear)
                .baseHandler((isClient, path, data, registryAccess) -> {
                    data.setID(path);
                    MaterialProperty.MATERIAL_REGISTRY.register(path, data);
                    data.generateConverters(isClient);
                })
                .priority(-2.0f)
                .register();
        ReloadHandlerBuilder
                .builder("miapi/material_extensions")
                .priority(-1.5f)
                .handler((isClient, path, data, registryAccess) -> MaterialProperty.loadMaterialExtention(path, data, registryAccess))
                .register();

        HierarchicalReloadBuilder
                .builder("miapi/modules", ItemModule.CODEC, CodecModuleInheritance.CODEC)
                .clear(RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY::clearTemporary)
                .baseHandler((isClient, path, data, registryAccess) -> {
                    data = new ItemModule(path, data.properties());
                    RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.registerTemporary(path, data);
                })
                .priority(-1.0f)
                .register();

        ReloadHandlerBuilder
                .builder("miapi/module_extensions")
                .priority(-0.4f)
                .handler(ReloadHandlerBuilder.DecodingFileHandler.from(
                        (isClient, path, data, access) -> ItemModuleExtension.loadModuleExtension(path, data, isClient),
                        (isClient, path, data, access) -> data.apply()))
                .register();
        ReloadHandlerBuilder
                .builder("miapi/wiki")
                .priority(0)
                .clear(DocPage.PAGE_LOOKUP::clear)
                .codec(DocPage.CODEC,
                        (isClient, path, data, registryAccess) -> DocPage.setupLookup(data))
                .register();
        ReloadHandlerBuilder
                .builder("miapi/data_composite")
                .clear(DatapackComposite.DATA_COMPOSITE_REGISTRY::clear)
                .codec(DatapackComposite.DATA_PACK_CODEC,
                        (isClient, path, data, registryAccess) -> DatapackComposite.DATA_COMPOSITE_REGISTRY.put(path, data))
                .register();
        ReloadHandlerBuilder
                .builder("miapi/modular_converter")
                .clear(ItemToModularConverter.regexes::clear)
                .handler((isClient, path, data, registryAccess) -> ItemToModularConverter.setupModularConverter(path, data, registryAccess))
                .priority(1)
                .register();
        ReloadHandlerBuilder
                .builder("miapi/skins/module")
                .priority(1)
                .clear(SkinOptions.skins::clear)
                .handler((isClient, path, data, registryAccess) -> SkinOptions.load(path, data))
                .register();
        ReloadHandlerBuilder
                .builder("miapi/skins/tab")
                .priority(1)
                .clear(SkinOptions.tabMap::clear)
                .handler((isClient, path, data, registryAccess) -> SkinOptions.loadTabData(data))
                .register();
        ReloadHandlerBuilder
                .builder("miapi/synergies")
                .priority(2f)
                .clear(SynergyManager::clear)
                .codec(SynergyManager.SYNERGY_CODEC,
                        (isClient, path, data, access) -> data.register())
                .register();
        ReloadHandlerBuilder
                .builder("miapi/create_options")
                .priority(10)
                .clear(CreateItemOption.CREATE_ITEM_MIAPI_REGISTRY::clear)
                .handler((isClient, path, data, registryAccess) -> {
                    CreateItemOption.CreateItem createItem = Miapi.gson.fromJson(data, CreateItemOption.JsonCreateItem.class);
                    if (createItem.getBaseModule() != null && createItem.getItem() != null) {
                        CreateItemOption.CREATE_ITEM_MIAPI_REGISTRY.register(path, createItem);
                    } else {
                        Miapi.LOGGER.error("could not find module or item for create option " + path);
                        Miapi.LOGGER.error(data);
                    }
                })
                .register();
        ReloadHandlerBuilder.builder("miapi/inventory_type")
                .clear(ItemInventoryManager.INVENTORY_TYPE_REGISTRY::clearTemporary)
                .syncToClient(true)
                .codec(InventoryType.CODEC,((isClient, path, data, registryAccess) ->
                        ItemInventoryManager.INVENTORY_TYPE_REGISTRY.registerTemporary(path,data.additionalSetup(path,registryAccess))))
                .priority(0.0f)
                .register();
    }


}
