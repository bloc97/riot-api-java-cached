/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bloc97.riot.cache.database;

import net.bloc97.riot.cache.cached.GenericObjectCache;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.bloc97.riot.cache.CachedRiotApi;
import static net.bloc97.riot.cache.CachedRiotApi.isRateLimited;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.masteries.dto.MasteryPage;
import net.rithms.riot.api.endpoints.masteries.dto.MasteryPages;
import net.rithms.riot.api.endpoints.match.dto.Mastery;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.match.dto.MatchTimeline;
import net.rithms.riot.api.endpoints.match.dto.Participant;
import net.rithms.riot.api.endpoints.match.dto.ParticipantIdentity;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.api.request.ratelimit.RateLimitException;
import net.rithms.riot.constant.Platform;

/**
 *
 * @author bowen
 */
public class MatchDatabase implements CachedDatabase {
    private static final long LIFE = TimeUnit.MINUTES.toMillis(20); //Caching Time to live
    public final int version = 3;
    
    private final RiotApi rApi;
    private final Platform platform;
    
    private final Map<Long, GenericObjectCache<Match>> matchesCache; //Maps match ID to Match
    private final Map<Long, GenericObjectCache<MatchTimeline>> timelinesCache; //Maps match ID to Match Timeline
    private final Map<Long, GenericObjectCache<MatchList>> matchListsRankedCache; //Maps account ID to MatchList
    private final Map<Long, GenericObjectCache<MatchList>> matchListsRecentCache; //Maps account ID to Recent MatchList
    
    public MatchDatabase(Platform platform, RiotApi rApi) {
        this.rApi = rApi;
        this.platform = platform;
        
        matchesCache = new HashMap<>();
        timelinesCache = new HashMap<>();
        matchListsRankedCache = new HashMap<>();
        matchListsRecentCache = new HashMap<>();
    }
    
    //Updaters, calls RiotApi for cache updates
    private Match updateMatch(long id, Date now) {
        
        Match data = null;
        try {
            data = rApi.getMatch(platform, id);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateMatch(id, now);
            }
            System.out.println(ex);
            matchesCache.remove(id);
        }
        if (data != null) {
            matchesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private MatchList updateRankedMatchListByAccountId(long id, Date now) {
        
        MatchList data = null;
        try {
            data = rApi.getMatchListByAccountId(platform, id);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateRankedMatchListByAccountId(id, now);
            }
            System.out.println(ex);
            matchListsRankedCache.remove(id);
        }
        if (data != null) {
            matchListsRankedCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private MatchList updateRecentMatchListByAccountId(long id, Date now) {
        
        MatchList data = null;
        try {
            data = rApi.getRecentMatchListByAccountId(platform, id);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateRecentMatchListByAccountId(id, now);
            }
            System.out.println(ex);
            matchListsRecentCache.remove(id);
        }
        if (data != null) {
            matchListsRecentCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    private MatchTimeline updateMatchTimeline(long id, Date now) {
        
        MatchTimeline data = null;
        try {
            data = rApi.getTimelineByMatchId(platform, id);
        } catch (RiotApiException ex) {
            if (isRateLimited(ex)) {
                return updateMatchTimeline(id, now);
            }
            System.out.println(ex);
            timelinesCache.remove(id);
        }
        if (data != null) {
            timelinesCache.put(id, new GenericObjectCache(data, now, LIFE));
        }
        return data;
    }
    
    
    public Match getMatch(long id) {
        Date now = new Date();
        
        GenericObjectCache<Match> cache = matchesCache.get(id);
        if (cache == null) {
            return updateMatch(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateMatch(id, now);
        }
    }
    public MatchList getRankedMatchListByAccountId(long id) {
        Date now = new Date();
        
        GenericObjectCache<MatchList> cache = matchListsRankedCache.get(id);
        if (cache == null) {
            return updateRankedMatchListByAccountId(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateRankedMatchListByAccountId(id, now);
        }
    }
    public MatchList getRecentMatchListByAccountId(long id) {
        Date now = new Date();
        
        GenericObjectCache<MatchList> cache = matchListsRecentCache.get(id);
        if (cache == null) {
            return updateRecentMatchListByAccountId(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateRecentMatchListByAccountId(id, now);
        }
    }
    public MatchTimeline getMatchTimeline(long id) {
        Date now = new Date();
        
        GenericObjectCache<MatchTimeline> cache = timelinesCache.get(id);
        if (cache == null) {
            return updateMatchTimeline(id, now);
        }
        if (cache.isValid(now)) {
            return cache.getObject();
        } else {
            return updateMatchTimeline(id, now);
        }
    }
    
    //Extra functions
    public List<Match> getMatches(MatchList matchList) {
        LinkedList<Match> fullList = new LinkedList<>();
        
        for (MatchReference mr : matchList.getMatches()) {
            Match m = getMatch(mr.getGameId());
            if (m != null) {
                fullList.add(m);
            }
        }
        return fullList;
    }
    
    public Participant tryGetParticipant(MatchReference match, Match fullMatch, long summonerId, boolean doTryGuess, CachedRiotApi api) {
        Participant unknownParticipant = fullMatch.getParticipantBySummonerId(summonerId);
        if (unknownParticipant != null) {
            if (unknownParticipant.getChampionId() == match.getChampion()) { //Found participant, just to make sure
                return unknownParticipant;
            }
        }
        //Use various data to try and find participan id when they are hidden
        int championId = match.getChampion();
        String lane = match.getLane();
        String role = match.getRole();
        
        List<Participant> participants = fullMatch.getParticipants();
        
        return filterParticipantLevel1(participants, championId, lane, role, summonerId, doTryGuess, api);
    }
    
    private Participant filterParticipantLevel1(List<Participant> participants, int championId, String lane, String role, long summonerId, boolean doTryGuess, CachedRiotApi api) {
        List<Participant> foundParticipants = new LinkedList<>();
        for (Participant participant : participants) {
            int thisChampionId = participant.getChampionId();
            if (thisChampionId == championId) {
                foundParticipants.add(participant);
            }
        }
        if (foundParticipants.size() == 1) { //If after small search only one remains
            return foundParticipants.get(0);
        } else if (foundParticipants.size() > 1) {
            return filterParticipantLevel2(foundParticipants, championId, lane, role, summonerId, doTryGuess, api);
        } else {
            return null;
        }
    }
    private Participant filterParticipantLevel2(List<Participant> participants, int championId, String lane, String role, long summonerId, boolean doTryGuess, CachedRiotApi api) {
        List<Participant> foundParticipants = new LinkedList<>();
        for (Participant participant : participants) { //Bigger search
            int thisChampionId = participant.getChampionId();
            String thisRole = participant.getTimeline().getRole();
            if (thisChampionId == championId && thisRole.equals(role)) {
                foundParticipants.add(participant);
            }
        }
        if (foundParticipants.size() == 1) { //If after small search only one remains
            return foundParticipants.get(0);
        } else if (foundParticipants.size() > 1) {
            return filterParticipantLevel3(foundParticipants, championId, lane, role, summonerId, doTryGuess, api);
        } else {
            return null;
        }
    }
    private Participant filterParticipantLevel3(List<Participant> participants, int championId, String lane, String role, long summonerId, boolean doTryGuess, CachedRiotApi api) {
        List<Participant> foundParticipants = new LinkedList<>();
        for (Participant participant : participants) { //Even Bigger search
            int thisChampionId = participant.getChampionId();
            String thisLane = participant.getTimeline().getLane();
            String thisRole = participant.getTimeline().getRole();
            if (thisChampionId == championId && isLanesEqual(lane, thisLane) && thisRole.equals(role)) {
                foundParticipants.add(participant);
            }
        }
        if (foundParticipants.size() == 1) { //If after small search only one remains
            return foundParticipants.get(0);
        } else if (foundParticipants.size() > 1) {
            if (doTryGuess) {
                return deepParticipantSearch(participants, summonerId, api);
            }
            return null;
        } else {
            return null;
        }
    }
    
    
    private Participant deepParticipantSearch(List<Participant> participants, long summonerId, CachedRiotApi api) {
        
        List<Participant> newParticipants = new LinkedList<>();
        MasteryPages masteryPages = api.Masteries.getMasteriesBySummoner(summonerId);
        
        for (Participant participant : participants) {
            List<Mastery> thisMasteryList = participant.getMasteries();
            for (MasteryPage masteryPage : masteryPages.getPages()) {
                if (isMasteryPageEqualToMasteryList(masteryPage, thisMasteryList)) {
                    newParticipants.add(participant);
                }
            }
        }
        if (newParticipants.size() == 1) {
            return newParticipants.get(0);
        } else {
            System.out.println(newParticipants.size());
            return null;
        }
        
    }
    public boolean isMasteryPageEqualToMasteryList(MasteryPage page, List<Mastery> list) {
        List<ComparableMastery> list1 = convertMasteryList(page.getMasteries());
        List<ComparableMastery> list2 = convertMatchMasteryList(list);
        return list1.containsAll(list2) && list2.containsAll(list1);
    }
    
    
    private List<ComparableMastery> convertMasteryList(List<net.rithms.riot.api.endpoints.masteries.dto.Mastery> masteryList) {
        LinkedList<ComparableMastery> list = new LinkedList<>();
        for (net.rithms.riot.api.endpoints.masteries.dto.Mastery mastery : masteryList) {
            list.add(new ComparableMastery(mastery));
        }
        return list;
    }
    private List<ComparableMastery> convertMatchMasteryList(List<net.rithms.riot.api.endpoints.match.dto.Mastery> masteryList) {
        LinkedList<ComparableMastery> list = new LinkedList<>();
        for (net.rithms.riot.api.endpoints.match.dto.Mastery mastery : masteryList) {
            list.add(new ComparableMastery(mastery));
        }
        return list;
    }
    
    private class ComparableMastery {
	private int masteryId;
	private int rank;

        private ComparableMastery(Mastery mastery) {
            masteryId = mastery.getMasteryId();
            rank = mastery.getRank();
        }
        private ComparableMastery(net.rithms.riot.api.endpoints.masteries.dto.Mastery mastery) {
            masteryId = mastery.getId();
            rank = mastery.getRank();
        }
        
	public int getMasteryId() {
		return masteryId;
	}
	
	public int getRank() {
		return rank;
	}
        @Override
        public boolean equals(Object o) {
            if (o instanceof ComparableMastery) {
                ComparableMastery m = (ComparableMastery) o;
                if (m.rank == rank && m.masteryId == masteryId) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private boolean isLanesEqual(String lane1, String lane2) {
        if (lane1.equals(lane2)) {
            return true;
        }
        if (lane1.equals("MID")) {
            lane1 = "MIDDLE";
        } else if (lane1.equals("BOT")) {
            lane1 = "BOTTOM";
        }
        if (lane2.equals("MID")) {
            lane2 = "MIDDLE";
        } else if (lane2.equals("BOT")) {
            lane2 = "BOTTOM";
        }
        if (lane1.equals(lane2)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void purge() {
        matchListsRankedCache.clear();
        matchListsRecentCache.clear();
        matchesCache.clear();
        timelinesCache.clear();
    }
    
}
