package game;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ResourceManager {
    private final Map<String, Resource> resources = new HashMap<>();

    public int countLoadedResources() {
        return resources.size();
    }

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
        if (!res.isAssignableFrom(r.getClass())) {
            System.out.println("ResourceManager error.");
            System.out.println("Incorrect class type given for id " + id);
            System.out.println("Type=" + res);
            System.out.println("Expected=" + r.getClass());
            return null;
        }
        return (T) r;
    }

    public <T extends Resource> T getOrLoad(String id, Class<T> res, Supplier<T> loader) {
        T r = get(id, res);
        boolean alreadyPresent = true;
        if (r == null) {
            r = loader.get();
            load(id, r);
            alreadyPresent = false;
        }
        if (r == null) {
            System.out.println("Resource not found: " + id);
        } else if (!alreadyPresent) {
            System.out.println("Loaded resource: " + id);
        }
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
