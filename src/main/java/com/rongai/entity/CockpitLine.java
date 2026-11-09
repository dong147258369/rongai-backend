package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.List;

@Data
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
}
