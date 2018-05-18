# Global Events Add-on (work in progress)

## Overview

The standard CUBA [Events](https://doc.cuba-platform.com/manual-latest/events.html) interface allows you to publish events that can be received by subscribers within the same application block. So there are no built-in facilities for notifying clients from middleware or propagating events in the cluster.
 
The Global Events add-on enables receiving Spring's application events on any application block of the distributed system. In the simplest configuration with `core` and `web` blocks running in a single JVM, it allows you to send events from `core` to `web` to notify UI screens or managed beans. In a cluster environment, an event published inside a block, will be received on all other connected parts of the system: on all middleware blocks and on the clients: `web`, `portal` and `desktop`.

In case of the client is deployed in the same JVM as middleware and `cuba.useLocalServiceInvocation` application property is set to `true`, it registers a callback in the `LocalRegistry` class located in the `shared-lib` module which is accessible to both middleware and the client:
 
![Local Interaction](etc/local-interaction.png)

In a distributed environment, clients open WebSocket connections to the middleware blocks, and middleware exchange events in a usual way using the cluster communication mechanism:

![Cluster Interaction](etc/cluster-interaction.png)

## Usage

Select a version of the add-on which is compatible with the platform version used in your project:

| Platform Version | Add-on Version |
| ---------------- | -------------- |
| 6.8.x            | 0.1.0          |

Add custom application component to your project (change the version part if needed):

`com.haulmont.addon.globalevents:cubaglevt-global:0.1.0`

Your global event classes must be inherited from `com.haulmont.addon.globalevents.GlobalApplicationEvent`, for example:

    package com.company.sample;
    
    import com.haulmont.addon.globalevents.GlobalApplicationEvent;
    
    public class MyGlobalEvent extends GlobalApplicationEvent {
        
        private String payload;
    
        public MyGlobalEvent(Object source, String payload) {
            super(source);
            this.payload = payload;
        }
    
        public String getPayload() {
            return payload;
        }
    }

Make sure all fields of the event class are serializable! 

If you want to send event to Generic UI screens of connected `web` blocks, add the `GlobalUiEvent` marker interface to the event class:

    package com.company.sample;
    
    import com.haulmont.addon.globalevents.GlobalApplicationEvent;
    import com.haulmont.addon.globalevents.GlobalUiEvent;
    
    public class MyUiNotificationEvent extends GlobalApplicationEvent implements GlobalUiEvent {
        
        private String message;
    
        public MyUiNotificationEvent(Object source, String message) {
            super(source);
            this.message = message;
        }
    
        public String getMessage() {
            return message;
        }
    }

Send global events using the standard `Events.publish()` method, and they will be received by subscribers running on all blocks of your distributed application.

## Testing

The [global-events-demo](https://github.com/cuba-platform/global-events-demo) project contains usage examples and system tests.
