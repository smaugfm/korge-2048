const CopyWebpackPlugin = require('copy-webpack-plugin');

// Here comes a list of hacks to make koltin/wasm working inside a web-worker.
if (config.output.filename({ chunk: { name: "main" } }).startsWith("wasm-worker")) {

    // First, Kotlin compiler emits glue code for wasm that checks what environment it is being run on.
    // And when run inside web-worker neither of the checks succeeds.
    // So we patch the glue code to execute browser branch which
    // uses WebAssembly.instantiateStreaming()
    config.module.rules.push({
        test: /\uninstantiated\.m?js$/,
        loader: 'string-replace-loader',
        options: {
            search: /isBrowser\s*=\s*.*?;/,
            replace: 'isBrowser=true;',
        }
    });

    // Just copy the wasm file into the output directory because this is not
    // happening automatically.
    // Also rename string in the bundled js to match copied wasm filename.
    config.module.rules.push({
        test: /\uninstantiated\.m?js$/,
        loader: 'string-replace-loader',
        options: {
            search: /\.\/game2048-wasm-worker.wasm/,
            replace: './wasm-worker.wasm',
        }
    });
    config.plugins.push(new CopyWebpackPlugin({
        patterns: [{
            from: require('path')
                .resolve(__dirname, "kotlin/game2048-wasm-worker.wasm"),
            to: "wasm-worker.wasm",
        }]
    }));
}
