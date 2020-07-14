package com.njyjz.cache;

import java.io.Serializable;

public class CacheMessage implements Serializable
{
    /** */
    private static final long serialVersionUID = 5987219310442078193L;
    
    private String srcIp;
    
    private String cacheName;
    
    private Object key;
    
    public CacheMessage(String srcIp, String cacheName, Object key)
    {
        super();
        this.srcIp = srcIp;
        this.cacheName = cacheName;
        this.key = key;
    }
    
    public String getCacheName()
    {
        return cacheName;
    }
    
    public void setCacheName(String cacheName)
    {
        this.cacheName = cacheName;
    }
    
    public Object getKey()
    {
        return key;
    }
    
    public void setKey(Object key)
    {
        this.key = key;
    }

    public String getSrcIp()
    {
        return srcIp;
    }

    public void setSrcIp(String srcIp)
    {
        this.srcIp = srcIp;
    }  
}
