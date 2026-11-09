package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
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
}
