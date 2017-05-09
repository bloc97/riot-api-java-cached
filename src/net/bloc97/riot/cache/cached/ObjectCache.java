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
public abstract class ObjectCache {
    
    public final Date liveUntil;
    
    public ObjectCache(Date currentDate, long cacheLife) {
        this.liveUntil = new Date(currentDate.getTime() + cacheLife);
    }
    public boolean isValid(Date now) {
        return liveUntil.after(now);
    }
}
