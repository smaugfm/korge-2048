# 2048 AI

2048 game AI implemented in Kotlin with

- [Korge](https://korge.org) game engine
- 64-bit board representation, [Expectimax](https://en.wikipedia.org/wiki/Expectiminimax)
  search algorithm, precomputed tables for moves and position evaluation and other
  optimizations (from [here](https://github.com/nneonneo/2048-ai))
- Kotlin/JS or WASM inside web-workers for multi-threading in the browser

This project evolved from the
introductory [tutorial](https://blog.korge.org/korge-tutorial-writing-2048-game-step-0) to
the Korge engine. Then I added the AI which on JVM now can search to up to 10 million moves
per second with depths up to 10 on the M1 Macbook.

Web version compiled with Kotlin/JS is here: [2048.marchuk.io](2048.marchuk.io).

Kotlin/WASM uses experimetnal [Web Assembly garbage collection](https://github.com/WebAssembly/gc) and if it is not supported in the browser the game falls back to JS-only implementation of Expectimax search. 
For now, to achieve maximum performance in the web version use Chromium-based browser.
