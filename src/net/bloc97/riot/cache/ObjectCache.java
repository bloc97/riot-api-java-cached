/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;

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
