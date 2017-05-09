/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.cached;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bowen
 */
public class GenericMapCache<T> {
    
    public final Map<Long, GenericObjectCache<T>> map;
    public final Class<T> type;
    
    public GenericMapCache(Class<T> type) {
        this.type = type;
        this.map = new HashMap<>();
    }
    public Class<T> getType() {
        return type;
    }
    
}
