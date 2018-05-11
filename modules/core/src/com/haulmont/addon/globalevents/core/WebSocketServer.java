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
import com.haulmont.cuba.core.app.ServerConfig;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("cubaglevt_WebSocketServer")
public class WebSocketServer extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private Map<WebSocketSession, Boolean> sessions = new ConcurrentHashMap<>();

    @Inject
    private ServerConfig serverConfig;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Received {} from {}", message, session);
        Boolean authenticated = sessions.get(session);
        if (authenticated != null) {
            if (!authenticated) {
                String payload = message.getPayload();
                if (serverConfig.getTrustedClientPassword().equals(payload)) {
                    sessions.put(session, true);
                    log.debug("Authenticated session: " + session);
                } else {
                    log.warn("Invalid credentials, removing session " + session);
                    sessions.remove(session);
                }
            } else {
                log.debug("Session {} is already authenticated");
            }
        } else {
            log.warn("Unknown session: " + session);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Opened session {}", session);
        sessions.put(session, false);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Closed session {} with status {}", session, status);
        sessions.remove(session);
    }

    public void sendEvent(GlobalApplicationEvent event) {
        log.debug("Sending {} to {}", event, sessions);
        Iterator<Map.Entry<WebSocketSession, Boolean>> it = sessions.entrySet().iterator();
        if (it.hasNext()) {
            byte[] bytes = SerializationSupport.serialize(event);
            String str;
            try {
                str = new String(Base64.getEncoder().encode(bytes), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            while (it.hasNext()) {
                Map.Entry<WebSocketSession, Boolean> entry = it.next();
                if (entry.getValue()) { // if the session is authenticated
                    WebSocketSession session = entry.getKey();
                    try {
                        TextMessage message = new TextMessage(str);
                        log.debug("Sending message {} to {}", message, session);
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.warn("Error sending message, removing the session: " + e);
                        it.remove();
                    }
                }
            }
        }
    }
}
