package smartin.miapi.modules.action;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ActionContext {
    private static final Map<Class<?>, Supplier<TypeManager<?>>> TYPE_MANAGER_REGISTRY = new HashMap<>();
    private final Map<Class<?>, TypeManager<?>> instanceTypeManagers = new HashMap<>();
    public ServerLevel level;
    public Player contextPlayer;
    public ItemStack contextItem;

    static {
        registerTypeManager(Entity.class, EntityTypeManager::new);
        registerTypeManager(Vec3.class, Vec3TypeManager::new);
    }

    public <T> void addListSupplier(Class<T> type, String key, Supplier<List<T>> listSupplier) {
        getTypeManager(type).putListSupplier(key, listSupplier, this);
    }

    public <T> void addObjectSupplier(Class<T> type, String key, Supplier<T> objectSupplier) {
        getTypeManager(type).putObjectSupplier(key, objectSupplier, this);
    }

    public <T> Optional<List<T>> getList(Class<T> type, String key) {
        return getTypeManager(type).getList(key, this);
    }

    public <T> Optional<T> getObject(Class<T> type, String key) {
        return getTypeManager(type).getObject(key, this);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeManager<T> getTypeManager(Class<T> type) {
        return (TypeManager<T>) instanceTypeManagers.computeIfAbsent(type, t -> {
            Supplier<TypeManager<?>> supplier = TYPE_MANAGER_REGISTRY.getOrDefault(t, DefaultTypeManager::new);
            return supplier.get();
        });
    }

    public static <T> void registerTypeManager(Class<T> type, Supplier<TypeManager<?>> managerSupplier) {
        TYPE_MANAGER_REGISTRY.put(type, managerSupplier);
    }

    public interface TypeManager<T> {
        void putListSupplier(String key, Supplier<List<T>> listSupplier, ActionContext actionContext);

        void putObjectSupplier(String key, Supplier<T> objectSupplier, ActionContext actionContext);

        Optional<List<T>> getList(String key, ActionContext actionContext);

        Optional<T> getObject(String key, ActionContext actionContext);
    }

    public static class DefaultTypeManager<T> implements TypeManager<T> {
        private final Map<String, Supplier<List<T>>> listSupplierMap = new HashMap<>();
        private final Map<String, Supplier<T>> objectSupplierMap = new HashMap<>();

        @Override
        public void putListSupplier(String key, Supplier<List<T>> listSupplier, ActionContext actionContext) {
            listSupplierMap.put(key, listSupplier);
        }

        @Override
        public void putObjectSupplier(String key, Supplier<T> objectSupplier, ActionContext actionContext) {
            objectSupplierMap.put(key, objectSupplier);
        }

        @Override
        public Optional<List<T>> getList(String key, ActionContext actionContext) {
            Supplier<List<T>> supplier = listSupplierMap.get(key);
            return supplier != null ? Optional.ofNullable(supplier.get()) : Optional.empty();
        }

        @Override
        public Optional<T> getObject(String key, ActionContext actionContext) {
            Supplier<T> supplier = objectSupplierMap.get(key);
            return supplier != null ? Optional.ofNullable(supplier.get()) : Optional.empty();
        }
    }

    public static class EntityTypeManager extends DefaultTypeManager<Entity> {
        @Override
        public void putObjectSupplier(String key, Supplier<Entity> entitySupplier, ActionContext actionContext) {
            super.putObjectSupplier(key, entitySupplier, actionContext);
            actionContext.addObjectSupplier(Vec3.class, key, () -> entitySupplier.get().position());
        }

        @Override
        public void putListSupplier(String key, Supplier<List<Entity>> entityListSupplier, ActionContext actionContext) {
            super.putListSupplier(key, entityListSupplier, actionContext);
            actionContext.addListSupplier(Vec3.class, key, () -> entityListSupplier.get().stream().map(Entity::position).toList());
        }
    }

    public static class Vec3TypeManager extends DefaultTypeManager<Vec3> {
        @Override
        public void putObjectSupplier(String key, Supplier<Vec3> vectorSupplier, ActionContext actionContext) {
            super.putObjectSupplier(key, vectorSupplier, actionContext);
            addComponentSuppliers(key, vectorSupplier, actionContext);
        }

        @Override
        public void putListSupplier(String key, Supplier<List<Vec3>> vectorListSupplier, ActionContext actionContext) {
            super.putListSupplier(key, vectorListSupplier, actionContext);
            vectorListSupplier.get().forEach(v -> addComponentSuppliers(key, () -> v, actionContext));
        }

        private void addComponentSuppliers(String key, Supplier<Vec3> vectorSupplier, ActionContext actionContext) {
            actionContext.addObjectSupplier(Float.class, key + ".x", () -> (float) vectorSupplier.get().x);
            actionContext.addObjectSupplier(Float.class, key + ".y", () -> (float) vectorSupplier.get().y);
            actionContext.addObjectSupplier(Float.class, key + ".z", () -> (float) vectorSupplier.get().z);
        }
    }
}
