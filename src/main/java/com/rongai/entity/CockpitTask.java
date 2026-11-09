package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cockpit_task")
public class CockpitTask {
    @TableId
    private String id;
    private String lineId;
    private String icon;
    private String title;
    private String subTitle;
    private String badgeText;
    private String badgeStatus;
    private String targetRoute;
    private Integer sortOrder;
}
