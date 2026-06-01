
## Eureka client under the hood
Data is holt in `com.netflix.discovery.shared.Application.instancesMap` and `Applications->virtualHostNameAppMap`
Data is refreshed in `com.netflix.discovery.DiscoveryClient.updateDelta` method
```java
private void updateDelta(Applications delta) {
    int deltaCount = 0;
    for (Application app : delta.getRegisteredApplications()) {
        for (InstanceInfo instance : app.getInstances()) {
            // ...
            if (ActionType.ADDED.equals(instance.getActionType())) {
                // ...
                applications.getRegisteredApplications(instance.getAppName()).addInstance(instance);
            } else if (ActionType.MODIFIED.equals(instance.getActionType())) {
                // ...
                applications.getRegisteredApplications(instance.getAppName()).addInstance(instance);
            } else if (ActionType.DELETED.equals(instance.getActionType())) {
                Application existingApp = applications.getRegisteredApplications(instance.getAppName());
                if (existingApp != null) {
                    existingApp.removeInstance(instance);
                    // ...
                }
            }
        }
    }
    logger.debug("The total number of instances fetched by the delta processor : {}", deltaCount);
    // ...
}
```