package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iep_track")
public class IepTrack {
    @TableId
    private String id;
    private String studentId;
    private String studentName;
    private String courseName;
    private Integer focusRate;
    private Integer participateRate;
    private String supportStrategy;
    private String behaviorRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime trackDate;

    private String observerName;
    private String status;
}
