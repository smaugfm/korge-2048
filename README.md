# game2048

2048 game AI implemented in Kotlin with

- [Korge](https://korge.org) game engine
- 64-bit board representation, [Expectimax](https://en.wikipedia.org/wiki/Expectiminimax)
  search algorithm, precomputed tables for moves and position evaluation and other
  optimizations (from [here](https://github.com/nneonneo/2048-ai))
- Kotlin/WASM inside web-workers for multi-threading in the browser

Evolved from the
introductory [tutorial](https://blog.korge.org/korge-tutorial-writing-2048-game-step-0) to
the Korge engine. Then I added the AI which on JVM now can search to up to 10 million moves
per second with depths up to 10 on the M1 Macbook.
