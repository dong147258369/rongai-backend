package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iep_goal")
public class IepGoal {
    @TableId
    private String id;
    private String studentId;
    private String studentName;
    private String domain;
    private String goalType;
    private String title;
    private String targetStandard;
    private Integer progress;
    private String status;
    private String trainerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
