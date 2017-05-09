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
import java.util.concurrent.TimeUnit;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.champion_mastery.dto.ChampionMastery;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class ChampionMasteryDatabase {
    private static final long LIFE = TimeUnit.MINUTES.toMillis(20); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Long, GenericObjectCache<List<ChampionMastery>>> championMasteriesCache; //Maps summoner ID to ChampionMastery
    private final Map<Long, GenericObjectCache<Integer>> scoresCache; //Maps summoner ID to score
    
    public ChampionMasteryDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        championMasteriesCache = new HashMap<>();
        scoresCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    private List<ChampionMastery> updateChampionMasteriesBySummoner(long id, Date now) {
        List<ChampionMastery> data = null;
        try {
            data = rApi.getChampionMasteriesBySummoner(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            championMasteriesCache.remove(id);
        }
        if (data != null) {
            championMasteriesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private int updateChampionMasteryScoresBySummoner(long id, Date now) {
        Integer data = null;
        try {
            data = rApi.getChampionMasteryScoresBySummoner(platform, id);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            championMasteriesCache.remove(id);
        }
        if (data != null) {
            championMasteriesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    
    
    public List<ChampionMastery> getChampionMasteriesBySummoner(long id) {
        Date now = new Date();
        
        GenericObjectCache<List<ChampionMastery>> cache = championMasteriesCache.get(id);
        if (cache == null) {
            return updateChampionMasteriesBySummoner(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateChampionMasteriesBySummoner(id, now);
        }
    }
    public ChampionMastery getChampionMasteriesBySummonerByChampion(long id, int championId) {
        List<ChampionMastery> championMasteries = getChampionMasteriesBySummoner(id);
        
        for (ChampionMastery cm : championMasteries) {
            if (cm.getChampionId() == championId) {
                return cm;
            }
        }
        return null;
    }
    public int getChampionMasteryScoresBySummoner(long id) {
        Date now = new Date();
        
        GenericObjectCache<Integer> cache = scoresCache.get(id);
        if (cache == null) {
            return updateChampionMasteryScoresBySummoner(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateChampionMasteryScoresBySummoner(id, now);
        }
    }
    
}
