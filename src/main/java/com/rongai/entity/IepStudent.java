package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iep_student")
public class IepStudent {
    @TableId
    private String id;
    private String studentNo;
    private String name;
    private String gender;
    private Integer age;
    private String grade;
    private String specialType;
    private String supportLevel;
    private String status;
    private String avatar;
    private String guardianPhone;
    private String teacherName;
    private String iepPeriod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
