package vk.vkPets.discovery;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record K8sEndpointEvent(
        String type,          // ADDED, MODIFIED, DELETED
        EndpointObject object)
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EndpointObject(
            Metadata metadata,
            List<SubSet> subsets
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metadata(
            String name       // This matches your Service Name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubSet(
            List<Address> addresses, // Healthy, ready pods
            List<Port> ports
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
            String ip,
            String nodeName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Port(
            int port,
            String name
    ) {}
}
