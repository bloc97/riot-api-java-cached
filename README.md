# [RIOT-API-JAVA-CACHED](https://github.com/bloc97/riot-api-java-cached)
----------
A extremely high-level, simple to use, Cached Riot Games API wrapper for Java.
This API wraps [RIOT-API-JAVA](https://github.com/taycaldwell/riot-api-java), and uses internal caching to migitate unnecessary API calls.

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
- **CHAMPION-MASTERY-V3**
- **CHAMPION-V3**
- **LEAGUE-V3**
- **LOL-STATUS-V3**
- **MASTERIES-V3**
- **MATCH-V3** (Partial Caching)\*
- **RUNES-V3**
- **SPECTATOR-V3**
- **STATIC-DATA-V3** \*\*
- **SUMMONER-V3**

*The two Tournament methods in MATCH-V3 are not cached, but are still included for completeness.  
\*\*The language methods are not included in this API, please use riot-api-java methods instead.

Note: the TOURNAMENT APIs will probably never be cached, as it is unadvised to cache data that is both written and read from multiple clients without controlling the server.  
However, you would still be able to access them through uncached functions.

## Contributing
All contributions are appreciated.
If you would like to contribute to this project, please send a pull request.

## Contact
Have a suggestion, complaint, or question? Open an [issue](https://github.com/riot-api-java-cached/riot-api-java/issues).
