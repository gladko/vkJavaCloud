package vk.vkPets.fabric8;

public class CronTabSpec {
    private String cronStmt;
    private String iconPath;
    private Integer replicas;

    public String getCronStmt() {
        return cronStmt;
    }

    public void setCronStmt(String cronStmt) {
        this.cronStmt = cronStmt;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }
}
