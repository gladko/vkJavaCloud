package vk.vkPets.endpoints;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class K8sEndpointEvent {
    private String type; // ADDED, MODIFIED, DELETED
    private EndpointObject object;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public EndpointObject getObject() { return object; }
    public void setObject(EndpointObject object) { this.object = object; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EndpointObject {
        private Metadata metadata;
        private List<SubSet> subsets;

        public Metadata getMetadata() { return metadata; }
        public void setMetadata(Metadata metadata) { this.metadata = metadata; }

        public List<SubSet> getSubsets() { return subsets; }
        public void setSubsets(List<SubSet> subsets) { this.subsets = subsets; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String name; // This matches your Service Name
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubSet {
        private List<Address> addresses; // Healthy, ready pods
        private List<Port> ports;

        public List<Address> getAddresses() { return addresses; }
        public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

        public List<Port> getPorts() { return ports; }
        public void setPorts(List<Port> ports) { this.ports = ports; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String ip;
        private String nodeName;

        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }

        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Port {
        private int port;
        private String name;

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
