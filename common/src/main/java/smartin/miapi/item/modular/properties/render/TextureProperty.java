package smartin.miapi.item.modular.properties.render;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.ColorUtil;
import smartin.miapi.client.model.DynamicBakedModel;
import smartin.miapi.client.model.ModelLoadAccessor;
import smartin.miapi.datapack.SpriteLoader;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class TextureProperty implements ModuleProperty {
    public static final String key = "texture";
    public static Function<SpriteIdentifier, Sprite> textureGetter;
    static BakedModel model;
    private static Function<SpriteIdentifier, Sprite> mirroredGetter;
    private static final String cacheKey = Miapi.MOD_ID + ":model";
    private static final Map<String, Json> modelMap = new HashMap<>();
    private static final Map<JsonUnbakedModel, String> modelPathMap = new HashMap<>();
    private static ItemModelGenerator generator;

    public TextureProperty() {
        mirroredGetter = (identifier) -> {
            return textureGetter.apply(identifier);
        };
        generator = new ItemModelGenerator();
        ModularItemCache.setSupplier(cacheKey, (stack) -> {
            return generateModel(stack);
        });
    }

    public static BakedModel generateModel(ItemStack stack) {
        ItemModule.ModuleInstance modules = ModularItem.getModules(stack);
        List<ItemModule.ModuleInstance> moduleInstances = modules.allSubModules();
        List<BakedQuad> quads = new ArrayList<>();
        AtomicReference<Float> scaleAdder = new AtomicReference<>(1.0f);
        moduleInstances.forEach(moduleI -> {
            Json json = modelMap.get(moduleI.module.getName());
            if (json != null) {
                JsonUnbakedModel unbakedModel = json.jsonUnbakedModel;
                scaleAdder.updateAndGet(v -> (v + 0.001f));
                Transform transform = json.transform.copy();
                transform = Transform.merge(SlotProperty.getTransform(moduleI), transform);
                transform.scale.multiplyComponentwise(scaleAdder.get(), scaleAdder.get(), scaleAdder.get());
                ModelBakeSettings settings = ModelRotation.X0_Y0;
                BakedModel model = bakeModel(unbakedModel, mirroredGetter, transform, ColorUtil.getModuleColor(moduleI), settings);
                quads.addAll(model.getQuads(null, null, Random.create()));
                for (Direction dir : Direction.values()) {
                    quads.addAll(model.getQuads(null, dir, Random.create()));
                }
            }
        });
        return new DynamicBakedModel(quads);
    }


    public static BakedModel bakeModel(JsonUnbakedModel unbakedModel, Function<SpriteIdentifier, Sprite> textureGetter, Transform transform, int color, ModelBakeSettings settings) {
        ModelLoader modelLoader = ModelLoadAccessor.getLoader();
        AtomicReference<JsonUnbakedModel> actualModel = new AtomicReference<>(unbakedModel);
        unbakedModel.getModelDependencies().stream().filter(identifier -> identifier.toString().equals("minecraft:item/generated") || identifier.toString().contains("handheld")).findFirst().ifPresent(identifier -> {
            actualModel.set(generator.create(mirroredGetter, unbakedModel));
        });
        actualModel.set(transformModel(actualModel.get(), transform));
        BakedModel model = actualModel.get().bake(modelLoader, unbakedModel.getRootModel(), textureGetter, settings, new Identifier(unbakedModel.id), true);
        return ColorUtil.recolorModel(model, color);
    }

    public static JsonUnbakedModel transformModel(JsonUnbakedModel model, Transform transform) {
        // Create a new model with the same properties as the original
        // Apply the transformations to each element in the model
        for (ModelElement element : model.getElements()) {
            transformModel(element, transform);
        }

        return model;
    }

    private static ModelElement transformModel(ModelElement element, Transform transform) {
        // apply translation
        element.from.add(transform.translation);
        element.to.add(transform.translation);

        // apply rotation

        // apply scale
        element.from.multiplyComponentwise(1 / transform.scale.getX(), 1 / transform.scale.getY(), 1 / transform.scale.getZ());
        element.to.multiplyComponentwise(transform.scale.getX(), transform.scale.getY(), transform.scale.getZ());

        return element;
    }

    /*
    private static ModelElement transformModel(ModelElement element, Transform transform) {
        // Copy the original element and its values
        Vec3f from = element.from.copy();
        Vec3f to = element.to.copy();
        Map<Direction, ModelElementFace> faces = new HashMap<>(element.faces);
        net.minecraft.client.render.model.json.ModelRotation rotation = element.rotation;
        boolean shade = element.shade;

        // Apply translation to the 'from' and 'to' vectors
        from.add(transform.translation);
        to.add(transform.translation);

        // Apply rotation to the 'from' and 'to' vectors
        if (rotation != null) {
            // Get the origin of the rotation and subtract it from the 'from' and 'to' vectors
            Vec3f origin = rotation.origin;
            from.subtract(origin);
            to.subtract(origin);

            // Apply the rotation angle around the rotation axis
            Vec3i axisVector = Direction.from(rotation.axis, Direction.AxisDirection.POSITIVE).getVector();
            float angle = (float) Math.toRadians(rotation.angle) * (false ? -1 : 1);
            Quaternion quat = new Quaternion(new Vec3f(axisVector.getX(),axisVector.getY(),axisVector.getZ()), angle,false);
            from.rotate(quat);
            to.rotate(quat);

            // If rescaling is enabled, multiply the 'from' and 'to' vectors by the scaling factor
            if (rotation.rescale) {
                from.multiplyComponentwise(transform.scale.getX(),transform.scale.getY(),transform.scale.getZ());
                to.multiplyComponentwise(transform.scale.getX(),transform.scale.getY(),transform.scale.getZ());
            }

            // Add the origin vector back to the 'from' and 'to' vectors
            from.add(origin);
            to.add(origin);
        }

        // Apply scaling to the 'from' and 'to' vectors
        from.multiply(transform.scale);
        to.multiply(transform.scale);

        // Create a new ModelElement object with the transformed values
        return new ModelElement(from, to, faces, rotation, shade);
    }
     */

    public static BakedModel getModel(ItemStack stack) {
        return (BakedModel) ModularItemCache.get(stack, cacheKey);
    }

    public static JsonUnbakedModel loadModelFromFilePath(String filePath) {
        try {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            Identifier modelId = new Identifier(filePath);
            Resource resource = resourceManager.getResource(modelId).orElseThrow(() -> new RuntimeException("could not load model from path " + filePath));
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            JsonUnbakedModel model = JsonUnbakedModel.deserialize(reader);
            model.getElements().forEach(modelElement -> {
                modelElement.faces.forEach((direction, modelElementFace) -> {
                });
            });
            model.id = modelId.toString();

            loadTextureDependencies(model);

            return model;
        } catch (IOException exception) {
            exception.printStackTrace();
            new RuntimeException("could not load model from path " + filePath);
        }
        return null;
    }

    protected static void loadTextureDependencies(JsonUnbakedModel model) {

        List<Identifier> spritesToLoad = new ArrayList<>();
        bakeModel(model, (identifier) -> {
            spritesToLoad.add(identifier.getTextureId());
            return mirroredGetter.apply(identifier);
        }, Transform.IDENTITY, 0, ModelRotation.X0_Y0);
        SpriteLoader.spritesToAdd.addAll(spritesToLoad);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Gson gson = new Gson();
        Json propertyJson = gson.fromJson(data.toString(), Json.class);
        propertyJson.repair();
        propertyJson.jsonUnbakedModel = loadModelFromFilePath(propertyJson.path);
        if (propertyJson.jsonUnbakedModel == null) {
            throw new Exception("could not find model path");
        }
        modelMap.put(moduleKey, propertyJson);
        return true;
    }

    public class Json {
        @Nullable
        public JsonUnbakedModel jsonUnbakedModel;
        public String path;
        public Transform transform = Transform.IDENTITY;

        public void repair() {
            //this shouldn't be necessary as the values should be loaded from the class but anyways
            if (transform == null) {
                transform = Transform.IDENTITY;
            }
        }
    }
}
