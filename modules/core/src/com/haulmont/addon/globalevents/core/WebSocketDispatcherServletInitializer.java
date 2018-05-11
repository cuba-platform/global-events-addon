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

import com.haulmont.cuba.core.sys.servlet.ServletRegistrationManager;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.Servlet;

@Component("cubaglevt_WebSocketDispatcherServletInitializer")
public class WebSocketDispatcherServletInitializer {

    @Inject
    private ServletRegistrationManager servletRegistrationManager;

    @EventListener
    public void initializeServlets(ServletContextInitializedEvent e) {
        Servlet myServlet = servletRegistrationManager.createServlet(e.getApplicationContext(),
                "com.haulmont.addon.globalevents.core.WebSocketDispatcherServlet");

        e.getSource().addServlet("cubaglevt_ws_servlet", myServlet)
                .addMapping("/cubaglevt-ws/*");
    }
}
