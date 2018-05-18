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
import com.haulmont.addon.globalevents.GlobalUiEvent;
import com.haulmont.addon.globalevents.transport.GlobalEventsService;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.desktop.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.*;
import java.util.Random;

@Component("cubaglevt_DesktopBroadcaster")
public class DesktopBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(DesktopBroadcaster.class);

    private int origin = new Random().nextInt();

    @Inject
    private GlobalEventsService globalEventsService;

    @Inject
    private UserSessionSource userSessionSource;

    public Integer getOrigin() {
        return origin;
    }

    @EventListener
    public void onGlobalEvent(GlobalApplicationEvent event) {
        if (event.getEventOrigin().fromClient()) {
            log.debug("Event from another client, ignoring it");
            return;
        }
        if (event.getEventOrigin().fromServer()) {
            log.debug("Event from server, ignoring it");
            return;
        }
        event.getEventOrigin().setClient(origin);

        if (event instanceof GlobalUiEvent) {
            SwingUtilities.invokeLater(() -> {
                App.getInstance().getUiEventsMulticaster().multicastEvent(event);
            });
        }

        // in case we are not in EDT
        AppContext.withSecurityContext(new SecurityContext(userSessionSource.getUserSession()), () ->
                globalEventsService.sendEvent(event));
    }
}
