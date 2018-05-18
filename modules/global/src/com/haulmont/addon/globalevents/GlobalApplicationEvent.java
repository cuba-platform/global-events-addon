/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.addon.globalevents;

import com.haulmont.addon.globalevents.transport.EventOrigin;
import org.springframework.context.ApplicationEvent;

/**
 * Base class for application events that must be propagated to all connected blocks of the application.
 */
public class GlobalApplicationEvent extends ApplicationEvent {

    private EventOrigin eventOrigin = new EventOrigin();

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public GlobalApplicationEvent(Object source) {
        super(source);
    }

    /**
     * INTERNAL
     */
    public EventOrigin getEventOrigin() {
        return eventOrigin;
    }
}
