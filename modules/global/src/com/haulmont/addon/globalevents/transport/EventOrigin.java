/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.addon.globalevents.transport;

import java.io.Serializable;
import java.util.Objects;

/**
 * INTERNAL.
 * Contains information needed to prevent cycles.
 */
public class EventOrigin implements Serializable {

    private Object client;
    private Object server;

    public void setClient(Object client) {
        this.client = client;
    }

    public void setServer(Object server) {
        this.server = server;
    }

    public boolean fromClient() {
        return client != null;
    }

    public boolean fromServer() {
        return server != null;
    }

    public boolean sameClient(Object other) {
        return Objects.equals(client, other);
    }
}
