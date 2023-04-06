# A card game

Based on a Latvian card game "cūkas" or "duraks".

This is a test project written in Kotlin with KorGE for school purposes.
It is not optimized for performance or anything else.
KorGE has a lot of features that are not used in this project.
Only a subset of features is used and in a very simple/inefficient way.

Game state graph is generated on the fly and is not stored anywhere,
but it could be optimized to use less memory.

## Build
#### How to run
- On PC (JVM): `./gradlew runJvm`
- As a web app (JS): `./gradlew runJs`
#### How to build
- On PC (JVM): `./gradlew packageJvmFatJar`
- As a web app (JS): `./gradlew jsBrowserDistribution`

## How to play
Here are the basic rules of the game:

The first player is determined by a coin toss or other method of random selection.

The player who goes first begins by laying down any card from their hand face up on the table.

The second player must then play a card with the same suit that is higher in rank than the card played by the first player. For example, if the first player plays a 6, the second player must play a card that is 7 or higher (of the same suit). If the second player does not have a higher card, they must pick up all the cards that have been played so far and add them to their hand and miss their turn to attack.

After the second player has played a card, the roles are reversed and the first player must play a card that is higher in rank than the card played by the second player. If they cannot, they must pick up the cards and add them to their hand.

If a player manages to get rid of all of their cards, they win the game.

If the deck runs out before either player can get rid of all of their cards, the game ends in a draw.

The game also has high cards which are determined by the suit of the high card that is face up under the deck of cards (the last card in the deck).
With high cards you can always defeat any other card except the ones with the same suit as the high card, for which the same rules apply as with regular cards.

# Demo
- [As a webapp](https://cardgame.kristianskaneps.lv/)

# Attribution
- [KorGE](https://korge.org/)
- [Playing cards by Byron Knoll](http://code.google.com/p/vector-playing-cards/)
