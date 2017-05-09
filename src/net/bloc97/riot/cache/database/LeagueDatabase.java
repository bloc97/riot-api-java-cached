/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.database;

import net.bloc97.riot.cache.cached.GenericObjectCache;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.dto.LeagueList;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */


public class LeagueDatabase {
    
    public static enum Queue {
        RANKED_SOLO_5x5, RANKED_FLEX_SR, RANKED_FLEX_TT;
    }
    public static enum Rank {
        CHALLENGER, MASTER;
    }
    private static final long LIFE = TimeUnit.HOURS.toMillis(1); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Queue, GenericObjectCache<LeagueList>> challengerCache; //Maps queue to LeagueList
    private final Map<Queue, GenericObjectCache<LeagueList>> masterCache; //Maps queue to LeagueList
    private final Map<Long, GenericObjectCache<List<LeagueList>>> leaguesCache; //Maps summoner ID to a Map of LeagueList
    private final Map<Long, GenericObjectCache<Set<LeaguePosition>>> positionsCache; //Maps summoner ID to a Set of LeaguePosition
    
    public LeagueDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        challengerCache = new HashMap<>();
        masterCache = new HashMap<>();
        leaguesCache = new HashMap<>();
        positionsCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    private LeagueList updateLeague(Rank rank, Queue queue, Date now) {
        LeagueList data = null;
        try {
            if (rank == Rank.CHALLENGER) {
                data = rApi.getChallengerLeagueByQueue(platform, queue.toString());
            } else if (rank == Rank.MASTER) {
                data = rApi.getMasterLeagueByQueue(platform, queue.toString());
            }
        } catch (RiotApiException ex) {
            System.out.println(ex);
            if (rank == Rank.CHALLENGER) {
                challengerCache.remove(queue);
            } else if (rank == Rank.MASTER) {
                masterCache.remove(queue);
            }
        }
        if (data != null) {
            if (rank == Rank.CHALLENGER) {
                challengerCache.put(queue, new GenericObjectCache(data, now, LIFE));
            } else if (rank == Rank.MASTER) {
                masterCache.put(queue, new GenericObjectCache(data, now, LIFE));
            }
        }
        return data;
    }
    private List<LeagueList> updateLeagueBySummoner(long id, Date now) {
        List<LeagueList> data = null;
        try {
            data = rApi.getLeagueBySummonerId(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            leaguesCache.remove(id);
        }
        if (data != null) {
            leaguesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private Set<LeaguePosition> updateLeaguePositionsBySummoner(long id, Date now) {
        Set<LeaguePosition> data = null;
        try {
            data = rApi.getLeaguePositionsBySummonerId(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            positionsCache.remove(id);
        }
        if (data != null) {
            positionsCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    //Getters
    public LeagueList getLeague(Rank rank, Queue queue) {
        Date now = new Date();
        GenericObjectCache<LeagueList> cache;
        
        if (rank == Rank.CHALLENGER) {
            cache = challengerCache.get(queue);
        } else if (rank == Rank.MASTER) {
            cache = masterCache.get(queue);
        } else {
            return null;
        }
        if (cache == null) {
            return updateLeague(rank, queue, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateLeague(rank, queue, now);
        }
    }
    public List<LeagueList> getLeagueBySummoner(long id) {
        Date now = new Date();
        
        GenericObjectCache<List<LeagueList>> cache = leaguesCache.get(id);
        if (cache == null) {
            return updateLeagueBySummoner(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateLeagueBySummoner(id, now);
        }
    }
    public Set<LeaguePosition> getLeaguePositionsBySummoner(long id) {
        Date now = new Date();
        
        GenericObjectCache<Set<LeaguePosition>> cache = positionsCache.get(id);
        if (cache == null) {
            return updateLeaguePositionsBySummoner(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateLeaguePositionsBySummoner(id, now);
        }
    }
    
}
