package smartin.miapi.datapack;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.handler.codec.DecoderException;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HierarchicalReloadBuilder<B, E extends HierarchicalReloadBuilder.Extension<B>> {

    private final String location;
    private Codec<B> baseCodec;
    private Codec<E> extCodec;
    private Runnable clear = () -> {
    };
    private ReloadHandlerBuilder.SingleDecodedFileHandler<B> baseHandler = (isClient, path, data, access) -> {
    };
    private ReloadHandlerBuilder.SingleDecodedFileHandler<E> extensionHandler = (isClient, path, data, access) -> {
    };
    private float priority = 0f;

    private HierarchicalReloadBuilder(String location, Codec<B> baseCodec, Codec<E> extensionCodec) {
        this.location = location;
        this.baseCodec = baseCodec;
        this.extCodec = extensionCodec;
    }

    public static <B, E extends Extension<B>> HierarchicalReloadBuilder<B, E> builder(String location, Codec<B> baseCodec, Codec<E> extensionCodec) {
        return new HierarchicalReloadBuilder<>(location, baseCodec, extensionCodec);
    }

    public static <T, E extends Extension<T>> Codec<BaseOrExtension<T, E>> createCodec(
            Codec<T> baseCodec,
            Codec<E> extCodec
    ) {
        return Codec.either(extCodec, baseCodec).xmap(
                either -> either.map(
                        BaseOrExtension.Ext::new,   // left = extension
                        BaseOrExtension.Base::new   // right = base
                ),
                boe -> {
                    if (boe instanceof BaseOrExtension.Ext<T, E> e) {
                        return Either.left(e.extension());
                    } else {
                        return Either.right(((BaseOrExtension.Base<T, E>) boe).base());
                    }
                }
        );
    }

    public HierarchicalReloadBuilder<B, E> clear(Runnable clear) {
        this.clear = clear;
        return this;
    }

    public HierarchicalReloadBuilder<B, E> baseHandler(ReloadHandlerBuilder.SingleDecodedFileHandler<B> handler) {
        this.baseHandler = handler;
        return this;
    }

    public HierarchicalReloadBuilder<B, E> extensionHandler(ReloadHandlerBuilder.SingleDecodedFileHandler<E> handler) {
        this.extensionHandler = handler;
        return this;
    }

    public HierarchicalReloadBuilder<B, E> priority(float priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Builds an EventListener without registering it
     */
    private ReloadEvents.EventListener build() {
        Codec<BaseOrExtension<B, E>> unionCodec = createCodec(baseCodec, extCodec);

        return (isClient, registryAccess, worker) -> {
            clear.run();

            Map<ResourceLocation, B> pendingBase = new HashMap<>();
            Map<ResourceLocation, E> pendingExts = new HashMap<>();

            // Collect all files
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                if (!path.getPath().startsWith(location + "/")) return;

                try {
                    JsonElement element = Miapi.gson.fromJson(data, JsonElement.class);
                    BaseOrExtension<B, E> decoded = unionCodec.decode(
                            RegistryOps.create(JsonOpsBooleanPatched.INSTANCE, registryAccess),
                            element
                    ).getOrThrow(s -> new DecoderException("Could not decode " + path + " " + s)).getFirst();

                    ResourceLocation shortId = Miapi.id(path.toString()
                            .replaceFirst(location + "/", "")
                            .replaceFirst(".json", ""));

                    if (decoded instanceof BaseOrExtension.Base<B, E> b) {
                        pendingBase.put(shortId, b.base());
                    } else if (decoded instanceof BaseOrExtension.Ext<B, E> e) {
                        pendingExts.put(shortId, e.extension());
                    }
                } catch (RuntimeException ex) {
                    Miapi.LOGGER.error("Failed to decode {}", path, ex);
                }
            });

            // Process base modules
            for (var entry : pendingBase.entrySet()) {
                baseHandler.reloadFile(isClient, entry.getKey(), entry.getValue(), registryAccess);
            }

            // Resolve extensions
            Map<ResourceLocation, E> remaining = new HashMap<>(pendingExts);
            boolean progress;
            do {
                progress = false;
                Iterator<Map.Entry<ResourceLocation, E>> it = remaining.entrySet().iterator();
                while (it.hasNext()) {
                    var entry = it.next();
                    E ext = entry.getValue();
                    B base = pendingBase.get(ext.target());
                    if (base == null) continue;

                    extensionHandler.reloadFile(isClient, entry.getKey(), ext, registryAccess);

                    B extended = ext.applyTo(base);
                    baseHandler.reloadFile(isClient, entry.getKey(), extended, registryAccess);
                    pendingBase.put(entry.getKey(), extended);

                    it.remove();
                    progress = true;
                }
            } while (progress);

            for (var entry : remaining.entrySet()) {
                Miapi.LOGGER.error(
                        "Unresolved extension {} for target {} (missing or cyclic dependency)",
                        entry.getKey(), entry.getValue().target()
                );
            }

            pendingBase.clear();
            pendingExts.clear();
        };
    }

    /**
     * Convenience method: builds and registers immediately
     */
    public void register(ReloadEvent event) {
        event.subscribe(build(), priority);
        ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID, location);
    }

    public void register() {
        register(ReloadEvents.MAIN);
    }


    public sealed interface BaseOrExtension<T, E extends Extension<T>>
            permits BaseOrExtension.Base, BaseOrExtension.Ext {

        record Base<T, E extends Extension<T>>(T base) implements BaseOrExtension<T, E> {
        }

        record Ext<T, E extends Extension<T>>(E extension) implements BaseOrExtension<T, E> {
        }
    }

    public interface Extension<T> {
        ResourceLocation target();

        T applyTo(T base);
    }
}