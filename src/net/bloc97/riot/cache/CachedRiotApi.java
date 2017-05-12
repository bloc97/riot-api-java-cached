/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bloc97.riot.cache.database.ChampionDatabase;
import net.bloc97.riot.cache.database.ChampionMasteryDatabase;
import net.bloc97.riot.cache.database.LeagueDatabase;
import net.bloc97.riot.cache.database.LolStatusDatabase;
import net.bloc97.riot.cache.database.MasteriesDatabase;
import net.bloc97.riot.cache.database.MatchDatabase;
import net.bloc97.riot.cache.database.RunesDatabase;
import net.bloc97.riot.cache.database.SpectatorDatabase;
import net.bloc97.riot.cache.database.SummonerDatabase;
import net.bloc97.riot.cache.database.StaticDataDatabase;
import net.bloc97.riot.cache.database.StatsDatabase;
import net.bloc97.riot.direct.database.MatchUncachedDatabase;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.request.ratelimit.RateLimitException;
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
    
    @Deprecated
    public final StatsDatabase Stats;
    
    public final ChampionMasteryDatabase ChampionMastery;
    public final ChampionDatabase Champion;
    public final LeagueDatabase League;
    public final LolStatusDatabase LolStatus;
    public final MasteriesDatabase Masteries;
    public final MatchDatabase Match;
    public final RunesDatabase Runes;
    public final SpectatorDatabase Spectator;
    public final StaticDataDatabase StaticData;
    public final SummonerDatabase Summoner;
    
    public final Uncached Uncached;
    
    public CachedRiotApi(String apiKey, Platform platform) {
        
        ApiConfig config = new ApiConfig();
        config.setKey(apiKey);
        config.setRespectRateLimit(true);
        config.setRequestTimeout(10000);
        
        this.rApi = new RiotApi(config);
        this.platform = platform;
        
        this.Stats = new StatsDatabase(platform, rApi);
        
        this.Uncached = new Uncached(platform, rApi);
        
        this.StaticData = new StaticDataDatabase(platform, rApi);
        this.Summoner = new SummonerDatabase(platform, rApi);
        this.ChampionMastery = new ChampionMasteryDatabase(platform, rApi);
        this.Champion = new ChampionDatabase(platform, rApi);
        this.League = new LeagueDatabase(platform, rApi);
        this.LolStatus = new LolStatusDatabase(platform, rApi);
        this.Masteries = new MasteriesDatabase(platform, rApi);
        this.Match = new MatchDatabase(platform, rApi);
        this.Runes = new RunesDatabase(platform, rApi);
        this.Spectator = new SpectatorDatabase(platform, rApi);
    }
    
    public static boolean isRateLimited(RiotApiException ex) {
        if (ex instanceof RateLimitException) {
            try {
                System.out.println("Rate Limited, Retrying in: " + ((RateLimitException) ex).getRetryAfter());
                Thread.sleep(((RateLimitException) ex).getRetryAfter()*1000+1000);
                return true;
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        if (ex.getMessage().equalsIgnoreCase("Forbidden")) {
            try {
                System.out.println("Blacklisted, sleeping forever.");
                Thread.sleep(Long.MAX_VALUE);
                return false;
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        return false;
    }
    
    public final void purgeAll() {
        Stats.purge();
        
        ChampionMastery.purge();
        Champion.purge();
        League.purge();
        LolStatus.purge();
        Masteries.purge();
        Match.purge();
        Runes.purge();
        Spectator.purge();
        StaticData.purge();
        Summoner.purge();
    }
    
}
