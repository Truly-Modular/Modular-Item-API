package smartin.miapi.registries;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MiapiRegistrar<T> extends MiapiRegistry<T> implements Registrar<T> {
    public final Registrar<T> registrar;

    protected MiapiRegistrar(Registrar<T> registrar) {
        this.registrar = registrar;
    }
    public static <T> MiapiRegistrar<T> of(Registrar<T> r) {
        return new MiapiRegistrar<>(r);
    }

    @Override
    public RegistrySupplier<T> delegate(ResourceLocation id) {
        return registrar.delegate(id);
    }

    @Override
    public <E extends T> RegistrySupplier<E> register(ResourceLocation id, Supplier<E> supplier) {
        RegistrySupplier<E> sup = registrar.register(id, supplier);
        sup.listen(e -> this.register(id, e));
        return sup;
    }

    public <E extends T> void registerWithoutRegistrar(ResourceLocation id, E object) {
        register(id, object);
    }

    @Override
    public @Nullable ResourceLocation getId(T obj) {
        return registrar.getId(obj);
    }

    @Override
    public int getRawId(T obj) {
        return registrar.getRawId(obj);
    }

    @Override
    public Optional<ResourceKey<T>> getKey(T obj) {
        return Optional.empty();
    }

    @Override
    public @Nullable T get(ResourceLocation id) {
        return super.get(id);
    }

    @Override
    public @Nullable T byRawId(int rawId) {
        return registrar.byRawId(rawId);
    }

    @Override
    public boolean contains(ResourceLocation id) {
        return this.entries.containsKey(id);
    }

    @Override
    public boolean containsValue(T obj) {
        return this.entries.containsValue(obj);
    }

    @Override
    public Set<ResourceLocation> getIds() {
        return new HashSet<>(this.entries.keySet());
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return registrar.entrySet();
    }

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return registrar.key();
    }

    @Override
    public @Nullable Holder<T> getHolder(ResourceKey<T> key) {
        return registrar.getHolder(key);
    }

    @Override
    public void listen(ResourceLocation id, Consumer<T> callback) {
        registrar.listen(id, callback);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return registrar.iterator();
    }
}
