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

package com.haulmont.addon.globalevents.transport;

import com.haulmont.addon.globalevents.GlobalApplicationEvent;
import com.haulmont.addon.globalevents.GlobalUiEvent;
import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.core.sys.remoting.discovery.ServerSelector;
import com.haulmont.cuba.core.sys.remoting.discovery.StickySessionServerSelector;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebSocketClient.class);

    protected WebSocketSession webSocketSession;

    @Resource(name = ServerSelector.NAME)
    private ServerSelector serverSelector;

    @Inject
    private Events events;

    private ClassLoader classLoader;

    protected abstract WebSocketAuthData getAuthMessageContent();

    protected abstract Object getCurrentClientOrigin();

    protected abstract void publishGlobalUiEvent(GlobalApplicationEvent event);

    @EventListener(AppContextStartedEvent.class)
    public void initClassLoader() {
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    public synchronized void connect() throws NoServersException {
        if (webSocketSession != null)
            return;
        log.debug("Opening session");

        Object context = serverSelector.initContext();
        String url = getUrl(context);
        if (url == null) {
            throw new NoServersException("No available server URLs");
        }
        while (true) {
            try {
                webSocketSession = tryConnect(url);
                break;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof ResourceAccessException) {
                    log.debug("Unable to open session: {}", e.getCause().toString());
                    serverSelector.fail(context);
                    url = getUrl(context);
                    if (url != null)
                        log.debug("Trying next URL");
                    else
                        throw new NoServersException("No more server URLs available");
                } else {
                    throw new RuntimeException("Error opening session", e);
                }
            } catch (InterruptedException e) {
                log.warn("Interrupted attempt to open session");
                Thread.currentThread().interrupt();
                break;
            }
        }
        authenticate();
    }

    private void authenticate() {
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            log.error("Invalid session: " + webSocketSession);
            return;
        }
        log.debug("Sending auth message to " + webSocketSession);
        try {
            WebSocketAuthData authData = getAuthMessageContent();
            byte[] bytes = SerializationSupport.serialize(authData);
            String strData = new String(Base64.getEncoder().encode(bytes), "UTF-8");
            webSocketSession.sendMessage(new TextMessage(strData));
        } catch (IOException e) {
            throw new RuntimeException("Error sending auth message", e);
        }
    }

    private String getUrl(Object context) {
        String url = serverSelector.getUrl(context);
        if (url != null && serverSelector instanceof StickySessionServerSelector) {
            String servletPath = ((StickySessionServerSelector) serverSelector).getServletPath();
            url = url.substring(0, url.lastIndexOf(servletPath));
        }
        return url;
    }

    private WebSocketSession tryConnect(String serverUrl) throws ExecutionException, InterruptedException {
        log.debug("Connecting to " + serverUrl);

        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(standardWebSocketClient));
        SockJsClient sockJsClient = new SockJsClient(transports);

        return sockJsClient
                .doHandshake(new ClientWebSocketHandler(), serverUrl + "cubaglevt-ws/handler")
                .get();
    }

    private void publishEvent(GlobalApplicationEvent event) {
        if (event.getEventOrigin().sameClient(getCurrentClientOrigin())) {
            log.debug("Received own event, ignoring it");
            return;
        }

        if (event instanceof GlobalUiEvent) {
            publishGlobalUiEvent(event);
        } else {
            events.publish(event);
        }
    }

    public synchronized void disconnect() {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            log.debug("Closing session");
            try {
                webSocketSession.close();
            } catch (IOException e) {
                log.warn("Error closing session: " + e);
            }
        }
        webSocketSession = null;
    }

    private class ClientWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            log.info("Opened " + session);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            log.info("Closed " + session);
            if (webSocketSession != null && webSocketSession.getId().equals(session.getId())) {
                webSocketSession = null;
            }
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            log.debug("Received message {} from {}", message, session);
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                byte[] bytes = Base64.getDecoder().decode(message.getPayload().getBytes("UTF-8"));
                GlobalApplicationEvent event = (GlobalApplicationEvent) SerializationSupport.deserialize(bytes);
                publishEvent(event);
            } catch (Exception e) {
                log.error("Error handling message", e);
            }
        }
    }
}
