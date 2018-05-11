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

import java.io.Serializable;

public class GlobalEventClusterMessage implements Serializable {

    private GlobalApplicationEvent event;

    public GlobalEventClusterMessage(GlobalApplicationEvent event) {
        this.event = event;
    }

    public GlobalApplicationEvent getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "GlobalEventClusterMessage{" +
                "event=" + event +
                '}';
    }
}
