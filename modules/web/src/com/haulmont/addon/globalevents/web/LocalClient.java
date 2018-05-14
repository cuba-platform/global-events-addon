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
import com.haulmont.addon.globalevents.LocalRegistry;
import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.cuba.web.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("cubaglevt_LocalClient")
public class LocalClient {

    private static final Logger log = LoggerFactory.getLogger(LocalClient.class);

    @Inject
    private WebConfig webConfig;

    @Inject
    private WebBroadcaster webBroadcaster;

    @Inject
    private GlobalUiEvents globalUiEvents;

    @Inject
    private Events events;

    private ExecutorService executor = Executors.newFixedThreadPool(5,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("GE-LocalClient-%d")
                    .setThreadFactory(Executors.defaultThreadFactory())
                    .build());

    @EventListener(AppContextStartedEvent.class)
    public void init() {
        if (webConfig.getUseLocalServiceInvocation()) {
            LocalRegistry.getInstance().addListener(this::onMessage);
        }
    }

    @EventListener(AppContextStoppedEvent.class)
    public void dispose() {
        executor.shutdownNow();
    }

    public void onMessage(byte[] message) {
        GlobalApplicationEvent event = (GlobalApplicationEvent) SerializationSupport.deserialize(message);

        if (webBroadcaster.getOrigin().equals(event.getClientOrigin())) {
            log.debug("Received own event, ignoring it");
            return;
        }

        // decouple from the calling thread
        executor.submit(() -> {
            if (event instanceof GlobalUiEvent) {
                globalUiEvents.publish(event);
            } else {
                events.publish(event);
            }
        });
    }
}
