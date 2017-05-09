/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.cached;

import java.util.Date;

/**
 *
 * @author bowen
 */
public class GenericObjectCache<T> extends ObjectCache {
    
    public final T object;
    
    public GenericObjectCache(T data, Date currentDate, long life) {
        super(currentDate, life);
        this.object = data;
    }
    public T getObject() {
        return object;
    }
    public Class<T> getType() {
        return (Class<T>)object;
    }
    
}
