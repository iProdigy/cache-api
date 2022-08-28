package io.github.xanthic.cache.provider.ehcache;

import io.github.xanthic.cache.core.LockedAbstractCache;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unchecked")
public class EhcacheDelegate<K, V> extends LockedAbstractCache<K, V> {
	org.ehcache.Cache<Object, Object> cache;

	@Override
	protected V getUnlocked(@NotNull K key) {
		return (V) cache.get(key);
	}

	@Override
	protected void putUnlocked(@NotNull K key, @NotNull V value) {
		cache.put(key, value);
	}

	@Override
	protected void removeUnlocked(@NotNull K key) {
		cache.remove(key);
	}

	@Override
	protected void clearUnlocked() {
		cache.clear();
	}

	@Override
	protected long sizeUnlocked() {
		long n = 0;
		for (org.ehcache.Cache.Entry<Object, Object> ignored : cache) {
			n++;
		}
		return n;
	}

	@Override
	public V putIfAbsent(@NotNull K key, @NotNull V value) {
		return read(() -> (V) cache.putIfAbsent(key, value));
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		read(() -> {
			cache.putAll(map);
			return Void.TYPE;
		});
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V value) {
		return read(() -> cache.replace(key, value) != null);
	}

	@Override
	public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
		return read(() -> cache.replace(key, oldValue, newValue));
	}
}
