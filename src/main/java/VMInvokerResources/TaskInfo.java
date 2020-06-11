package VMInvokerResources;

import java.util.List;

public class TaskInfo {

    private int id;
    private String taskName;
    private String taskFilePath;
    private List<String> metaDataNames;
    private List<String> metaDataFilePaths;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskFilePath() {
        return taskFilePath;
    }

    public void setTaskFilePath(String taskFilePath) {
        this.taskFilePath = taskFilePath;
    }

    public List<String> getMetaDataNames() {
        return metaDataNames;
    }

    public void setMetaDataNames(List<String> metaDataNames) {
        this.metaDataNames = metaDataNames;
    }

    public List<String> getMetaDataFilePaths() {
        return metaDataFilePaths;
    }

    public void setMetaDataFilePaths(List<String> metaDataFilePaths) {
        this.metaDataFilePaths = metaDataFilePaths;
    }

    @Override
    public String toString() {
        return "\nID: " + id +
                "\nTask name: " + taskName +
                "\nTask file path: " + taskFilePath +
                "\nMeta data names: " + getMetaDataNamesAsString() +
                "\nMeta data file paths: " + getMetaDataFilePathsAsString();
    }

    private String getMetaDataNamesAsString() {
        String metaDataNamesAsString = "";
        if (metaDataNames != null) {
            for (String name : metaDataNames) {
                metaDataNamesAsString = metaDataNamesAsString + "\n\t" + name;
            }
        }
        return metaDataNamesAsString;
    }

    private String getMetaDataFilePathsAsString() {
        String metaDataFilePathsAsString = "";
        if (metaDataFilePaths != null) {
            for (String path : metaDataFilePaths) {
                metaDataFilePathsAsString = metaDataFilePathsAsString + "\n\t" + path;
            }
        }
        return metaDataFilePathsAsString;
    }

}
