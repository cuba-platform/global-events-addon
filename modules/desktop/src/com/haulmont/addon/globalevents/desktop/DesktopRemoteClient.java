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

package com.haulmont.addon.globalevents.desktop;

import com.haulmont.addon.globalevents.GlobalApplicationEvent;
import com.haulmont.addon.globalevents.transport.AbstractWebSocketClient;
import com.haulmont.addon.globalevents.transport.NoServersException;
import com.haulmont.addon.globalevents.transport.WebSocketAuthData;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.desktop.App;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import java.util.UUID;

@Component("cubaglevt_DesktopRemoteClient")
public class DesktopRemoteClient extends AbstractWebSocketClient {

    @Inject
    private DesktopBroadcaster desktopBroadcaster;

    @Inject
    private UserSessionSource userSessionSource;

    @Override
    protected WebSocketAuthData getAuthMessageContent() {
        return new WebSocketAuthData(userSessionSource.getUserSession().getId());
    }

    @Override
    protected UUID getCurrentClientOrigin() {
        return desktopBroadcaster.getOrigin();
    }

    @Override
    protected void publishGlobalUiEvent(GlobalApplicationEvent event) {
        SwingUtilities.invokeLater(() -> {
            App.getInstance().getUiEventsMulticaster().multicastEvent(event);
        });
    }

    @PostConstruct
    public void init() {
        App.getInstance().getConnection().addListener(connection -> {
            if (connection.isConnected()) {
                try {
                    connect();
                } catch (NoServersException e) {
                    throw new RuntimeException("Unable to open session", e);
                }
            } else {
                disconnect();
            }
        });
    }
}
