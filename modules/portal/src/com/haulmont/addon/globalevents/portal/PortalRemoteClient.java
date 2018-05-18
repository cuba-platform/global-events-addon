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

package com.haulmont.addon.globalevents.portal;

import com.haulmont.addon.globalevents.GlobalApplicationEvent;
import com.haulmont.addon.globalevents.transport.AbstractWebSocketClient;
import com.haulmont.addon.globalevents.transport.NoServersException;
import com.haulmont.addon.globalevents.transport.WebSocketAuthData;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.portal.config.PortalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("cubaglevt_PortalRemoteClient")
public class PortalRemoteClient extends AbstractWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(PortalRemoteClient.class);

    @Inject
    private PortalConfig portalConfig;

    @Inject
    private PortalBroadcaster portalBroadcaster;

    @Override
    protected WebSocketAuthData getAuthMessageContent() {
        return new WebSocketAuthData(portalConfig.getTrustedClientPassword());
    }

    @Override
    protected Object getCurrentClientOrigin() {
        return portalBroadcaster.getOrigin();
    }

    @Override
    protected void publishGlobalUiEvent(GlobalApplicationEvent event) {
        // ignore
    }

    @EventListener(AppContextStartedEvent.class)
    public void init() {
        // TODO replace with PortalConfig since 6.9
        String useLocal = AppContext.getProperty("cuba.useLocalServiceInvocation");
        if (!Boolean.valueOf(useLocal)) {
            Thread thread = new Thread(this::doConnect);
            thread.setDaemon(true);
            thread.setName("GE-PortalRemoteClient-connect");
            thread.start();
        }
    }

    private void doConnect() {
        for (int i = 0; i < 1000; i++) {
            try {
                connect();
                break;
            } catch (NoServersException e) {
                log.info("Unable to open session: " + e);
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e1) {
                    log.warn("Waiting for connection has been interrupted");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (webSocketSession == null) {
            log.warn("WebSocket is not connected, global events will not be received");
        }
    }

    @EventListener(AppContextStoppedEvent.class)
    public void dispose() {
        // TODO replace with PortalConfig since 6.9
        String useLocal = AppContext.getProperty("cuba.useLocalServiceInvocation");
        if (!Boolean.valueOf(useLocal)) {
            // disconnect on server shutdown
            disconnect();
        }
    }
}
