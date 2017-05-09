/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache;

import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class CachedRiotApi {
    
    private final RiotApi rApi;
    private final Platform platform;
    
    
    public final StaticDataDatabase StaticData;
    public final SummonerDatabase Summoner;
    
    public CachedRiotApi(String apiKey, Platform platform) {
        
        ApiConfig config = new ApiConfig();
        config.setKey(apiKey);
        config.setRespectRateLimit(true);
        config.setRequestTimeout(4000);
        
        this.rApi = new RiotApi(config);
        this.platform = platform;
        this.StaticData = new StaticDataDatabase(platform, rApi);
        this.Summoner = new SummonerDatabase(platform, rApi);
        
    }
    
}
