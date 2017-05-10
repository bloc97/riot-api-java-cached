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
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.spectator.dto.FeaturedGames;
import net.rithms.riot.api.request.ratelimit.RateLimitException;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class SpectatorDatabase implements CachedDatabase {
    private static final long LIFE = TimeUnit.SECONDS.toMillis(30); //Caching Time to live
    private long featuredLife = TimeUnit.MINUTES.toMillis(10); //Caching Time to live, modified by suggested ttl from Riot API
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Long, GenericObjectCache<CurrentGameInfo>> currentGameCache; //Maps summoner ID to RunePages
    private GenericObjectCache<FeaturedGames> featuredCache;
    
    public SpectatorDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        currentGameCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    private CurrentGameInfo updateActiveGameBySummoner(long id, Date now) {
        CurrentGameInfo data = null;
        try {
            data = rApi.getActiveGameBySummoner(platform, id);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateActiveGameBySummoner(id, now);
            }
            System.out.println(ex);
            currentGameCache.remove(id);
        }
        if (data != null) {
            currentGameCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private FeaturedGames updateFeaturedGames(Date now) {
        FeaturedGames data = null;
        try {
            data = rApi.getFeaturedGames(platform);
            featuredLife = data.getClientRefreshInterval();
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateFeaturedGames(now);
            }
            System.out.println(ex);
            featuredCache = null;
        }
        if (data != null) {
            featuredCache = new GenericObjectCache(data, now, featuredLife);
        }
        return data;
    }
    
    public CurrentGameInfo getActiveGameBySummoner(long id) {
        Date now = new Date();
        
        GenericObjectCache<CurrentGameInfo> cache = currentGameCache.get(id);
        if (cache == null) {
            return updateActiveGameBySummoner(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateActiveGameBySummoner(id, now);
        }
    }
    public FeaturedGames getFeaturedGames() {
        Date now = new Date();
        
        GenericObjectCache<FeaturedGames> cache = featuredCache;
        if (cache == null) {
            return updateFeaturedGames(now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateFeaturedGames(now);
        }
    }

    @Override
    public void purge() {
        currentGameCache.clear();
        featuredCache = null;
    }
    
    
}
