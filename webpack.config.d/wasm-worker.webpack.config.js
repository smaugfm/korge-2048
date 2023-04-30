const CopyWebpackPlugin = require('copy-webpack-plugin');

// Here comes a list of hacks to make koltin/wasm working inside a web-worker.
if (config.output.filename({chunk: {name: "main"}}).startsWith("wasm-worker")) {

    // First, Kotlin compiler emits glue code for wasm that checks what environment it is being run on.
    // And when run inside web-worker neither of the checks succeeds.
    // So we patch the glue code to execute browser branch which
    // uses WebAssembly.instantiateStreaming()
    config.module.rules.push({
        test: /game2048-wasm-worker\.uninstantiated\.mjs$/,
        loader: 'string-replace-loader',
        options: {
            search: /isBrowser\s*=\s*.*?;/,
            replace: 'isBrowser=true;',
        }
    });

    // Just copy the wasm file into the output directory because this is not
    // happening automatically.
    config.plugins.push(new CopyWebpackPlugin({
        patterns: [{
            from: require('path')
            .resolve(__dirname, "kotlin/game2048-wasm-worker.wasm"),
            to: "game2048-wasm-worker.wasm",
        }]
    }));
}

if (config.output.filename({chunk: {name: "main"}}).startsWith("wasm-test")) {
    config.plugins.push(new CopyWebpackPlugin({
        patterns: [{
            from: require('path')
            .resolve(__dirname, "kotlin/game2048-wasm-test.uninstantiated.mjs"),
            to: "game2048-wasm-test.uninstantiated.mjs",
        }]
    }));
    config.plugins.push(new CopyWebpackPlugin({
        patterns: [{
            from: require('path')
            .resolve(__dirname, "kotlin/game2048-wasm-test.wasm"),
            to: "game2048-wasm-test.wasm",
        }]
    }));
}
