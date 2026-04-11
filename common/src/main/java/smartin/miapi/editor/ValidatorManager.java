package smartin.miapi.editor;

import dev.architectury.event.EventResult;
import smartin.miapi.blueprint.BlueprintComponent;
import smartin.miapi.editor.syntax.CodecValidatorInterface;
import smartin.miapi.editor.syntax.ModuleValidatorInterface;
import smartin.miapi.editor.syntax.SynergyEditor;
import smartin.miapi.material.codec.CodecMaterial;
import smartin.miapi.material.codec.CodecMaterialExtension;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.key.MiapiBinding;

public class ValidatorManager {
    static void setupValidators() {
        EditorEvents.EDITOR_INTERFACES.register(event -> {
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/modules/")) {
                event.interfaces.add(new ModuleValidatorInterface());
                //event.interfaces.add(new PropertyMapHighlighter(event.resourceLocation));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/synergies/")) {
                event.interfaces.add(new SynergyEditor());
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/modular_converters/")) {
                event.interfaces.add(new CodecValidatorInterface(ModuleInstance.CODEC, "Modular Converter Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/material/")) {
                event.interfaces.add(new CodecValidatorInterface(CodecMaterial.CODEC, "Material Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/blueprints/")) {
                event.interfaces.add(new CodecValidatorInterface(BlueprintComponent.CODEC, "Blueprint Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/key_binding")) {
                event.interfaces.add(new CodecValidatorInterface(MiapiBinding.CODEC, "KeyBind Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/create_options/")) {
                //TODO:validator
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/material_extension/")) {
                event.interfaces.add(new CodecValidatorInterface(CodecMaterialExtension.CODEC, "KeyBind Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/module_extension/")) {
                //TODO:validator
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/skin/module")) {
                //TODO:validator
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/skin/tab")) {
                //TODO:validator
            }
            return EventResult.pass();
        });
    }
}
