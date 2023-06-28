package smartin.miapi.registries;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    public RegistrySupplier<T> delegate(Identifier id) {
        return registrar.delegate(id);
    }

    @Override
    public <E extends T> RegistrySupplier<E> register(Identifier id, Supplier<E> supplier) {
        RegistrySupplier<E> sup = registrar.register(id, supplier);
        sup.listen(e -> this.register(id.toString(), e));
        return sup;
    }

    @Override
    public @Nullable Identifier getId(T obj) {
        return registrar.getId(obj);
    }

    @Override
    public int getRawId(T obj) {
        return registrar.getRawId(obj);
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T obj) {
        return Optional.empty();
    }

    @Override
    public @Nullable T get(Identifier id) {
        return this.get(id.toString());
    }

    @Override
    public @Nullable T byRawId(int rawId) {
        return registrar.byRawId(rawId);
    }

    @Override
    public boolean contains(Identifier id) {
        return this.entries.containsKey(id.toString());
    }
    public boolean contains(String id) {
        return this.entries.containsKey(id);
    }

    @Override
    public boolean containsValue(T obj) {
        return this.entries.containsValue(obj);
    }

    @Override
    public Set<Identifier> getIds() {
        return this.entries.keySet().stream().map(Identifier::new).collect(Collectors.toSet());
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> entrySet() {
        return registrar.entrySet();
    }

    @Override
    public RegistryKey<? extends Registry<T>> key() {
        return registrar.key();
    }

    @Override
    public void listen(Identifier id, Consumer<T> callback) {
        registrar.listen(id, callback);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return registrar.iterator();
    }
}
