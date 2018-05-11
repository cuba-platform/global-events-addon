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

import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.security.events.AppStartedEvent;
import com.vaadin.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component("globevnt_GlobalUiEvents")
public class GlobalUiEvents {

    private static final Logger log = LoggerFactory.getLogger(GlobalUiEvents.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final List<VaadinSession> sessions = new ArrayList<>();

    @EventListener
    public void onAppStart(AppStartedEvent event) {
        lock.writeLock().lock();
        try {
            sessions.add(VaadinSession.getCurrent());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void publish(ApplicationEvent event) {
        ArrayList<VaadinSession> activeSessions;

        lock.readLock().lock();
        try {
            activeSessions = new ArrayList<>(sessions);
        } finally {
            lock.readLock().unlock();
        }

        log.debug("Sending {} to {} Vaadin sessions", event, activeSessions.size());

        for (VaadinSession session : activeSessions) {
            // obtain lock on session state
            session.accessSynchronously(() -> {
                if (session.getState() == VaadinSession.State.OPEN) {
                    // active app in this session
                    App app = App.getInstance();

                    // notify all opened web browser tabs
                    List<AppUI> appUIs = app.getAppUIs();
                    for (AppUI ui : appUIs) {
                        if (!ui.isClosing()) {
                            // work in context of UI
                            ui.accessSynchronously(() -> {
                                ui.getUiEventsMulticaster().multicastEvent(event);
                            });
                        }
                    }
                }
            });
        }
    }
}
