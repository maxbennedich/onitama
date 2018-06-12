# Onitama

AI for the board game [Onitama](http://www.arcanewonders.com/game/onitama/). Read the
[rules of the game here](http://www.arcanewonders.com/resources/Onitama_Rulebook.PDF).

The AI uses a negamax search with alpha-beta pruning and several other techniques to limit the
search space.

<p align="center"><img src="http://www.arcanewonders.com/wp-content/uploads/2017/06/onitama-1.png"></p>

## Features implemented
- [Negamax](https://chessprogramming.wikispaces.com/Negamax) with [alpha-beta pruning](https://chessprogramming.wikispaces.com/Alpha-Beta)
- [Iterative deepening](https://chessprogramming.wikispaces.com/Iterative+Deepening)
- [Principal variation search](https://chessprogramming.wikispaces.com/Principal%20Variation%20Search)
- [Quiescence search](https://chessprogramming.wikispaces.com/Quiescence%20Search)
- Dynamically sized, two-tier, [transposition table](https://chessprogramming.wikispaces.com/Transposition+Table)
- [Zobrist hashing](https://chessprogramming.wikispaces.com/Zobrist+Hashing)
- [Move ordering](https://chessprogramming.wikispaces.com/Move+Ordering): best move, winning moves, capture moves, [history heuristics](https://chessprogramming.wikispaces.com/History%20Heuristic)
- [Evaluation function](https://chessprogramming.wikispaces.com/Evaluation): piece count, weighted piece positions
- [Bitboards](https://chessprogramming.wikispaces.com/Bitboards), for move generation and validation
- [Pondering](https://chessprogramming.wikispaces.com/Pondering), with one thread per opponent move

## Features tried
- [Endgame table](https://chessprogramming.wikispaces.com/Endgame+Tablebases): Problematic since there are 131,040 combinations of 5 cards to play the game with
- [Parallelization](https://chessprogramming.wikispaces.com/Parallel+Search) through multiple search threads with a shared TT: Scaled very badly
- [Aspiration windows](https://chessprogramming.wikispaces.com/Aspiration+Windows): Did not improve search
- Storing the quiescence search results in the TT: Slowed down search
- Check evasion during quiescence search: More nodes searched, without finding a win faster
- Two best moves: Did not reduce states visited

## Performance

Below are some average statistics for various nominal search depths.
For each depth, 25 searches were done from the initial board position,
with different initial cards for each search (but the same set of cards for each depth tested).
The transposition table fill rate was kept at < 75 %, so to not have any greater impact on the
search times.
The search is single-threaded, and a 2.6 GHz laptop was used for these tests.

|Depth|Max depth|Time|States|Quiescent|Branching|States / s|
|:---:|:-------:|:--:|:----:|:-------:|:----:|:--------:|
| 8 | 14.8 | 23 ms | 95.5 K | 21.2 % | 2.79 | 4.12 M |
| 10 | 18.4 | 177 ms | 869 K | 22.2 % | 2.76 | 4.91 M |
| 12 | 20.8 | 1.41 s | 6.87 M | 20.9 % | 2.76 | 4.88 M |
| 14 | 23.0 | 9.60 s | 45.0 M | 18.7 % | 2.68 | 4.67 M |
| 16 | 25.1 | 49.8 s | 213 M | 16.5 % | 2.55 | 4.27 M |
| 18 | 27.1 | 195 s | 802 M | 14.6 % | 2.53 | 4.11 M |
| 20 | 29.6 | 751 s | 2.90 G | 13.2 % | 2.54 | 3.87 M |

_**Depth:** Nominal depth searched_  
_**Max depth:** Maximum depth analyzed including quiescence search_  
_**Time:** Elapsed time to complete search_  
_**States:** Total states (moves) evaluated during both nominal and quiescence search_  
_**Quiescent:** Percent of states evaluated that were part of the quiescence search_  
_**Branching:** Average branching factor, i.e. number of moves tried per state_  
_**States / s:** Average number of states evaluated per second_  

Thanks to its two-tier property, the transposition table performs quite well even when
filling up. The following table shows the average number of states evaluated, and time
elapsed, for a search to nominal depth 14, using different sizes for the TT.
Note that the table is used for storing both score/node information and best moves
(for move ordering). Each entry in the TT is 12 bytes.

|TT size|Fill rate|Time|States|
|:-----:|:-------:|:--:|:----:|
| none | N/A | 85.9 s | 464 M |
| 768 b | 100 % | 63.3 s | 341 M |
| 3 KB | 100 % | 53.2 s | 285 M |
| 24 KB | 100 % | 40.9 s | 205 M |
| 192 KB | 100 % | 24.7 s | 127 M |
| 768 KB | 100 % | 18.7 s | 96.5 M |
| 3 MB | 100 % | 14.2 s | 70.8 M |
| 6 MB |  99.99 % | 13.1 s | 61.7 M |
| 12 MB | 99.6 % | 12.0 s | 54.9 M |
| 24 MB | 95.8 % | 11.4 s | 50.8 M |
| 48 MB | 84.8 % | 10.1 s | 46.2 M |
| 192 MB | 45.0 % | 9.99 s | 45.6 M |
| 768 MB | 14.2 % | 9.60 s | 45.0 M |

## Example output

Below is some output from a game where the AI, after a single move from a human player, was able
to guarantee a win in 19 plies (10 moves), after 44 seconds of computation on a
2.6 GHz laptop. Depth "18/24" means that the game was searched in full to a depth of 18 plies,
and that the maximum depth analyzed (including quiescence search) was 24 plies. Looking more closely
at the principal variation (list of best moves), we see that "Tiger c3-c1" puts the AI in the winning
square and occurs at ply 19.
A score of ~100 means one piece advantage. 500 means victory. Smaller difference in scores means
better positioning on the board. The output format is inspired by
[Crafty](http://www.craftychess.com/documentation/craftydoc.html).

```
Player 1 cards: Crane Rooster
Player 2 cards: Tiger Ox
Extra card: Horse

Enter player move 1: Rooster c1d2

  +---+---+---+---+---+
5 | b | b | B | b | b |
  +---+---+---+---+---+
4 |   |   |   |   |   |
  +---+---+---+---+---+
3 |   |   |   |   |   |
  +---+---+---+---+---+
2 |   |   |   | R |   |
  +---+---+---+---+---+
1 | r | r |   | r | r |
  +---+---+---+---+---+
    a   b   c   d   e

Player 1 cards: Crane Horse
Player 2 cards: Tiger Ox
Extra card: Rooster

 depth    time  score  best moves
 1/ 2->   0.00     2   Tiger a5-a3
 2/ 3->   0.00     1   Tiger a5-a3 | Crane d2-d3
 3/ 6->   0.00     2   Ox d5-d4 | Crane d2-d3 | Tiger a5-a3
 4/ 7->   0.00     3   Ox d5-d4 | Crane d2-d3 | Tiger a5-a3 | Ox d3-e3
 5/ 8->   0.00     4   Ox d5-d4 | Crane d2-d3 | Tiger b5-b3 | Ox d3-e3 | Crane e5-e4
 6/10->   0.00     2   Ox d5-d4 | Crane d2-d3 | Tiger b5-b3 | Ox d3-d2 | Rooster b3-c3 | Tiger a1-a3
 7/12->   0.01     4   Ox c5-c4 | Crane d2-c1 | Rooster e5-d4 | Horse b1-b2 | Crane b5-b4 | Ox a1-a2 | Tiger d5-d3
 8/14->   0.03     3   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse c5-c4 | Crane a1-a2 | Ox b5-b4 | Horse b1-b2
 9/14->   0.05     5   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Rooster a1-b2 | Ox b5-b4 | Crane b2-c1 | Tiger a5-a3
10/18->   0.19     6   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane a1-a2 | Ox e4-d4 | Rooster d2-e2 | Tiger b5-b3 | Horse d1-c1
11/18->   0.32     6   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane a1-a2 | Ox e4-d4 | Rooster d2-e2 | Tiger b5-b3 | Horse d1-c1 | Crane b3-c4
12/19->   1.05   105   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-a2 | Horse a5-a4 | Rooster a2-b3
13/18     1.20   105   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-a2 | Horse a5-a4 | Rooster a2-b3 | Ox c3-b3
13/18->   1.84   105   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-a2 | Horse a5-a4 | Rooster a2-b3 | Ox c3-b3
14/21     2.59   105   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-a2 | Horse a5-a4 | Rooster a2-b3 | Ox e4-d4 | Horse d1-c1
14/21->   5.50   105   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-a2 | Horse a5-a4 | Rooster a2-b3 | Ox e4-d4 | Horse d1-c1
15/20     6.38   107   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-a2 | Crane b3-b2 | Tiger d1-d3 | Ox b2-a2 | Rooster d3-e4 | Tiger a5-a3
15/21->   9.48   107   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-a2 | Crane b3-b2 | Tiger d1-d3 | Ox b2-a2 | Rooster d3-e4 | Tiger a5-a3
16/23     14 s   107   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-b1 | Crane a5-a4 | Rooster d2-c1 | Ox e4-d4 | Crane d1-d2 | Horse a4-a3 | Tiger e1-e3
16/23->   27 s   107   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-b1 | Crane a5-a4 | Rooster d2-c1 | Ox e4-d4 | Crane d1-d2 | Horse a4-a3 | Tiger e1-e3
17/23     31 s   109   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-b1 | Crane b3-b2 | Rooster d2-e2 | Ox b2-b1 | Tiger e2-e4 | Rooster b1-c2 | Ox e1-e2 | Tiger a5-a3
17/23->   44 s   109   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox c5-c4 | Horse b2-b3 | Tiger b5-b3 | Ox a1-b1 | Crane b3-b2 | Rooster d2-e2 | Ox b2-b1 | Tiger e2-e4 | Rooster b1-c2 | Ox e1-e2 | Tiger a5-a3
18/24     44 s   500   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox e4-d4 | Rooster d2-e2 | Crane c5-c4 | Ox d1-d2 | Rooster c3-b2 | Crane e2-e3 | Tiger d4-d2 | Rooster e3-d2 | Ox c4-c3 | Tiger a1-a3 | Crane b2-a3 | Horse e1-e2 | Tiger c3-c1
18/24->   44 s   500   Ox d5-d4 | Horse d2-c2 | Rooster d4-c3 | Ox c2-d2 | Horse e5-e4 | Crane b1-b2 | Ox e4-d4 | Rooster d2-e2 | Crane c5-c4 | Ox d1-d2 | Rooster c3-b2 | Crane e2-e3 | Tiger d4-d2 | Rooster e3-d2 | Ox c4-c3 | Tiger a1-a3 | Crane b2-a3 | Horse e1-e2 | Tiger c3-c1

Nominal depth searched: 18
Max depth searched: 24

States evaluated: 187451090
Quiescence states evaluated: 14778317
Leaves evaluated: 113728736

TT size: 268435456 entries (3072 MB)
TT fill rate: 14.04 %
TT hit rate: 36.32 % (27779445 / 76487599) -- 1: 0.00%  2: 1.83%  3: 39.85%  4: 15.66%  5: 19.53%  6: 17.46%  7: 5.48%  8: 25.91%  9: 7.01%  10: 32.02%  11: 8.55%  12: 37.94%  13: 11.60%  14: 41.44%  15: 16.73%  16: 42.39%  17: 23.86%  18: 37.15%
Best move hit rate: 31.44 % (21089510 / 67085575) -- 1: 94.44%  2: 93.33%  3: 69.32%  4: 79.65%  5: 55.98%  6: 82.20%  7: 64.26%  8: 80.70%  9: 63.15%  10: 79.08%  11: 61.53%  12: 74.37%  13: 54.12%  14: 62.65%  15: 35.45%  16: 36.64%  17: 0.05%  18: 6.56%

Branching factor: 2.79 -- 1: 9.50  2: 2.56  3: 8.44  4: 1.62  5: 7.73  6: 1.25  7: 8.82  8: 1.23  9: 8.76  10: 1.14  11: 8.61  12: 1.12  13: 7.83  14: 1.18  15: 6.25  16: 1.24  17: 3.16  18: 0.63
Quiescence branching factor:  19: 0.71  20: 0.62  21: 0.38  22: 0.34  23: 0.13  24: 0.00
```

## Details on features implemented/tried

### Transposition table ([wiki](https://chessprogramming.wikispaces.com/Transposition+Table))
Also known as TT. A table that stores states that have been evaluated previously during the search, so that they don't have to be evaluated again.
Uses [Zobrist hashing](https://chessprogramming.wikispaces.com/Zobrist+Hashing) to encode states.
By itself this improved search times by around 25 %, using a replace-always scheme. Moreover, it makes it possible to store the
best move for each node. Changing this to a depth-preferred scheme gave much better results when the TT was filling up (>25 % full), but sometimes
resulted in worse results. Using a two-tier table, storing one depth-preferred entry and one most recent entry, gave the best result. Huge improvements
were seen for searches where the TT was filling up. However, for situations where the TT is sparsely populated, such as fast game play with a large TT,
this does not matter much. An example search from the initial board state with a 29 bit (6 GB) TT searched 22 plies in 179 minutes and 38.8 billion
states with a replace-always scheme, and in 32 minutes and 6.45 billion states with the two-tier scheme (>90 % populated TT).

##### Composition
Each entry in the TT uses 96 bits (12 bytes), organized like this:

|Bits|Size|Purpose|Range|
|----|----|-------|-----|
|0 - 63|64 bits|Zobrist key (board and card state)|64 bits|
|64 - 65|2 bits|Bound type|0 = exact score, 1 = lower bound, 2 = lower bound|
|66 - 71|6 bits|Depth|0 - 63|
|72 - 81|10 bits|Score|-512 - 511|
|82 - 86|5 bits|Best move, origin cell|0 - 24|
|87|1 bit|Best move, card used|0 = lower card played, 1 = higher card played|
|88 - 92|5 bits|Best move, destination cell|0 - 24|
|93 - 95|3 bits|Unused|-|

It should be possible to save some space by combining the Zobrist key and the TT index. For example, if a 24 bit TT is used,
we could use the lower 24 bits of the Zobrist key as the index into the TT, and only store the higher 40 bits in the table.
Moreover, the Zobrist keys might not need to be 64 bits, they could be made shorter, at the cost of an increased risk of
hash collisions. I have not experimented with this.

##### Dynamic resizing
This allows the TT to change size during the search, carrying over all stored entries. Experiments show that this works
quite well (at least with a two-tiered TT); a search with a small TT that is later adjusted to a larger size, does not seem to suffer in the
long run from initially having started out small. This is used by the pondering feature, by starting many searches simultaneously with small TTs, and
increasing the size gradually as some searches finish and there are fewer remaining.


### Move ordering ([wiki](https://chessprogramming.wikispaces.com/Move+Ordering))
Thanks to [alpha-beta pruning](https://chessprogramming.wikispaces.com/Alpha-Beta),
trying better moves first during the search
lowers the effective [branching factor](https://chessprogramming.wikispaces.com/Branching+Factor),
meaning an exponential savings in nodes visited (bigger saving the
more plies are searched). At a typical game state, there are 10 - 20 possible moves, yet with efficient move
ordering, we only need to test ~2.5 of those moves on average before a cutoff. To save time, moves are
generated lazily (no need to score and sort 20 moves, if we only need to test 2 of them). The order is as follows:

1. [Best move](https://chessprogramming.wikispaces.com/Best+Move), if available.
   This is the best move found during a more shallow search in
   [iterative deepening](https://chessprogramming.wikispaces.com/Iterative+Deepening) and
   stored in the TT. This by itself resulted in ~5x faster search times over no move ordering at all
   in the `TestVariousBoardsAndCards` test suite.
   (Note: It is ok to put this before winning moves, since the best move will always be the
   winning move, if such exists, thanks to the quiescence search.)
1. Winning moves; capturing the opponent's king, or moving the king to the winning position. This reduced the number
   of visited states by ~20 % in the test suite.
1. Piece captures. This made a huge difference -- 10-20x less states visited overall in the test suite.
1. [History table heuristic](https://chessprogramming.wikispaces.com/History+Heuristic).
   Moves are ordered by how often they produce non-capture, non-winning alpha-beta cutoffs.
   This cut the number of visited nodes in half in the test suite.

##### Two best moves
Tried this, and it did not decrease the number of states visited. If anything it did the opposite. Unclear why. The
implementation seemed to work as intended. Possibly the two best moves tend to be similar to each other (such as grabbing a certain
opponent piece, leading to material difference and a high score), and in the case that the first move fails to produce a cut-off, a more different
move is needed.


### Evaluation function ([wiki](https://chessprogramming.wikispaces.com/Evaluation))
First used `material difference * 100 + king distance difference`. Then changed to
`material difference * 100 + weighted piece position difference`. This resulted in a 90 % win rate against
an AI with the old function. That's a better improvement than increasing the search depth by 1! Adding
[mobility](https://chessprogramming.wikispaces.com/Mobility)
to the equation did not improve the win rate. The weights are as follows:

```
+---+---+---+---+---+
| 0 | 1 | 2 | 1 | 0 |
+---+---+---+---+---+
| 1 | 2 | 3 | 2 | 1 |
+---+---+---+---+---+
| 2 | 3 | 4 | 3 | 2 |
+---+---+---+---+---+
| 1 | 2 | 3 | 2 | 1 |
+---+---+---+---+---+
| 0 | 1 | 2 | 1 | 0 |
+---+---+---+---+---+
```


### Bitboards ([wiki](https://chessprogramming.wikispaces.com/Bitboards))
Instead of representing the board as a 2D array,
and available moves as a list, store them as bits packed into integers, where each bit represents
a cell on the board. This speeds up
move generation and validation, and unlike most other techniques, which seek to limit the search space, this
feature solely works on reducing the evaluation time of each individual state.

For example, for determining valid moves, an array of 5x5 bits (stored in a single 32 bit integer) is pre-calculated for each
combination of card, player and cell on the board, with true/false indicating whether a move to that position is valid.
Things like filtering for capture moves, or excluding moves onto oneself, then become simple bitwise operations.

This feature resulted in a 4x speedup overall.


### Quiescence search ([wiki](https://chessprogramming.wikispaces.com/Quiescence%20Search))
If there are pending captures or wins once the [horizon node](https://chessprogramming.wikispaces.com/Horizon+Node)
is reached, keep searching until a quiet stage is reached. This resulted
in an 80-90 % win rate against an AI without quiescence search with the same nominal depth. However, since it searches more states, it uses a bit more
time. Adjusting for this, i.e. searching at unlimited depth (with iterative deepening) for a fixed period of time per move, the win rate was 63-68 %
(times tested were 30, 50, 100 and 1000 ms / move). In general, wins will be detected in at least one depth less. For the default test case in
`TestSingleSearch`, it means that the win is found at depth 12 in 21 seconds rather than depth 13 in 41 seconds.

##### Quiescence search and the TT
Tried storing the result from the quiescence search in the TT, and even using the TT for all quiescence nodes. Preliminary testing to store and retrieve
the quiescence scores actually made the search take twice as long. Should experiment more with this, it feels like it could be improved.

##### Check evasion during quiescence search
This did not help, it lead to quite a bit more nodes searched during the quiescence search, without finding a win faster.


### Principal variation search ([wiki](https://chessprogramming.wikispaces.com/Principal%20Variation%20Search))
Trivial change, which reduced the number of visited nodes by roughly 10 % on average. For some test cases, it barely
made a difference, although for some it cut the number of visited nodes in half. Running AI vs AI tests with a fixed time per move (200, 2000
and 5000 ms) showed a very slight improvement with a 52.5 - 54 % win rate for the principal variation search.


### Aspiration windows ([wiki](https://chessprogramming.wikispaces.com/Aspiration+Windows))
Experimented with this and did not find that it helped.


### Endgame table ([wiki](https://chessprogramming.wikispaces.com/Endgame+Tablebases))
Problematic since there are 131,040 combinations of 5 cards, and it would take a long time to pre-calculate all of them. Possibly a
few background threads can calculate endgames once the 5 cards are known. Searching the default test case to the end (depth 12), we have that 0.02 % of
all states analyzed have 2 pieces, 0.25 % have 3 pieces, 1.4 % have 4 pieces, 4.7 % have 5 pieces, and 12.1 % have 6 pieces. There are 635,400 possible
board states with 4 pieces, not including card permutations, so it may be feasible to calculate all the endgames up to 4 pieces during runtime, but
likely not more than that. This might not make much of a difference during general game play. (Can try this by extending the depth when the piece
count is small.)


### Pondering ([wiki](https://chessprogramming.wikispaces.com/Pondering))
Most literature recommends a single search pondering just the most probable opponent move, assuming that this move will actually be
played by the opponent 50+ % of the times ("ponder hit rate"). For this project, I have a assumed a much lower ponder hit rate, so instead a
separate search is started for every possible opponent move, and once the opponent moves, all irrelevant search threads are terminated. This
feature uses dynamic TT resizing to make efficient use of the available memory.


### Parallelization ([wiki](https://chessprogramming.wikispaces.com/Parallel+Search))
Tried parallelization through multiple identical search threads started at the same time with a
[shared TT](https://chessprogramming.wikispaces.com/Shared%20Hash%20Table). This scaled quite badly, with a speedup of
around 1.2 - 1.25 with 2 - 3 threads (on a 2x4 core system), with no TT locking at all. The number of nodes visited was 60-70 % of the single
threaded search.


## Future ideas / improvements
- Better UI. In particular the pondering threads are cluttering the output now.
- Clean up the test code. There is a lot of commented out and temporary code from one-off tests.
- Compress the TT better. Store key and state together for better cache locality. There's room for improvement.
- Better evaluation function. For example using machine learning and playing AIs against each other.
