package vk.vkPets.xdiscovery;


import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("details")
public class NodeDetails {
    private String description;

    public NodeDetails() {
        this("");
    }

    public NodeDetails(String description) {
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
