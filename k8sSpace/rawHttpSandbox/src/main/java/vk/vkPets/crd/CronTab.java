package vk.vkPets.crd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CronTab {
    private Metadata metadata;
    private Spec spec;

    // Getters and Setters
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public Spec getSpec() { return spec; }
    public void setSpec(Spec spec) { this.spec = spec; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Spec {
        private String cronStmt;
        private String iconPath;

        // Getters and Setters
        public String getCronStmt() { return cronStmt; }
        public void setCronStmt(String cronStmt) { this.cronStmt = cronStmt; }

        public String getIconPath() { return iconPath; }
        public void setIconPath(String iconPath) { this.iconPath = iconPath; }
    }
}
