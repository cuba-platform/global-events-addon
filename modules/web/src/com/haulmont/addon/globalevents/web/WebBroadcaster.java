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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.haulmont.addon.globalevents.GlobalApplicationEvent;
import com.haulmont.addon.globalevents.GlobalUiEvent;
import com.haulmont.addon.globalevents.transport.GlobalEventsService;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.security.app.TrustedClientService;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("cubaglevt_WebBroadcaster")
public class WebBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(WebBroadcaster.class);

    private Integer origin = new Random().nextInt();

    @Inject
    private GlobalUiEvents globalUiEvents;

    @Inject
    private GlobalEventsService globalEventsService;

    @Inject
    private TrustedClientService trustedClientService;

    @Inject
    private WebAuthConfig webAuthConfig;

    private ExecutorService executor = Executors.newFixedThreadPool(5,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("GE-WebBroadcaster-%d")
                    .setThreadFactory(Executors.defaultThreadFactory())
                    .build());

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
            // decouple from the calling thread
            executor.submit(() -> globalUiEvents.publish(event));
        }

        UserSession session;
        try {
            session = trustedClientService.getSystemSession(webAuthConfig.getTrustedClientPassword());
        } catch (LoginException e) {
            throw new RuntimeException("Unable to get system session for sending global event", e);
        }
        AppContext.withSecurityContext(new SecurityContext(session), () ->
                globalEventsService.sendEvent(event));
    }

    @EventListener(AppContextStoppedEvent.class)
    public void dispose() {
        executor.shutdownNow();
    }
}
