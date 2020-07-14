package com.njyjz.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yjz.cache")
public class YjzCacheProperties
{
    /** key：cacheName， value：cache的具体配置 */
    private Map<String, CacheType> types = new LinkedHashMap<>();
    
    /** 缓存更新时通知其他节点的topic名称 */
    private String redisTopic = "yjzcache:topic";
    
    public Map<String, CacheType> getTypes()
    {
        return types;
    }

    public void setTypes(Map<String, CacheType> types)
    {
        this.types = types;
    }

    public String getRedisTopic()
    {
        return redisTopic;
    }

    public void setRedisTopic(String redisTopic)
    {
        this.redisTopic = redisTopic;
    }

    public static class CacheType
    {
        
        /** 缓存级别数，默认为1（只有Caffeine本地缓存），2包含Caffeine本地缓存和Redis远程缓存 */
        private int levels = 1;
        
        /** 是否存储空值，默认true，防止缓存穿透 */
        private boolean cacheNullValues = true;
        
        /** 是否动态根据cacheName创建Cache的实现，默认true */
        private boolean dynamic = true;
        
        private Redis redis = new Redis();
        
        private Caffeine caffeine = new Caffeine();
        
        public class Redis
        {     
            /** redis key的前缀 */
            private String keyPrefix;
            
            /** 对应redis key的过期时间,默认15分钟  */
            private long expiration = 9000000;   
            
            public long getExpiration()
            {
                return expiration;
            }

            public void setExpiration(long expiration)
            {
                this.expiration = expiration;
            }
            
            public String getKeyPrefix()
            {
                return keyPrefix;
            }
            
            public void setKeyPrefix(String keyPrefix)
            {
                this.keyPrefix = keyPrefix;
            }  
        }
        
        public class Caffeine
        {
            /** 访问后过期时间，单位毫秒 */
            private long expireAfterAccess;
            
            /** 写入后过期时间，单位毫秒 */
            private long expireAfterWrite;
            
            /** 写入后刷新时间，单位毫秒 */
            private long refreshAfterWrite;
            
            /** 初始化大小 */
            private int initialCapacity;
            
            /** 最大缓存对象个数，超过此数量时之前放入的缓存将失效 */
            private long maximumSize;
            
            /**
             * 由于权重需要缓存对象来提供，对于使用spring cache这种场景不是很适合，所以暂不支持配置
             */
//            private long maximumWeight;
            
            public long getExpireAfterAccess()
            {
                return expireAfterAccess;
            }
            
            public void setExpireAfterAccess(long expireAfterAccess)
            {
                this.expireAfterAccess = expireAfterAccess;
            }
            
            public long getExpireAfterWrite()
            {
                return expireAfterWrite;
            }
            
            public void setExpireAfterWrite(long expireAfterWrite)
            {
                this.expireAfterWrite = expireAfterWrite;
            }
            
            public long getRefreshAfterWrite()
            {
                return refreshAfterWrite;
            }
            
            public void setRefreshAfterWrite(long refreshAfterWrite)
            {
                this.refreshAfterWrite = refreshAfterWrite;
            }
            
            public int getInitialCapacity()
            {
                return initialCapacity;
            }
            
            public void setInitialCapacity(int initialCapacity)
            {
                this.initialCapacity = initialCapacity;
            }
            
            public long getMaximumSize()
            {
                return maximumSize;
            }
            
            public void setMaximumSize(long maximumSize)
            {
                this.maximumSize = maximumSize;
            }
        }
        
        public boolean isCacheNullValues()
        {
            return cacheNullValues;
        }
        
        public void setCacheNullValues(boolean cacheNullValues)
        {
            this.cacheNullValues = cacheNullValues;
        }
        
        public boolean isDynamic()
        {
            return dynamic;
        }
        
        public void setDynamic(boolean dynamic)
        {
            this.dynamic = dynamic;
        }
        

        public Redis getRedis()
        {
            return redis;
        }
        
        public void setRedis(Redis redis)
        {
            this.redis = redis;
        }
        
        public Caffeine getCaffeine()
        {
            return caffeine;
        }
        
        public void setCaffeine(Caffeine caffeine)
        {
            this.caffeine = caffeine;
        }

        public int getLevels()
        {
            return levels;
        }

        public void setLevels(int levels)
        {
            this.levels = levels;
        }
    }
    
    
}
