package com.bigyun.provider.core.snapshot;

import com.bigyun.common.core.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Provider 运行时快照 Redis 存取组件。
 * <p>
 * 读取顺序由执行器控制，这里只负责 Redis 和本地短缓存，不向外暴露快照正文，避免敏感字段被误打印。
 * </p>
 */
public class ProviderRuntimeSnapshotStore
{
    private static final Logger log = LoggerFactory.getLogger(ProviderRuntimeSnapshotStore.class);

    private static final Duration LOCAL_CACHE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Cache<String, ProviderRuntimeSnapshot> localCache = Caffeine.newBuilder()
            .maximumSize(256)
            .expireAfterWrite(LOCAL_CACHE_TTL)
            .build();

    public ProviderRuntimeSnapshotStore(StringRedisTemplate redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    public Optional<ProviderRuntimeSnapshot> getByCapability(String capabilityCode)
    {
        if (StringUtils.isBlank(capabilityCode))
        {
            return Optional.empty();
        }
        return read(ProviderRuntimeSnapshotKeys.capabilityKey(capabilityCode));
    }

    public Optional<ProviderRuntimeSnapshot> getByProvider(String configType, String providerCode, String operation)
    {
        if (StringUtils.isBlank(configType) || StringUtils.isBlank(providerCode) || StringUtils.isBlank(operation))
        {
            return Optional.empty();
        }
        return read(ProviderRuntimeSnapshotKeys.providerKey(configType, providerCode, operation));
    }

    public List<SnapshotEntry> listSnapshots()
    {
        List<SnapshotEntry> result = new ArrayList<>();
        scanSnapshots(ProviderRuntimeSnapshotKeys.CAPABILITY_PREFIX, result);
        scanSnapshots(ProviderRuntimeSnapshotKeys.PROVIDER_PREFIX, result);
        return result;
    }

    public boolean publishCapabilitySnapshot(ProviderRuntimeSnapshot snapshot)
    {
        if (snapshot == null || StringUtils.isBlank(snapshot.getCapabilityCode()))
        {
            return false;
        }
        return write(ProviderRuntimeSnapshotKeys.capabilityKey(snapshot.getCapabilityCode()), snapshot);
    }

    public boolean publishProviderSnapshot(ProviderRuntimeSnapshot snapshot)
    {
        if (snapshot == null || StringUtils.isBlank(snapshot.getConfigType())
                || StringUtils.isBlank(snapshot.getProviderCode()) || StringUtils.isBlank(snapshot.getOperation()))
        {
            return false;
        }
        return write(ProviderRuntimeSnapshotKeys.providerKey(snapshot.getConfigType(),
                snapshot.getProviderCode(), snapshot.getOperation()), snapshot);
    }

    public void deleteCapabilitySnapshot(String capabilityCode)
    {
        delete(ProviderRuntimeSnapshotKeys.capabilityKey(capabilityCode));
    }

    public void deleteProviderSnapshot(String configType, String providerCode, String operation)
    {
        delete(ProviderRuntimeSnapshotKeys.providerKey(configType, providerCode, operation));
    }

    public void invalidateLocalCache()
    {
        localCache.invalidateAll();
    }

    private void scanSnapshots(String keyPrefix, List<SnapshotEntry> result)
    {
        ScanOptions options = ScanOptions.scanOptions().match(keyPrefix + "*").count(200).build();
        try (Cursor<String> cursor = redisTemplate.scan(options))
        {
            while (cursor.hasNext())
            {
                String key = cursor.next();
                read(key).ifPresent(snapshot -> result.add(new SnapshotEntry(key, snapshot)));
            }
        }
        catch (Exception e)
        {
            log.warn("Provider runtime snapshot scan failed: keyPrefix={}, message={}", keyPrefix, e.getMessage());
        }
    }

    private Optional<ProviderRuntimeSnapshot> read(String key)
    {
        ProviderRuntimeSnapshot cached = localCache.getIfPresent(key);
        if (cached != null)
        {
            return Optional.of(cached);
        }
        try
        {
            String json = redisTemplate.opsForValue().get(key);
            if (StringUtils.isBlank(json))
            {
                return Optional.empty();
            }
            ProviderRuntimeSnapshot snapshot = objectMapper.readValue(json, ProviderRuntimeSnapshot.class);
            localCache.put(key, snapshot);
            return Optional.of(snapshot);
        }
        catch (Exception e)
        {
            log.warn("Provider runtime snapshot read failed: key={}, message={}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean write(String key, ProviderRuntimeSnapshot snapshot)
    {
        try
        {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(snapshot));
            redisTemplate.opsForValue().set(ProviderRuntimeSnapshotKeys.VERSION_KEY, snapshot.getVersion());
            localCache.put(key, snapshot);
            log.info("Provider runtime snapshot published: key={}, version={}", key, snapshot.getVersion());
            return true;
        }
        catch (Exception e)
        {
            log.warn("Provider runtime snapshot publish failed: key={}, message={}", key, e.getMessage());
            return false;
        }
    }

    private void delete(String key)
    {
        try
        {
            redisTemplate.delete(key);
            localCache.invalidate(key);
            log.info("Provider runtime snapshot deleted: key={}", key);
        }
        catch (Exception e)
        {
            log.warn("Provider runtime snapshot delete failed: key={}, message={}", key, e.getMessage());
        }
    }

    public static class SnapshotEntry
    {
        private final String redisKey;

        private final ProviderRuntimeSnapshot snapshot;

        public SnapshotEntry(String redisKey, ProviderRuntimeSnapshot snapshot)
        {
            this.redisKey = redisKey;
            this.snapshot = snapshot;
        }

        public String getRedisKey()
        {
            return redisKey;
        }

        public ProviderRuntimeSnapshot getSnapshot()
        {
            return snapshot;
        }
    }
}
