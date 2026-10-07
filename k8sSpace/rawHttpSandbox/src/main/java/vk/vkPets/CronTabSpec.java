package vk.vkPets;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CronTabSpec {
    private String cronStmt;
    private String iconPath;

    // Getters and Setters
    public String getCronStmt() { return cronStmt; }
    public void setCronStmt(String cronStmt) { this.cronStmt = cronStmt; }

    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }
}
