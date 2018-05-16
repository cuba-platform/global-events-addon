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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.UUID;

public class WebSocketAuthData implements Serializable {

    private static final long serialVersionUID = 2573903795522147359L;

    private final UUID userSessionId;
    private final String trustedClientPassword;

    public WebSocketAuthData(UUID userSessionId) {
        this.userSessionId = userSessionId;
        this.trustedClientPassword = null;
    }

    public WebSocketAuthData(String trustedClientPassword) {
        this.trustedClientPassword = trustedClientPassword;
        this.userSessionId = null;
    }

    @Nullable
    public UUID getUserSessionId() {
        return userSessionId;
    }

    @Nullable
    public String getTrustedClientPassword() {
        return trustedClientPassword;
    }

    @Override
    public String toString() {
        return "WebSocketAuthData{" +
                "userSessionId=" + userSessionId +
                ", trustedClientPassword='" + trustedClientPassword + '\'' +
                '}';
    }
}
