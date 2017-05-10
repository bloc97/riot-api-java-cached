# [RIOT-API-JAVA-CACHED](https://github.com/bloc97/riot-api-java-cached)
----------
A extremely high-level, simple to use, Cached Riot Games API wrapper for Java.
This API wraps [RIOT-API-JAVA](https://github.com/taycaldwell/riot-api-java), and uses internal caching to migitate unnecessary API calls.

## Features
- Caching  
 Allows you to fetch similar data without spending your API Rate.
- Wait on rate limit  
 Allows your method to complete after waiting for the rate limit to end, no more null objects caused by rate limiting.
- Silent fail  
 Does not end your program, even if there is an error. The methods simply return null.
- High level API  
 Allows you to call methods unavailable on the vanilla Riot API, such as searching the database by name, sorting champion masteries, etc.
## Disclaimer
This product is not endorsed, certified or otherwise approved in any way by Riot Games, Inc. or any of its affiliates.

## Requirements

**riot-api-java-cached** requires Java 8 and the following libraries with their respective requirements:
- [riot-api-java](https://github.com/taycaldwell/riot-api-java)

## Usage

This library can be used strictly according to the [Riot API Documentation](https://developer.riotgames.com/api/methods) 

## Documentation
There is not yet an documentation, function names are what they describe.
The documentation for riot-api-java can be found [here.](http://taycaldwell.com/riot-api-java/doc/)

## API Versions
The current version of this library supports, and only supports the following Riot Games API versions:
**API** | *(Caching Life)*
- **CHAMPION-MASTERY-V3** (20 Minutes)
- **CHAMPION-V3** (12 Hours)
- **LEAGUE-V3** (1 Hour)
- **LOL-STATUS-V3** (1 Minute)
- **MASTERIES-V3** (20 Minutes)
- **MATCH-V3** (20 Minutes) (Partial Caching)\*
- **RUNES-V3** (20 MInutes)
- **SPECTATOR-V3** (30 Seconds)
- **STATIC-DATA-V3** (12 Hours) \*\*
- **SUMMONER-V3** (20 Minutes)

*The two Tournament methods in MATCH-V3 are not cached, but are still included for completeness.  
\*\*The language method is not fully supported, you can only use them for the default locale. For other locales, please use riot-api-java methods instead.

Note: the TOURNAMENT APIs will probably never be cached, as it is unadvised to cache data that is both written and read from multiple clients without controlling the server.  
However, you would still be able to access them through uncached functions.

## Contributing
All contributions are appreciated.
If you would like to contribute to this project, please send a pull request.

## Contact
Have a suggestion, complaint, or question? Open an [issue](https://github.com/riot-api-java-cached/riot-api-java/issues).
