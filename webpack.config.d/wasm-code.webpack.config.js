const CopyWebpackPlugin = require('copy-webpack-plugin');

if (config.output.filename({ chunk: { name: "main" } }).startsWith("wasm-worker")) {
    config.module.rules.push({
        test: /\uninstantiated\.m?js$/,
        loader: 'string-replace-loader',
        options: {
            search: /isStandaloneJsVM\s*=\s*.*?;/s,
            replace: 'isStandaloneJsVM=false;',
        }
    });
    config.module.rules.push({
        test: /\uninstantiated\.m?js$/,
        loader: 'string-replace-loader',
        options: {
            search: /isBrowser\s*=\s*.*?;/,
            replace: 'isBrowser=true;',
        }
    });
    config.module.rules.push({
        test: /\uninstantiated\.m?js$/,
        loader: 'string-replace-loader',
        options: {
            search: /\.\/game2048-wasm-worker.wasm/,
            replace: './wasm-worker.wasm',
        }
    });
    //
    config.plugins.push(new CopyWebpackPlugin({
        patterns: [{
            from: require('path')
                .resolve(__dirname, "kotlin/game2048-wasm-worker.wasm"),
            to: "wasm-worker.wasm",
        }]
    }));
}
