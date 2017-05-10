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
import net.rithms.riot.api.endpoints.lol_status.dto.ShardStatus;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class LolStatusDatabase implements CachedDatabase {
    private static final long LIFE = TimeUnit.MINUTES.toMillis(1); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private GenericObjectCache<ShardStatus> statusCache;
    
    public LolStatusDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
    }
    
    //Updaters, calls RiotApi for cache updates
    private ShardStatus updateShardData(Date now) {
        ShardStatus data = null;
        try {
            data = rApi.getShardData(platform);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            statusCache = null;
        }
        if (data != null) {
            statusCache = new GenericObjectCache(data, now, LIFE);
        }
        return data;
    }
    
    public ShardStatus getShardData() {
        Date now = new Date();
        
        GenericObjectCache<ShardStatus> cache = statusCache;
        if (cache == null) {
            return updateShardData(now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateShardData(now);
        }
    }

    @Override
    public void purge() {
        statusCache = null;
    }
    
    
}
