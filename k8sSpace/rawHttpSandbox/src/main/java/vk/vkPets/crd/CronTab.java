package vk.vkPets.crd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CronTab {
    private Metadata metadata;
    private CronTabSpec spec;

    // Getters and Setters
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public CronTabSpec getSpec() { return spec; }
    public void setSpec(CronTabSpec spec) { this.spec = spec; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
