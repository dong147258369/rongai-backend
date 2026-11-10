package com.rongai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getFocusRate() {
        return focusRate;
    }

    public void setFocusRate(Integer focusRate) {
        this.focusRate = focusRate;
    }

    public Integer getParticipateRate() {
        return participateRate;
    }

    public void setParticipateRate(Integer participateRate) {
        this.participateRate = participateRate;
    }

    public String getSupportStrategy() {
        return supportStrategy;
    }

    public void setSupportStrategy(String supportStrategy) {
        this.supportStrategy = supportStrategy;
    }

    public String getBehaviorRemark() {
        return behaviorRemark;
    }

    public void setBehaviorRemark(String behaviorRemark) {
        this.behaviorRemark = behaviorRemark;
    }

    public LocalDateTime getTrackDate() {
        return trackDate;
    }

    public void setTrackDate(LocalDateTime trackDate) {
        this.trackDate = trackDate;
    }

    public String getObserverName() {
        return observerName;
    }

    public void setObserverName(String observerName) {
        this.observerName = observerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
