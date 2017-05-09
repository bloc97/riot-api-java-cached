/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.direct.database;

import java.util.List;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class MatchUncachedDatabase {
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    public MatchUncachedDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
    }
    
    //Direct getters, uncached!!!
    public List<Long> getMatchIdsByTournamentCode(String code) {
        try {
            return rApi.getMatchIdsByTournamentCode(platform, code);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            return null;
        }
    }
    public Match getMatchByTournamentCode(long matchId, String code) {
        try {
            return rApi.getMatchByMatchIdAndTournamentCode(platform, matchId, code);
        } catch (RiotApiException ex) {
            System.out.println(ex);
            return null;
        }
    }
    
    
    
}
