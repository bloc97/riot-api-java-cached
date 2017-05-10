/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.database;

import net.bloc97.riot.cache.cached.GenericObjectCache;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static net.bloc97.riot.cache.CachedRiotApi.isRateLimited;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.runes.dto.RunePages;
import net.rithms.riot.api.request.ratelimit.RateLimitException;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class RunesDatabase implements CachedDatabase {
    private static final long LIFE = TimeUnit.MINUTES.toMillis(20); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Long, GenericObjectCache<RunePages>> runesCache; //Maps summoner ID to RunePages
    
    public RunesDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        runesCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    private RunePages updateRunesBySummoner(long id, Date now) {
        RunePages data = null;
        try {
            data = rApi.getRunesBySummoner(platform, id);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateRunesBySummoner(id, now);
            }
            System.out.println(ex);
            runesCache.remove(id);
        }
        if (data != null) {
            runesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    public RunePages getRunesBySummoner(long id) {
        Date now = new Date();
        
        GenericObjectCache<RunePages> cache = runesCache.get(id);
        if (cache == null) {
            return updateRunesBySummoner(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateRunesBySummoner(id, now);
        }
    }

    @Override
    public void purge() {
        runesCache.clear();
    }
    
    
}
