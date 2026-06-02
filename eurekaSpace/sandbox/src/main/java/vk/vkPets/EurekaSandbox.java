package vk.vkPets;

import com.netflix.appinfo.*;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EurekaSandbox {
    static EurekaClient eurekaClient;
    static ApplicationInfoManager applicationInfoManager;

    public static void main(String[] args) throws InterruptedException {
        // 1. Define InstanceInfo (your application's metadata)
        EurekaInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();

        LeaseInfo leaseInfo = LeaseInfo.Builder.newBuilder()
                .setRenewalIntervalInSecs(30)
                .build();

        InstanceInfo instanceInfo = InstanceInfo.Builder.newBuilder()
                .setAppName("MY-NATIVE-CLIENT")
                .setIPAddr("127.0.0.1")
                .setHostName("localhost")
                .setPort(8080)
                .setVIPAddress("my-native-client")
                .setLeaseInfo(leaseInfo)
                .setDataCenterInfo(instanceConfig.getDataCenterInfo())
                .build();


//        EurekaInstanceConfig instanceConfig = new CloudInstanceConfig();
//        EurekaInstanceConfig instanceConfig = buildInstanceConfig(localNode);

        // app instance
//        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
//        InstanceInfo instanceInfo = new InstanceInfoFactory().create(instanceConfig);

        // 2. Create ApplicationInfoManager with InstanceInfo
        applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);

        registerNode();

        // 3. Instantiate EurekaClient with ApplicationInfoManager and config
        EurekaClientConfig clientConfig = new DefaultEurekaClientConfig();
        DiscoveryClient discoveryClient = new DiscoveryClient(applicationInfoManager, clientConfig,
                Jersey3TransportClientFactories.getInstance());


        List<InstanceInfo> nodes = discoveryClient.getInstancesByVipAddress("test", false);
        System.out.println(nodes);

        // 5. Query Eureka for info about your app or others
        Applications applications = eurekaClient.getApplications();
        System.out.println("Number of registered applications: " + applications.getRegisteredApplications().size());
        // Sleep to keep app running to continue heartbeat renewals
        Thread.sleep(1000 * 60 * 10);
        // Clean up and unregister on shutdown
        eurekaClient.shutdown();
        System.out.println("Unregistered and shut down.");

        Thread.sleep(Long.MAX_VALUE);
    }


    // Optional: Register the instance explicitly (though the DiscoveryClient does this automatically on init)
    public static void registerNode() {
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
//        if (!skipEurekaRegistrationVerification) {
            verifyEurekaRegistration();
//        }
    }

    public static void discovery() {
        InstanceInfo instance = eurekaClient.getNextServerFromEureka("SOME-OTHER-SERVICE", false);
        System.out.println("Host: " + instance.getHostName() + ", port: " + instance.getPort());
    }

    static void verifyEurekaRegistration() {
        String applicationName = applicationInfoManager.getEurekaInstanceConfig().getAppname();
        Application application;
        do {
            try {
                System.out.println("Waiting for registration with Eureka...");
                application = eurekaClient.getApplication(applicationName);
                if (application != null) {
                    System.out.println("Registered in Eureka");
                    break;
                }
            } catch (Throwable t) {
                if (t instanceof Error) {
                    throw (Error) t;
                }
            }

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException almostIgnore) {
                Thread.currentThread().interrupt();
            }
        } while (true);
    }

    /*
    private EurekaInstanceConfig buildInstanceConfig(DiscoveryNode localNode) {
        try {
            String value;
            if (this.useClasspathEurekaClientProps) {
                String configProperty = DynamicPropertyFactory
                        .getInstance()
                        .getStringProperty("eureka.client.props", "eureka-client").get();

                String eurekaPropertyFile = String.format("%s.properties", configProperty);
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                URL url = loader.getResource(eurekaPropertyFile);
                if (url == null) {
                    throw new IllegalStateException("Cannot locate " + eurekaPropertyFile + " as a classpath resource.");
                }
                Properties props = new Properties();
                props.load(url.openStream());

                String key = String.format("%s.datacenter", this.namespace);
                value = props.getProperty(key, "");
            } else {
                value = String.valueOf(getProperties().get(DATACENTER.key()));
            }
            if ("cloud".equals(value.trim().toLowerCase())) {
                return new CloudInstanceConfig(this.namespace), localNode);
            }
            if (this.useClasspathEurekaClientProps) {
                return new MyDataCenterInstanceConfig(this.namespace), localNode);
            }
            return new MyDataCenterInstanceConfig(this.namespace), localNode, getAppname();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot build EurekaInstanceInfo", e);
        }
    }

     */




    public static void discoverNodes2() {
        String applicationName = applicationInfoManager.getEurekaInstanceConfig().getAppname();
        int NUM_RETRIES = 3;
        Application application = null;

        for (int i = 0; i < NUM_RETRIES; i++) {
            application = eurekaClient.getApplication(applicationName);
            if (application != null) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException almostIgnore) {
                Thread.currentThread().interrupt();
            }
        }

        if (application != null) {
            List<InstanceInfo> instances = application.getInstancesAsIsFromEureka();
            System.out.println(instances);
        }

    }
}