package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("cockpit_scene")
public class CockpitScene {
    @TableId
    private String id;
    private String name;
    private String sceneKey;
    private String cls;
    private String description;
    private Integer statStudents;
    private String statRate;
    private Integer statSupport;
    private Integer sortOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSceneKey() {
        return sceneKey;
    }

    public void setSceneKey(String sceneKey) {
        this.sceneKey = sceneKey;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatStudents() {
        return statStudents;
    }

    public void setStatStudents(Integer statStudents) {
        this.statStudents = statStudents;
    }

    public String getStatRate() {
        return statRate;
    }

    public void setStatRate(String statRate) {
        this.statRate = statRate;
    }

    public Integer getStatSupport() {
        return statSupport;
    }

    public void setStatSupport(Integer statSupport) {
        this.statSupport = statSupport;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
