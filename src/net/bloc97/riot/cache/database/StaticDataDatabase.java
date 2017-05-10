/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import net.bloc97.riot.cache.cached.GenericMapCache;
import net.bloc97.riot.cache.cached.GenericObjectCache;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.bloc97.helpers.Levenshtein;
import net.bloc97.riot.cache.static_data.dto.LanguageData;
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
import net.rithms.riot.api.endpoints.static_data.constant.MasteryData;
import net.rithms.riot.api.endpoints.static_data.constant.RuneData;
import net.rithms.riot.api.endpoints.static_data.constant.SpellData;
import net.rithms.riot.api.endpoints.static_data.dto.LanguageStrings;
import net.rithms.riot.api.endpoints.static_data.dto.Mastery;
import net.rithms.riot.api.endpoints.static_data.dto.Rune;
import net.rithms.riot.api.endpoints.static_data.dto.SummonerSpell;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class StaticDataDatabase {
    private static final long LIFE = TimeUnit.HOURS.toMillis(12); //Caching Time to live
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
            } else if (type.equals(LanguageStrings.class)) {
                data = (T) rApi.getDataLanguageStrings(platform);
            } else if (type.equals(LanguageData.class)) {
                data = (T) new LanguageData(rApi.getDataLanguages(platform));
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
    public LanguageStrings getDataLanguageStrings() {
        return getDataList(LanguageStrings.class);
    }
    public LanguageData getDataLanguages () {
        return getDataList(LanguageData.class);
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
    
    //List Sorters
    
    //Sort by closest to name, returns the partial data list
    public List<Champion> getDataChampionListByClosest(String name) {
        final String nameL = name.toLowerCase();
        List<Champion> list = new ArrayList<>(getDataChampionList().getData().values());
        
        Collections.sort(list, (Champion o1, Champion o2) -> Levenshtein.substringDistance(Levenshtein.toLowerCase(o1.getKey()), nameL) - Levenshtein.substringDistance(Levenshtein.toLowerCase(o2.getKey()), nameL));
        return list;
    }
    public List<Item> getDataItemListByClosest(String name) {
        final String nameL = name.toLowerCase();
        List<Item> list = new ArrayList<>(getDataItemList().getData().values());
        
        Collections.sort(list, (Item o1, Item o2) -> Levenshtein.substringDistance(Levenshtein.toLowerCase(o1.getName()), nameL) - Levenshtein.substringDistance(Levenshtein.toLowerCase(o2.getName()), nameL));
        return list;
    }
    public List<Mastery> getDataMasteryListByClosest(String name) {
        final String nameL = name.toLowerCase();
        List<Mastery> list = new ArrayList<>(getDataMasteryList().getData().values());
        
        Collections.sort(list, (Mastery o1, Mastery o2) -> Levenshtein.substringDistance(Levenshtein.toLowerCase(o1.getName()), nameL) - Levenshtein.substringDistance(Levenshtein.toLowerCase(o2.getName()), nameL));
        return list;
    }
    public List<Rune> getDataRuneListByClosest(String name) {
        final String nameL = name.toLowerCase();
        List<Rune> list = new ArrayList<>(getDataRuneList().getData().values());
        
        Collections.sort(list, (Rune o1, Rune o2) -> Levenshtein.substringDistance(Levenshtein.toLowerCase(o1.getName()), nameL) - Levenshtein.substringDistance(Levenshtein.toLowerCase(o2.getName()), nameL));
        return list;
    }
    public List<SummonerSpell> getDataSummonerSpellListByClosest(String name) {
        final String nameL = name.toLowerCase();
        List<SummonerSpell> list = new ArrayList<>(getDataSummonerSpellList().getData().values());
        
        Collections.sort(list, (SummonerSpell o1, SummonerSpell o2) -> Levenshtein.substringDistance(Levenshtein.toLowerCase(o1.getName()), nameL) - Levenshtein.substringDistance(Levenshtein.toLowerCase(o2.getName()), nameL));
        return list;
    }
    
    //List Searchers (searches in listCache with partial data), then if possible, use ID to return a complete entry
    public Champion searchDataChampion(String name) {
        name = name.toLowerCase();
        ChampionList list = getDataChampionList();
        for (Map.Entry<String, Champion> entry : list.getData().entrySet()) {
            Champion dto = entry.getValue();
            if (dto.getName()== null) {
                continue;
            }
            if (dto.getName().toLowerCase().equals(name)) {
                return getDataChampion(dto.getId());
            }
        }
        return null;
    }
    public Champion searchDataChampionClosest(String name) {
        name = name.toLowerCase();
        Champion dto = null;
        int distanceScore = Integer.MAX_VALUE;
        ChampionList list = getDataChampionList();
        for (Map.Entry<String, Champion> entry : list.getData().entrySet()) {
            if (entry.getValue().getName() == null) {
                continue;
            }
            String dtoName = entry.getValue().getKey().toLowerCase();
            int newDistanceScore = Levenshtein.substringDistance(dtoName, name);
            if (newDistanceScore < distanceScore) {
                distanceScore = newDistanceScore;
                dto = entry.getValue();
            }
        }
        return getDataChampion(dto.getId());
    }
    public Item searchDataItem(String name) {
        name = name.toLowerCase();
        ItemList list = getDataItemList();
        for (Map.Entry<String, Item> entry : list.getData().entrySet()) {
            Item dto = entry.getValue();
            if (dto.getName() == null) {
                continue;
            }
            if (dto.getName().toLowerCase().equals(name)) {
                return getDataItem(dto.getId());
            }
        }
        return null;
    }
    public Item searchDataItemClosest(String name) {
        name = name.toLowerCase();
        Item dto = null;
        int distanceScore = Integer.MAX_VALUE;
        ItemList list = getDataItemList();
        for (Map.Entry<String, Item> entry : list.getData().entrySet()) {
            if (entry.getValue().getName() == null) {
                continue;
            }
            String dtoName = entry.getValue().getName().toLowerCase();
            int newDistanceScore = Levenshtein.substringDistance(dtoName, name);
            if (newDistanceScore < distanceScore) {
                distanceScore = newDistanceScore;
                dto = entry.getValue();
            }
        }
        return getDataItem(dto.getId());
    }
    public Mastery searchDataMastery(String name) {
        name = name.toLowerCase();
        MasteryList list = getDataMasteryList();
        for (Map.Entry<String, Mastery> entry : list.getData().entrySet()) {
            Mastery dto = entry.getValue();
            if (dto.getName() == null) {
                continue;
            }
            if (dto.getName().toLowerCase().equals(name)) {
                return getDataMastery(dto.getId());
            }
        }
        return null;
    }
    public Mastery searchDataMasteryClosest(String name) {
        name = name.toLowerCase();
        Mastery dto = null;
        int distanceScore = Integer.MAX_VALUE;
        MasteryList list = getDataMasteryList();
        for (Map.Entry<String, Mastery> entry : list.getData().entrySet()) {
            if (entry.getValue().getName() == null) {
                continue;
            }
            String dtoName = entry.getValue().getName().toLowerCase();
            int newDistanceScore = Levenshtein.substringDistance(dtoName, name);
            if (newDistanceScore < distanceScore) {
                distanceScore = newDistanceScore;
                dto = entry.getValue();
            }
        }
        return getDataMastery(dto.getId());
    }
    public Rune searchDataRune(String name) {
        name = name.toLowerCase();
        RuneList list = getDataRuneList();
        for (Map.Entry<String, Rune> entry : list.getData().entrySet()) {
            Rune dto = entry.getValue();
            if (dto.getName() == null) {
                continue;
            }
            if (dto.getName().toLowerCase().equals(name)) {
                return getDataRune(dto.getId());
            }
        }
        return null;
    }
    public Rune searchDataRuneClosest(String name) {
        name = name.toLowerCase();
        Rune dto = null;
        int distanceScore = Integer.MAX_VALUE;
        RuneList list = getDataRuneList();
        for (Map.Entry<String, Rune> entry : list.getData().entrySet()) {
            if (entry.getValue().getName() == null) {
                continue;
            }
            String dtoName = entry.getValue().getName().toLowerCase();
            int newDistanceScore = Levenshtein.substringDistance(dtoName, name);
            if (newDistanceScore < distanceScore) {
                distanceScore = newDistanceScore;
                dto = entry.getValue();
            }
        }
        return getDataRune(dto.getId());
    }
    public SummonerSpell searchDataSummonerSpell(String name) {
        name = name.toLowerCase();
        SummonerSpellList list = getDataSummonerSpellList();
        for (Map.Entry<String, SummonerSpell> entry : list.getData().entrySet()) {
            SummonerSpell dto = entry.getValue();
            if (dto.getName() == null) {
                continue;
            }
            if (dto.getName().toLowerCase().equals(name)) {
                return getDataSummonerSpell(dto.getId());
            }
        }
        return null;
    }
    public SummonerSpell searchDataSummonerSpellClosest(String name) {
        name = name.toLowerCase();
        SummonerSpell dto = null;
        int distanceScore = Integer.MAX_VALUE;
        SummonerSpellList list = getDataSummonerSpellList();
        for (Map.Entry<String, SummonerSpell> entry : list.getData().entrySet()) {
            if (entry.getValue().getName() == null) {
                continue;
            }
            String dtoName = entry.getValue().getName().toLowerCase();
            int newDistanceScore = Levenshtein.substringDistance(dtoName, name);
            if (newDistanceScore < distanceScore) {
                distanceScore = newDistanceScore;
                dto = entry.getValue();
            }
        }
        return getDataSummonerSpell(dto.getId());
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
