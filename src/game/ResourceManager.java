package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ResourceManager {
    private final Map<String, Resource> resources = new HashMap<>();

    public ResourceManager load(String id, Resource resource) {
        if (resources.containsKey(id)) {
            resources.remove(id).deinit();
        }
        resource.init();
        resources.put(id, resource);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Resource> T get(String id, Class<T> res) {
        Resource r = resources.get(id);
        if (r == null) return null;
        if (!r.getClass().isInstance(res)) {
            System.out.println("ResourceManager error.");
            System.out.println("Incorrect class type given for id " + id);
            return null;
        }
        return (T) r;
    }

    public <T extends Resource> T getOrLoad(String id, Class<T> res, Supplier<T> loader) {
        T r = get(id, res);
        if (r == null) r = loader.get();
        return r;
    }

    public void unloadAll() {
        System.out.println("Unloading all resources ...");
        long start = System.currentTimeMillis();
        resources.values().forEach(Resource::deinit);
        resources.clear();
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Unload Done... " + elapsed + "ms");
    }
}
