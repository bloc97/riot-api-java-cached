/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.static_data.constant.ChampData;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;
import net.rithms.riot.api.endpoints.static_data.dto.ChampionList;
import net.rithms.riot.api.endpoints.static_data.dto.Item;
import net.rithms.riot.api.endpoints.static_data.dto.ItemList;
import net.rithms.riot.api.endpoints.static_data.dto.MapData;
import net.rithms.riot.api.endpoints.static_data.dto.MasteryList;
import net.rithms.riot.api.endpoints.static_data.dto.ProfileIconData;
import net.rithms.riot.api.endpoints.static_data.dto.Realm;
import net.rithms.riot.api.endpoints.static_data.dto.RuneList;
import net.rithms.riot.api.endpoints.static_data.dto.SummonerSpellList;
import net.bloc97.riot.cache.static_data.dto.VersionData;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class StaticDataDatabase {
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Class, GenericObjectCache> listCache; //A Map of "List Objects"
    private final Map<Class, GenericMapCache> entryCache; //A Map of "Map of Objects"
    
    
    public StaticDataDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        listCache = new HashMap<>();
        entryCache = new HashMap<>();
    }
    
    private <T> T updateListData(Class<T> type, Date now) {
        T data = null;
        try {
            if (type.equals(ChampionList.class)) {
                data = (T) rApi.getDataChampionList(platform);
            } else if (type.equals(ItemList.class)) {
                data = (T) rApi.getDataItemList(platform);
            } else if (type.equals(MapData.class)) {
                data = (T) rApi.getDataMaps(platform);
            } else if (type.equals(MasteryList.class)) {
                data = (T) rApi.getDataMasteryList(platform);
            } else if (type.equals(ProfileIconData.class)) {
                data = (T) rApi.getDataProfileIcons(platform);
            } else if (type.equals(Realm.class)) {
                data = (T) rApi.getDataRealm(platform);
            } else if (type.equals(RuneList.class)) {
                data = (T) rApi.getDataRuneList(platform);
            } else if (type.equals(SummonerSpellList.class)) {
                data = (T) rApi.getDataSummonerSpellList(platform);
            } else if (type.equals(VersionData.class)) {
                data = (T) new VersionData(rApi.getDataVersions(platform));
            }
        } catch (RiotApiException ex) {
            System.out.println(ex);
            listCache.remove(type);
        }
        if (data != null) {
            listCache.put(type, new GenericObjectCache(data, now));
        }
        return data;
    }
    
    public <T> T getDataList(Class<T> type) {
        Date now = new Date();
        
        GenericObjectCache cache = listCache.get(type);
        if (cache == null) {
            return updateListData(type, now);
        }
        if (cache.isValid(now)) {
            return (T) cache.getObject();
        } else {
            return updateListData(type, now);
        }
        
    }
    
    //Searchers (searches in cache), usually returns partial data
    public Champion getDataChampion(int id) {
        ChampionList list = getDataList(ChampionList.class);
        for (Map.Entry<String, Champion> entry : list.getData().entrySet()) {
            if (entry.getValue().getId() == id) {
                return entry.getValue();
            }
        }
        return null;
    }
    public Champion getDataChampion(String name) {
        name = name.toLowerCase();
        ChampionList list = getDataList(ChampionList.class);
        for (Map.Entry<String, Champion> championEntry : list.getData().entrySet()) {
            if (championEntry.getValue().getName().toLowerCase().equals(name)) {
                return championEntry.getValue();
            }
        }
        return null;
    }
    public Champion getDataChampionClosest(String name) {
        name = name.toLowerCase();
        Champion champion = null;
        int distanceScore = Integer.MAX_VALUE;
        ChampionList list = getDataList(ChampionList.class);
        for (Map.Entry<String, Champion> championEntry : list.getData().entrySet()) {
            String championName = championEntry.getValue().getKey().toLowerCase();
            int newDistanceScore = Levenshtein.substringDistance(championName, name);
            if (newDistanceScore < distanceScore) {
                distanceScore = newDistanceScore;
                champion = championEntry.getValue();
            }
        }
        return champion;
    }
    public Item getDataItem(int id) {
        ItemList list = getDataItemList();
        for (Map.Entry<String, Item> entry : list.getData().entrySet()) {
            if (entry.getValue().getId() == id) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public String getDataLatestVersion() {
        String version = getDataVersions().get(0);
        if (version == null) {
            return "0";
        }
        return version;
    }
    
    //Uncached complete data
    public Champion getDataChampionFull(int id) { //Uncached
        try {
            return rApi.getDataChampion(platform, id, null, null, ChampData.ALL);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            return null;
        }
    }
    
    
    
}
