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

package com.haulmont.addon.globalevents.web;

import com.haulmont.addon.globalevents.GlobalApplicationEvent;
import com.haulmont.addon.globalevents.transport.AbstractWebSocketClient;
import com.haulmont.addon.globalevents.transport.NoServersException;
import com.haulmont.addon.globalevents.transport.WebSocketAuthData;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.web.WebConfig;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import com.haulmont.cuba.web.security.events.AppStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("cubaglevt_WebRemoteClient")
public class WebRemoteClient extends AbstractWebSocketClient {

    @Inject
    private WebAuthConfig webAuthConfig;

    @Inject
    private WebConfig webConfig;

    @Inject
    private GlobalUiEvents globalUiEvents;

    @Inject
    private WebBroadcaster webBroadcaster;

    @Override
    protected WebSocketAuthData getAuthMessageContent() {
        return new WebSocketAuthData(webAuthConfig.getTrustedClientPassword());
    }

    @Override
    protected Object getCurrentClientOrigin() {
        return webBroadcaster.getOrigin();
    }

    @Override
    protected void publishGlobalUiEvent(GlobalApplicationEvent event) {
        globalUiEvents.publish(event);
    }

    @EventListener(AppStartedEvent.class)
    public void init() {
        if (!webConfig.getUseLocalServiceInvocation()) {
            // connect on first web request
            try {
                connect();
            } catch (NoServersException e) {
                throw new RuntimeException("Unable to open session", e);
            }
        }
    }

    @EventListener(AppContextStoppedEvent.class)
    public void dispose() {
        if (!webConfig.getUseLocalServiceInvocation()) {
            // disconnect on server shutdown
            disconnect();
        }
    }
}
