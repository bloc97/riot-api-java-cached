/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.database;

import net.bloc97.riot.cache.cached.GenericObjectCache;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.champion.dto.Champion;
import net.rithms.riot.api.endpoints.champion.dto.ChampionList;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class ChampionDatabase {
    private static final long LIFE = TimeUnit.HOURS.toMillis(12); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private GenericObjectCache<ChampionList> championsCache;
    
    public ChampionDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
    }
    
    //Updaters, calls RiotApi for cache updates
    private ChampionList updateChampions(Date now) {
        ChampionList data = null;
        try {
            data = rApi.getChampions(platform);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            championsCache = null;
        }
        if (data != null) {
            championsCache = new GenericObjectCache(data, now, LIFE);
        }
        return data;
    }
    
    public ChampionList getChampions() {
        Date now = new Date();
        
        GenericObjectCache<ChampionList> cache = championsCache;
        if (cache == null) {
            return updateChampions(now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateChampions(now);
        }
    }
    
    public List<Champion> getChampions(boolean freeToPlay) {
        ChampionList championsList = getChampions();
        if (championsList == null) {
            return new LinkedList();
        }
        List<Champion> champions = championsList.getChampions();
        
        LinkedList<Champion> filteredChampions = new LinkedList<>();
        for (Champion champion : champions) {
            if (champion.isFreeToPlay() == freeToPlay) {
                filteredChampions.add(champion);
            }
        }
        return filteredChampions;
    }
    public Champion getChampion(int id) {
        ChampionList championsList = getChampions();
        if (championsList == null) {
            return null;
        }
        List<Champion> champions = championsList.getChampions();
        
        for (Champion champion : champions) {
            if (champion.getId() == id) {
                return champion;
            }
        }
        return null;
    }
    
}
