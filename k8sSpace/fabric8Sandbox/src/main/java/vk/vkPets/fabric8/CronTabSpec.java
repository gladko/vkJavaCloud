package vk.vkPets.fabric8;

public class CronTabSpec {
    private String cronStmt;
    private String iconPath;

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
}
