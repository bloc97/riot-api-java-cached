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
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchTimeline;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class MatchDatabase {
    private static final long LIFE = TimeUnit.MINUTES.toMillis(20); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Long, GenericObjectCache<Match>> matchesCache; //Maps match ID to Match
    private final Map<Long, GenericObjectCache<MatchTimeline>> timelinesCache; //Maps match ID to Match Timeline
    private final Map<Long, GenericObjectCache<MatchList>> matchListsRankedCache; //Maps account ID to MatchList
    private final Map<Long, GenericObjectCache<MatchList>> matchListsRecentCache; //Maps account ID to Recent MatchList
    
    public MatchDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        matchesCache = new HashMap<>();
        timelinesCache = new HashMap<>();
        matchListsRankedCache = new HashMap<>();
        matchListsRecentCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    private Match updateMatch(long id, Date now) {
        Match data = null;
        try {
            data = rApi.getMatch(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            matchesCache.remove(id);
        }
        if (data != null) {
            matchesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private MatchList updateRankedMatchListByAccountId(long id, Date now) {
        MatchList data = null;
        try {
            data = rApi.getMatchListByAccountId(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            matchListsRankedCache.remove(id);
        }
        if (data != null) {
            matchListsRankedCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private MatchList updateRecentMatchListByAccountId(long id, Date now) {
        MatchList data = null;
        try {
            data = rApi.getRecentMatchListByAccountId(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            matchListsRecentCache.remove(id);
        }
        if (data != null) {
            matchListsRecentCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private MatchTimeline updateMatchTimeline(long id, Date now) {
        MatchTimeline data = null;
        try {
            data = rApi.getTimelineByMatchId(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            timelinesCache.remove(id);
        }
        if (data != null) {
            timelinesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    
    public Match getMatch(long id) {
        Date now = new Date();
        
        GenericObjectCache<Match> cache = matchesCache.get(id);
        if (cache == null) {
            return updateMatch(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateMatch(id, now);
        }
    }
    public MatchList getRankedMatchListByAccountId(long id) {
        Date now = new Date();
        
        GenericObjectCache<MatchList> cache = matchListsRankedCache.get(id);
        if (cache == null) {
            return updateRankedMatchListByAccountId(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateRankedMatchListByAccountId(id, now);
        }
    }
    public MatchList getRecentMatchListByAccountId(long id) {
        Date now = new Date();
        
        GenericObjectCache<MatchList> cache = matchListsRecentCache.get(id);
        if (cache == null) {
            return updateRecentMatchListByAccountId(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateRecentMatchListByAccountId(id, now);
        }
    }
    public MatchTimeline getMatchTimeline(long id) {
        Date now = new Date();
        
        GenericObjectCache<MatchTimeline> cache = timelinesCache.get(id);
        if (cache == null) {
            return updateMatchTimeline(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateMatchTimeline(id, now);
        }
    }
    
    
    
}
