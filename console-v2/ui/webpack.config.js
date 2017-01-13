/*******************************************************************************
* Copyright (c) 2011, 2016 Eurotech and/or its affiliates                       
*                                                                               
* All rights reserved. This program and the accompanying materials              
* are made available under the terms of the Eclipse Public License v1.0         
* which accompanies this distribution, and is available at                      
* http://www.eclipse.org/legal/epl-v10.html                                     
*                                                                               
* Contributors:                                                                 
*     Eurotech - initial API and implementation                                 
*                                                                               
*******************************************************************************/
var HtmlWebpackPlugin = require("html-webpack-plugin");
var failPlugin = require("webpack-fail-plugin");
var webpack = require("webpack");

module.exports = {
    entry: "./src/app.ts",
    output: {
        filename: "bundle.js",
        path: "dist"
    },
    resolve: {
        // Add ".ts" and ".tsx" as a resolvable extension.
        extensions: ["", ".webpack.js", ".web.js", ".ts", ".tsx", ".js"],
        alias: {
            "jquery": "angular-patternfly/node_modules/patternfly/node_modules/jquery"
        }
    },
    module: {
        loaders: [
            // all files with a ".ts" or ".tsx" extension will be handled by "ts-loader"
            { test: /\.tsx?$/, loader: "ts" },
            { test: /\.css$/, loader: "style!css" },
            { test: /\.scss$/, loader: "style!css!sass" },
            { test: /\.woff(\?v=\d+\.\d+\.\d+)?$/,loader: "url?limit=10000&mimetype=application/font-woff" }, 
            { test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=application/font-woff" }, 
            { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=application/octet-stream" }, 
            { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: "file" }, 
            { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: "url?limit=10000&mimetype=image/svg+xml" },
            { test: /\.(png|jpe?g|gif)$/, loader: "url?limit=25000" },
            { test: /\.html$/, exclude: /node_modules/, loader: "html" },
            { test: /\.json$/, loader: "json" },
            { test: require.resolve("angular-patternfly/node_modules/patternfly/node_modules/jquery"), loader: 'expose?jQuery!expose?$!expose?jquery!expose?window.jQuery!expose?window.jquery' }
        ]
    },
    plugins: [
        // new webpack.optimize.UglifyJsPlugin(
        //     {
        //         warning: false,
        //         mangle: true,
        //         comments: false
        //     }
        // ),
        new HtmlWebpackPlugin({
            template: "./src/index.html",
            inject: "body",
            hash: true
        }),
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery",
            "window.jQuery": "jquery",
            "window.jquery": "jquery"
        }),
        failPlugin
    ]
}
