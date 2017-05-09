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
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.masteries.dto.MasteryPages;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class MasteriesDatabase {
    private static final long LIFE = TimeUnit.MINUTES.toMillis(20); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Long, GenericObjectCache<MasteryPages>> masteriesCache; //Maps summoner ID to MasteryPages
    
    public MasteriesDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        masteriesCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    private MasteryPages updateMasteriesBySummoner(long id, Date now) {
        MasteryPages data = null;
        try {
            data = rApi.getMasteriesBySummoner(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            masteriesCache.remove(id);
        }
        if (data != null) {
            masteriesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    public MasteryPages getMasteriesBySummoner(long id) {
        Date now = new Date();
        
        GenericObjectCache<MasteryPages> cache = masteriesCache.get(id);
        if (cache == null) {
            return updateMasteriesBySummoner(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateMasteriesBySummoner(id, now);
        }
    }
    
    
}
