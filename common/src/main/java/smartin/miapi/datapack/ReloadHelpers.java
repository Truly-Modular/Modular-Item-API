package smartin.miapi.datapack;

import smartin.miapi.Miapi;
import smartin.miapi.editor.DocPage;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.material.CodecMaterial;
import smartin.miapi.material.CodecMaterialExtension;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.composite.material.DatapackComposite;
import smartin.miapi.modules.CodecModuleExtension;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ItemModuleExtension;
import smartin.miapi.modules.abilities.key.KeyBindManager;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.registries.RegistryInventory;

public class ReloadHelpers {
    /**
     * these need to be registered before most other things
     */
    public static void registerReloadHandlers() {
        ReloadHandlerBuilder
                .builder("miapi/module_extensions")
                .priority(-0.4f)
                .handler(ReloadHandlerBuilder.DecodingFileHandler.from(
                        (isClient, path, data, access) -> ItemModuleExtension.loadModuleExtension(path, data, isClient),
                        (isClient, path, data, access) -> data.apply()))
                .register();
        ReloadHandlerBuilder
                .builder("miapi/synergies")
                .priority(2f)
                .clear(SynergyManager::clear)
                .codec(SynergyManager.SYNERGY_CODEC,
                        (isClient, path, data, access) -> data.register())
                .register();
        ReloadHandlerBuilder
                .builder("miapi/wiki")
                .priority(0)
                .clear(DocPage.PAGE_LOOKUP::clear)
                .codec(DocPage.CODEC,
                        (isClient, path, data, registryAccess) -> DocPage.setupLookup(data))
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
        ReloadHandlerBuilder
                .builder("miapi/key_binding")
                .handler((isClient, id, data, registryAccess) -> KeyBindManager.processKeybind(isClient, id, data))
                .register();
        ReloadHandlerBuilder
                .builder("miapi/data_composite")
                .clear(DatapackComposite.DATA_COMPOSITE_REGISTRY::clear)
                .codec(DatapackComposite.DATA_PACK_CODEC,
                        (isClient, path, data, registryAccess) -> DatapackComposite.DATA_COMPOSITE_REGISTRY.put(path, data))
                .register();
        ReloadHandlerBuilder
                .builder("miapi/material_extensions")
                .priority(-1.5f)
                .handler((isClient, path, data, registryAccess) -> MaterialProperty.loadMaterialExtention(path, data, registryAccess))
                .register();

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

        HierarchicalReloadBuilder
                .builder("miapi/modules", ItemModule.CODEC, CodecModuleExtension.CODEC)
                .clear(RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY::clear)
                .baseHandler((isClient, path, data, registryAccess) -> {
                    data = new ItemModule(path, data.properties());
                    RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.register(path, data);
                }).register();

        ReloadHandlerBuilder
                .builder("miapi/modular_converter")
                .clear(ItemToModularConverter.regexes::clear)
                .handler((isClient, path, data, registryAccess) -> ItemToModularConverter.setupModularConverter(path, data))
                .priority(1)
                .register();
    }


}
