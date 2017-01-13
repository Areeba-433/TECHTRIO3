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
"use strict";

import * as bodyParser from "body-parser";
import * as express from "express";
import * as path from "path";

import * as routes from "./routes";

class Server {

    public app: express.Application;

    public static bootstrap(): Server {
        return new Server();
    }

    constructor() {
        this.app = express();
        this.routes();
        this.app.listen(3000, () => {
            console.log("Server listening...");
        });
    }

    private routes() {
        let router = express.Router();
        this.app.use(express.static(path.resolve(__dirname, "../../ui/dist")));

        this.app.use(bodyParser.json());
        this.app.use(bodyParser.urlencoded());

        let oauthLogin: routes.OAuthLogin = new routes.OAuthLogin();
        router.post("/oauth/authenticate", oauthLogin.oauthLogin);

        let api = new routes.Api();
        router.all("/api/*", api.api);

        let index: routes.Index = new routes.Index();
        router.get("*", index.index);

        this.app.use(router);
    }
}

Server.bootstrap();

