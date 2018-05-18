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

package com.haulmont.addon.globalevents.core;

import com.haulmont.addon.globalevents.GlobalApplicationEvent;
import com.haulmont.cuba.core.app.ClusterListenerAdapter;
import com.haulmont.cuba.core.app.ClusterManagerAPI;
import com.haulmont.cuba.core.global.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Random;

@Component("cubaglevt_CoreBroadcaster")
public class CoreBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(CoreBroadcaster.class);

    private Integer origin = new Random().nextInt();

    @Inject
    private WebSocketServer webSocketServer;

    @Inject
    private LocalServer localServer;

    @Inject
    private Events events;

    private ClusterManagerAPI clusterManagerAPI;

    @Inject
    public void setClusterManager(ClusterManagerAPI clusterManagerAPI) {
        this.clusterManagerAPI = clusterManagerAPI;
        clusterManagerAPI.addListener(GlobalEventClusterMessage.class, new ClusterListenerAdapter<GlobalEventClusterMessage>() {
            @Override
            public void receive(GlobalEventClusterMessage message) {
                log.debug("Received {}, re-publishing it", message.getEvent());
                events.publish(message.getEvent());
            }
        });
    }

    @EventListener
    public void onGlobalEvent(GlobalApplicationEvent event) {
        if (!event.getEventOrigin().fromServer()) {
            event.getEventOrigin().setServer(origin);
            clusterManagerAPI.send(new GlobalEventClusterMessage(event));
        }
        localServer.sendEvent(event);
        webSocketServer.sendEvent(event);
    }
}
