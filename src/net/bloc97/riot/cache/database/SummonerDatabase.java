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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bloc97.riot.cache.CachedRiotApi.Limiter;
import static net.bloc97.riot.cache.CachedRiotApi.isRateLimited;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.api.request.ratelimit.RateLimitException;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class SummonerDatabase {
    private static final long LIFE = TimeUnit.MINUTES.toMillis(20); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Long, GenericObjectCache<Summoner>> summonerCache; //Maps summoner ID to summoner
    
    public SummonerDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        summonerCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    private Summoner updateSummoner(long id, Date now) {
        
        Summoner data = null;
        try {
            data = rApi.getSummoner(platform, id);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateSummoner(id, now);
            }
            System.out.println(ex);
            summonerCache.remove(id);
        }
        
        if (data != null) {
            summonerCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private Summoner updateSummonerByName(String name, Date now) {
        
        Summoner data = null;
        try {
            data = rApi.getSummonerByName(platform, name);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateSummonerByName(name, now);
            }
            System.out.println(ex);
        }
        if (data != null) {
            summonerCache.put(data.getId(), new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private Summoner updateSummonerByAccount(long accountId, Date now) {
        
        Summoner data = null;
        try {
            data = rApi.getSummonerByAccount(platform, accountId);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateSummonerByAccount(accountId, now);
            }
            System.out.println(ex);
        }
        if (data != null) {
            summonerCache.put(data.getId(), new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    //Fetchers, fetches data from cache
    private GenericObjectCache<Summoner> fetchSummonerByName(String name) {
        name = name.toLowerCase();
        for (HashMap.Entry<Long, GenericObjectCache<Summoner>> cacheEntry : summonerCache.entrySet()) { //Try finding summoner in cache
            GenericObjectCache<Summoner> cache = cacheEntry.getValue();
            if (cache.getObject().getName().toLowerCase().equals(name)) { //Lowercase since unique names are not case sensitive
                return cache;
            }
        }
        return null;
    }
    private GenericObjectCache<Summoner> fetchSummonerByAccount(long accountId) {
        for (HashMap.Entry<Long, GenericObjectCache<Summoner>> cacheEntry : summonerCache.entrySet()) { //Try finding summoner in cache
            GenericObjectCache<Summoner> cache = cacheEntry.getValue();
            if (cache.getObject().getAccountId() == accountId) {
                return cache;
            }
        }
        return null;
    }
    
    public Summoner getSummoner(long id) {
        Date now = new Date();
        
        GenericObjectCache<Summoner> cache = summonerCache.get(id);
        if (cache == null) {
            return updateSummoner(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateSummoner(id, now);
        }
    }
    public Summoner getSummonerByName(String name) {
        Date now = new Date();
        
        GenericObjectCache<Summoner> cache = fetchSummonerByName(name);
        if (cache == null) { //If didn't find summoner in cache
            return updateSummonerByName(name, now);
        }
        if (cache.liveUntil.after(now)) { //Found summoner, TTL is not yet reached
            return cache.getObject();
        } else { //If found summoner in cache, and TTL was reached, refresh the entry.
            updateSummoner(cache.getObject().getId(), now);
            return getSummonerByName(name); //Recursive function, since name change might break the cache and search.
        }
    }
    public Summoner getSummonerByAccount(long accountId) {
        Date now = new Date();
        
        GenericObjectCache<Summoner> cache = fetchSummonerByAccount(accountId);
        if (cache == null) { //If didn't find summoner in cache
            return updateSummonerByAccount(accountId, now);
        }
        if (cache.liveUntil.after(now)) { //Found summoner, TTL is not yet reached
            return cache.getObject();
        } else { //If found summoner in cache, and TTL was reached, refresh the entry.
            updateSummoner(cache.getObject().getId(), now);
            return getSummonerByAccount(accountId); //Recursive function, since account id change might break the cache and search.
        }
    }
    
    //Extra helper functions
    public boolean summonerNameExists(String name) {
        return getSummonerByName(name) != null;
    }
    
    
}
