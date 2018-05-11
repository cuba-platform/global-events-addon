/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.addon.globalevents;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class GlobalApplicationEvent extends ApplicationEvent {

    private UUID serverOrigin;

    private UUID clientOrigin;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public GlobalApplicationEvent(Object source) {
        super(source);
    }

    public UUID getServerOrigin() {
        return serverOrigin;
    }

    public void setServerOrigin(UUID serverOrigin) {
        this.serverOrigin = serverOrigin;
    }

    public UUID getClientOrigin() {
        return clientOrigin;
    }

    public void setClientOrigin(UUID clientOrigin) {
        this.clientOrigin = clientOrigin;
    }
}
