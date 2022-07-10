package io.github.iprodigy.cache.provider.ehcache;

import io.github.iprodigy.cache.api.Cache;
import io.github.iprodigy.cache.api.ICacheSpec;
import io.github.iprodigy.cache.api.domain.ExpiryType;
import io.github.iprodigy.cache.api.domain.RemovalCause;
import io.github.iprodigy.cache.core.AbstractCacheProvider;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Provides {@link Cache} instances using {@link org.ehcache.core.Ehcache} in heap-mode.
 * <p>
 * Implements size and time-based expiry.
 * <p>
 * Specifying {@link ICacheSpec#maxSize()} is highly recommended.
 */
public final class EhcacheProvider extends AbstractCacheProvider {

	@Override
	public <K, V> Cache<K, V> build(ICacheSpec<K, V> spec) {
		CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

		//noinspection unchecked
		final CacheConfigurationBuilder<Object, Object>[] builder = new CacheConfigurationBuilder[] {
			CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, poolBuilder(spec.maxSize()))
		};

		handleExpiration(spec.expiryTime(), spec.expiryType(), (time, type) -> {
			if (type == ExpiryType.POST_WRITE)
				builder[0] = builder[0].withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(time));
			else
				builder[0] = builder[0].withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(time));
		});

		if (spec.removalListener() != null) {
			builder[0] = builder[0].withService(
				CacheEventListenerConfigurationBuilder.newEventListenerConfiguration(
					e -> {
						//noinspection unchecked
						spec.removalListener().onRemoval((K) e.getKey(), (V) e.getOldValue(), getCause(e.getType()));
					},
					EventType.EVICTED, EventType.EXPIRED, EventType.REMOVED, EventType.UPDATED
				)
			);
		}

		org.ehcache.Cache<Object, Object> cache = manager.createCache(UUID.randomUUID().toString(), builder[0]);
		return new EhcacheDelegate<>(cache);
	}

	private static ResourcePoolsBuilder poolBuilder(Long maxSize) {
		if (maxSize == null)
			return ResourcePoolsBuilder.newResourcePoolsBuilder().heap(Runtime.getRuntime().maxMemory() / 2, MemoryUnit.B);
		return ResourcePoolsBuilder.heap(maxSize);
	}

	private static RemovalCause getCause(EventType type) {
		switch (type) {
			case EVICTED:
				return RemovalCause.SIZE;
			case EXPIRED:
				return RemovalCause.TIME;
			case REMOVED:
				return RemovalCause.MANUAL;
			case UPDATED:
				return RemovalCause.REPLACED;
			default:
				return RemovalCause.OTHER;
		}
	}

}
