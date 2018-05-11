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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.UUID;

@Component("cubaglevt_CoreBroadcaster")
public class CoreBroadcaster {

    private UUID origin = UUID.randomUUID();

    @Inject
    private WebSocketServer wsServer;

    @Inject
    private LocalServer localServer;

    @Inject
    private Events events;

    private ClusterManagerAPI clusterManagerAPI;

    @Inject
    public void setClusterManager(ClusterManagerAPI clusterManagerAPI) {
        this.clusterManagerAPI = clusterManagerAPI;
        clusterManagerAPI.addListener(GlobalApplicationEvent.class, new ClusterListenerAdapter<GlobalApplicationEvent>() {
            @Override
            public void receive(GlobalApplicationEvent event) {
                events.publish(event);
            }
        });
    }

    @EventListener
    public void onGlobalEvent(GlobalApplicationEvent event) {
        if (event.getServerOrigin() == null) {
            event.setServerOrigin(origin);
            clusterManagerAPI.send(event);
        }
        localServer.sendEvent(event);
        wsServer.sendEvent(event);
    }
}
