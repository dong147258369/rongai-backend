package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.List;

@TableName("cockpit_line")
public class CockpitLine {
    @TableId
    private String id;
    private String sceneKey;
    private String lineNo;
    private String name;
    private String description;
    private String iconType;
    private String targetRoute;
    private Integer activeStep;
    private String mainBtnText;
    private Integer sortOrder;

    @TableField(exist = false)
    private List<CockpitTask> tasks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSceneKey() {
        return sceneKey;
    }

    public void setSceneKey(String sceneKey) {
        this.sceneKey = sceneKey;
    }

    public String getLineNo() {
        return lineNo;
    }

    public void setLineNo(String lineNo) {
        this.lineNo = lineNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public String getTargetRoute() {
        return targetRoute;
    }

    public void setTargetRoute(String targetRoute) {
        this.targetRoute = targetRoute;
    }

    public Integer getActiveStep() {
        return activeStep;
    }

    public void setActiveStep(Integer activeStep) {
        this.activeStep = activeStep;
    }

    public String getMainBtnText() {
        return mainBtnText;
    }

    public void setMainBtnText(String mainBtnText) {
        this.mainBtnText = mainBtnText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<CockpitTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<CockpitTask> tasks) {
        this.tasks = tasks;
    }
}
