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
import com.haulmont.addon.globalevents.service.GlobalEventsService;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.portal.config.PortalConfig;
import com.haulmont.cuba.security.app.TrustedClientService;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.UUID;

@Component("cubaglevt_PortalBroadcaster")
public class PortalBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(PortalBroadcaster.class);

    private UUID origin = UUID.randomUUID();

    @Inject
    private GlobalEventsService globalEventsService;

    @Inject
    private TrustedClientService trustedClientService;

    @Inject
    private PortalConfig portalConfig;

    public UUID getOrigin() {
        return origin;
    }

    @EventListener
    public void onGlobalEvent(GlobalApplicationEvent event) {
        if (event.getClientOrigin() != null) {
            log.debug("Event from another client, ignoring it");
            return;
        }
        if (event.getServerOrigin() != null) {
            log.debug("Event from server, ignoring it");
            return;
        }
        event.setClientOrigin(origin);

        UserSession session;
        try {
            session = trustedClientService.getSystemSession(portalConfig.getTrustedClientPassword());
        } catch (LoginException e) {
            throw new RuntimeException("Unable to get system session for sending global event", e);
        }
        AppContext.withSecurityContext(new SecurityContext(session), () ->
                globalEventsService.sendEvent(event));
    }
}
