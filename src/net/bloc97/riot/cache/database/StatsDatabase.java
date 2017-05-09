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
import net.rithms.riot.api.endpoints.stats.dto.PlayerStatsSummary;
import net.rithms.riot.api.endpoints.stats.dto.PlayerStatsSummaryList;
import net.rithms.riot.api.endpoints.stats.dto.RankedStats;
import net.rithms.riot.constant.Platform;
import net.rithms.riot.constant.Region;

/**
 *
 * @author bowen
 */
@Deprecated
public class StatsDatabase {
    private static final long LIFE = TimeUnit.MINUTES.toMillis(20); //Caching Time to live
    public final int version = 1;
    
    private final RiotApi rApi;
    private final Region region;
    
    private final Map<Long, GenericObjectCache<PlayerStatsSummaryList>> summariesCache; //Maps summoner ID to stats summary
    private final Map<Long, GenericObjectCache<RankedStats>> rankedCache; //Maps summoner ID to ranked stats
    
    public StatsDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.region = Region.valueOf(platform.getName().toUpperCase());
        
        summariesCache = new HashMap<>();
        rankedCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    @Deprecated
    private PlayerStatsSummaryList updatePlayerStatsSummary(long id, Date now) {
        PlayerStatsSummaryList data = null;
        try {
            data = rApi.getPlayerStatsSummary(region, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            summariesCache.remove(id);
        }
        if (data != null) {
            summariesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    @Deprecated
    private RankedStats updatePlayerStatsRanked(long id, Date now) {
        RankedStats data = null;
        try {
            data = rApi.getRankedStats(region, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            rankedCache.remove(id);
        }
        if (data != null) {
            rankedCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    @Deprecated
    public PlayerStatsSummaryList getPlayerStatsSummary(long id) {
        Date now = new Date();
        
        GenericObjectCache<PlayerStatsSummaryList> cache = summariesCache.get(id);
        if (cache == null) {
            return updatePlayerStatsSummary(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updatePlayerStatsSummary(id, now);
        }
    }
    @Deprecated
    public RankedStats getPlayerStatsRanked(long id) {
        Date now = new Date();
        
        GenericObjectCache<RankedStats> cache = rankedCache.get(id);
        if (cache == null) {
            return updatePlayerStatsRanked(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updatePlayerStatsRanked(id, now);
        }
    }
    
    
}
