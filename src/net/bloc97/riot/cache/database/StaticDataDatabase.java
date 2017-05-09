/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.database;

import net.bloc97.riot.cache.cached.GenericMapCache;
import net.bloc97.riot.cache.cached.GenericObjectCache;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.bloc97.helpers.Levenshtein;
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
import net.rithms.riot.api.endpoints.static_data.constant.ItemData;
import net.rithms.riot.api.endpoints.static_data.constant.Locale;
import net.rithms.riot.api.endpoints.static_data.constant.MasteryData;
import net.rithms.riot.api.endpoints.static_data.constant.RuneData;
import net.rithms.riot.api.endpoints.static_data.constant.SpellData;
import net.rithms.riot.api.endpoints.static_data.dto.Mastery;
import net.rithms.riot.api.endpoints.static_data.dto.Rune;
import net.rithms.riot.api.endpoints.static_data.dto.SummonerSpell;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class StaticDataDatabase {
    private static final long LIFE = TimeUnit.HOURS.toMillis(2); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Class, GenericObjectCache> listCache; //A Map of "List Objects", Maps Class Type to the List Object
    private final Map<Class, GenericMapCache> entryCache; //A Map of "Map of Objects", Maps Class Type to a Map of Objects
    
    
    public StaticDataDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        listCache = new HashMap<>();
        entryCache = new HashMap<>();
    }
    
    //Cache updaters
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
            listCache.put(type, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private <T> T updateEntryData(Class<T> type, long id, Date now) {
        T data = null;
        try {
            if (type.equals(Champion.class)) {
                data = (T) rApi.getDataChampion(platform, (int)id, null, null, ChampData.ALL);
            } else if (type.equals(Item.class)) {
                data = (T) rApi.getDataItem(platform, (int)id, null, null, ItemData.ALL);
            } else if (type.equals(Mastery.class)) {
                data = (T) rApi.getDataMastery(platform, (int)id, null, null, MasteryData.ALL);
            } else if (type.equals(Rune.class)) {
                data = (T) rApi.getDataRune(platform, (int)id, null, null, RuneData.ALL);
            } else if (type.equals(SummonerSpell.class)) {
                data = (T) rApi.getDataSummonerSpell(platform, (int)id, null, null, SpellData.ALL);
            }
        } catch (RiotApiException ex) {
            System.out.println(ex);
            entryCache.get(type).map.remove(id);
        }
        if (data != null) {
            entryCache.get(type).map.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    //Cache getters
    private <T> T getDataEntry(Class<T> type, long id) {
        Date now = new Date();
        
        GenericMapCache<T> mapCache = entryCache.get(type);
        if (mapCache == null) {
            mapCache = new GenericMapCache(type);
            entryCache.put(type, mapCache);
        }
        
        GenericObjectCache<T> cache = mapCache.map.get(id);
        if (cache == null) {
            return updateEntryData(type, id, now);
        }
        if (cache.isValid(now)) {
            return (T) cache.getObject();
        } else {
            return updateEntryData(type, id, now);
        }
        
    }
    private <T> T getDataList(Class<T> type) {
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
    
    //List Getters (They contain partial data)
    public ChampionList getDataChampionList() {
        return getDataList(ChampionList.class);
    }
    public ItemList getDataItemList() {
        return getDataList(ItemList.class);
    }
    public MapData getDataMaps() {
        return getDataList(MapData.class);
    }
    public MasteryList getDataMasteryList() {
        return getDataList(MasteryList.class);
    }
    public ProfileIconData getDataProfileIcons() {
        return getDataList(ProfileIconData.class);
    }
    public Realm getDataRealm() {
        return getDataList(Realm.class);
    }
    public RuneList getDataRuneList() {
        return getDataList(RuneList.class);
    }
    public SummonerSpellList getDataSummonerSpellList() {
        return getDataList(SummonerSpellList.class);
    }
    public VersionData getDataVersions() {
        return getDataList(VersionData.class);
    }
    
    //Entry Getters (They contain full data)
    public Champion getDataChampion(long id) {
        return getDataEntry(Champion.class, id);
    }
    public Item getDataItem(long id) {
        return getDataEntry(Item.class, id);
    }
    public Mastery getDataMastery(long id) {
        return getDataEntry(Mastery.class, id);
    }
    public Rune getDataRune(long id) {
        return getDataEntry(Rune.class, id);
    }
    public SummonerSpell getDataSummonerSpell(long id) {
        return getDataEntry(SummonerSpell.class, id);
    }
    
    //Extra functions
    
    //List Searchers (searches in listCache with partial data), then if possible, use ID to return a complete entry
    public Champion searchDataChampion(String name) {
        name = name.toLowerCase();
        ChampionList list = getDataList(ChampionList.class);
        for (Map.Entry<String, Champion> championEntry : list.getData().entrySet()) {
            Champion champion = championEntry.getValue();
            if (champion.getName().toLowerCase().equals(name)) {
                return getDataChampion(champion.getId());
            }
        }
        return null;
    }
    public Champion searchDataChampionClosest(String name) {
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
        return getDataChampion(champion.getId());
    }
    
    
    public String getDataLatestVersion() {
        VersionData versions = getDataList(VersionData.class);
        String version = versions.getLatestVersion();
        if (version == null) {
            return "0";
        }
        return version;
    }
    
    
    
    
}
