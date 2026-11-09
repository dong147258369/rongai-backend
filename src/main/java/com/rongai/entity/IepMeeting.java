package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("iep_meeting")
public class IepMeeting {
    @TableId
    private String id;
    private String studentId;
    private String studentName;
    private String title;
    private String meetingType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime meetingTime;

    private String participants;
    private String keyDecisions;
    private String nextActions;
    private String status;
}
