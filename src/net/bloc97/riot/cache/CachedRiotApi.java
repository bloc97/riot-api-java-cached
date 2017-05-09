/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache;

import net.bloc97.riot.cache.database.ChampionDatabase;
import net.bloc97.riot.cache.database.ChampionMasteryDatabase;
import net.bloc97.riot.cache.database.LeagueDatabase;
import net.bloc97.riot.cache.database.LolStatusDatabase;
import net.bloc97.riot.cache.database.MasteriesDatabase;
import net.bloc97.riot.cache.database.MatchDatabase;
import net.bloc97.riot.cache.database.SummonerDatabase;
import net.bloc97.riot.cache.database.StaticDataDatabase;
import net.bloc97.riot.direct.database.MatchUncachedDatabase;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */

public class CachedRiotApi {
    
    private class Uncached {
        public final MatchUncachedDatabase Match;
        
        private Uncached(Platform platform, RiotApi rApi) {
            this.Match = new MatchUncachedDatabase(platform, rApi);
        }
    }
    private final RiotApi rApi;
    private final Platform platform;
    
    
    public final ChampionMasteryDatabase ChampionMastery;
    public final ChampionDatabase Champion;
    public final LeagueDatabase League;
    public final LolStatusDatabase LolStatus;
    public final MasteriesDatabase Masteries;
    public final MatchDatabase Match;
    
    public final StaticDataDatabase StaticData;
    public final SummonerDatabase Summoner;
    
    public final Uncached Uncached;
    
    public CachedRiotApi(String apiKey, Platform platform) {
        
        ApiConfig config = new ApiConfig();
        config.setKey(apiKey);
        config.setRespectRateLimit(true);
        config.setRequestTimeout(4000);
        
        this.rApi = new RiotApi(config);
        this.platform = platform;
        
        this.Uncached = new Uncached(platform, rApi);
        
        this.StaticData = new StaticDataDatabase(platform, rApi);
        this.Summoner = new SummonerDatabase(platform, rApi);
        this.ChampionMastery = new ChampionMasteryDatabase(platform, rApi);
        this.Champion = new ChampionDatabase(platform, rApi);
        this.League = new LeagueDatabase(platform, rApi);
        this.LolStatus = new LolStatusDatabase(platform, rApi);
        this.Masteries = new MasteriesDatabase(platform, rApi);
        this.Match = new MatchDatabase(platform, rApi);
        
    }
    
}
