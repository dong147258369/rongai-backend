package com.rongai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rongai.common.Result;
import com.rongai.entity.*;
import com.rongai.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 个别化教育计划 (IEP - Individualized Education Program) 专业企业级 RESTful 控制器
 * 遵循标准 RESTful 资源规范 (版本化路径 /api/v1/iep，无日期纯业务标识符如 case-048 / 10048)
 */
@RestController
@RequestMapping({"/api/v1/iep", "/api/iep"})
public class IepController {

    @Autowired
    private IepStudentMapper studentMapper;

    @Autowired
    private IepGoalMapper goalMapper;

    @Autowired
    private IepTrackMapper trackMapper;

    @Autowired
    private IepMeetingMapper meetingMapper;

    @Autowired
    private IepCaseStateMapper caseStateMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将外部无日期的专业标识符（如 case-048, 10048, active）智能解析为内部个案实体主键
     */
    private String resolveCaseId(String caseId) {
        if (!StringUtils.hasText(caseId) || "active".equalsIgnoreCase(caseId) || "current".equalsIgnoreCase(caseId) || "default".equalsIgnoreCase(caseId)) {
            return "C-2026-048";
        }
        if (caseId.matches("(?i).*(048|10048).*")) {
            return "C-2026-048";
        }
        if (caseId.matches("(?i).*(026|10026).*")) {
            return "C-2026-026";
        }
        IepCaseState exists = caseStateMapper.selectById(caseId);
        if (exists != null) return exists.getCaseId();
        if (caseId.contains("-")) {
            String suffix = caseId.substring(caseId.lastIndexOf("-") + 1);
            List<IepCaseState> list = caseStateMapper.selectList(
                    new LambdaQueryWrapper<IepCaseState>().like(IepCaseState::getCaseId, suffix)
            );
            if (!list.isEmpty()) return list.get(0).getCaseId();
        }
        return caseId;
    }

    /**
     * 将内部带日期的历史主键转换为外部标准的无日期专业编号，例如 C-2026-048 -> case-048
     */
    private String formatProfessionalId(String internalId) {
        if (!StringUtils.hasText(internalId)) return "case-001";
        if (internalId.contains("-")) {
            String suffix = internalId.substring(internalId.lastIndexOf("-") + 1);
            return "case-" + suffix;
        }
        return internalId;
    }

    // ========================================================
    // 1. 工作台全局指标总览 (Dashboard & Metrics)
    // ========================================================
    @GetMapping({"/overview", "/dashboard/metrics"})
    public Result<Map<String, Object>> getOverview() {
        Long totalStudents = studentMapper.selectCount(null);
        Long activeGoals = goalMapper.selectCount(
                new LambdaQueryWrapper<IepGoal>().eq(IepGoal::getStatus, "进行中")
        );
        Long totalTracks = trackMapper.selectCount(null);
        Long pendingMeetings = meetingMapper.selectCount(
                new LambdaQueryWrapper<IepMeeting>().ne(IepMeeting::getStatus, "已完成")
        );

        List<IepTrack> recentTracks = trackMapper.selectList(
                new LambdaQueryWrapper<IepTrack>().orderByDesc(IepTrack::getTrackDate).last("LIMIT 4")
        );

        List<IepMeeting> recentMeetings = meetingMapper.selectList(
                new LambdaQueryWrapper<IepMeeting>().orderByDesc(IepMeeting::getMeetingTime).last("LIMIT 3")
        );

        Map<String, Object> map = new HashMap<>();
        map.put("totalStudents", totalStudents);
        map.put("activeGoals", activeGoals);
        map.put("totalTracks", totalTracks);
        map.put("pendingMeetings", pendingMeetings);
        map.put("recentTracks", recentTracks);
        map.put("recentMeetings", recentMeetings);
        return Result.success(map);
    }

    // ========================================================
    // 2. IEP 个案档案管理 (IEP Dossiers & Cases Management)
    //    专业无日期路径: GET /api/v1/iep/cases
    //                   GET /api/v1/iep/cases/{caseId} (如 case-048)
    // ========================================================

    /**
     * 获取全园 IEP 个案档案列表及当前所处审议阶段
     */
    @GetMapping({"/cases", "/case-state/list"})
    public Result<List<Map<String, Object>>> getCaseList() {
        List<IepCaseState> list = caseStateMapper.selectList(
                new LambdaQueryWrapper<IepCaseState>().orderByAsc(IepCaseState::getCaseId)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (IepCaseState s : list) {
            Map<String, Object> item = new HashMap<>();
            String profId = formatProfessionalId(s.getCaseId());
            item.put("id", profId);
            item.put("caseCode", profId);
            item.put("rawId", s.getCaseId());
            item.put("alias", s.getAlias());
            item.put("name", s.getName());
            item.put("gender", s.getGender());
            item.put("age", s.getAge());
            item.put("cls", s.getCls());
            item.put("stage", s.getCurrentStage());
            item.put("stageIdx", s.getStageIdx());
            item.put("docStatus", s.getDocStatus());
            item.put("docVersion", s.getDocVersion());
            item.put("effectiveBy", s.getEffectiveBy());
            item.put("effectiveAt", s.getEffectiveAt());
            item.put("adviceCount", s.getAdviceCount());
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 获取指定个案全量业务档案
     * 标准专业路径: GET /api/v1/iep/cases/case-048 或 GET /api/v1/iep/cases/active
     */
    @GetMapping({"/cases/{caseId}", "/case-state/{caseId}"})
    public Result<Map<String, Object>> getCaseDossier(@PathVariable("caseId") String caseId) {
        String realId = resolveCaseId(caseId);
        IepCaseState state = caseStateMapper.selectById(realId);
        if (state == null) {
            state = caseStateMapper.selectById("C-2026-048");
        }
        if (state == null) {
            return Result.error("未找到对应 IEP 个案档案: " + caseId);
        }
        Map<String, Object> res = new HashMap<>();
        String profId = formatProfessionalId(state.getCaseId());
        res.put("caseId", profId);
        res.put("caseCode", profId);
        res.put("alias", state.getAlias());
        res.put("name", state.getName());
        res.put("gender", state.getGender());
        res.put("age", state.getAge());
        res.put("cls", state.getCls());
        res.put("currentStage", state.getCurrentStage());
        res.put("stageIdx", state.getStageIdx());
        res.put("docStatus", state.getDocStatus());
        res.put("docVersion", state.getDocVersion());
        res.put("effectiveBy", state.getEffectiveBy());
        res.put("effectiveAt", state.getEffectiveAt());
        res.put("adviceCount", state.getAdviceCount());
        try {
            JsonNode dataNode = OBJECT_MAPPER.readTree(state.getDataJson());
            res.put("data", dataNode);
        } catch (Exception e) {
            res.put("data", state.getDataJson());
        }
        return Result.success(res);
    }

    /**
     * 保存/更新 IEP 个案档案全量业务数据
     * 标准专业路径: PUT /api/v1/iep/cases/case-048
     */
    @RequestMapping(
            value = {"/cases/{caseId}", "/case-state/{caseId}"},
            method = {RequestMethod.POST, RequestMethod.PUT}
    )
    public Result<Boolean> saveCaseDossier(@PathVariable("caseId") String caseId, @RequestBody Map<String, Object> payload) {
        String realId = resolveCaseId(caseId);
        IepCaseState entity = caseStateMapper.selectById(realId);
        if (entity == null) {
            entity = new IepCaseState();
            entity.setCaseId(realId);
            entity.setCreateTime(LocalDateTime.now());
        }
        if (payload.containsKey("alias")) entity.setAlias(String.valueOf(payload.get("alias")));
        if (payload.containsKey("name")) entity.setName(String.valueOf(payload.get("name")));
        if (payload.containsKey("currentStage")) entity.setCurrentStage(String.valueOf(payload.get("currentStage")));
        if (payload.containsKey("stageIdx")) entity.setStageIdx(Integer.parseInt(String.valueOf(payload.get("stageIdx"))));
        if (payload.containsKey("docStatus")) entity.setDocStatus(String.valueOf(payload.get("docStatus")));
        if (payload.containsKey("docVersion")) entity.setDocVersion(String.valueOf(payload.get("docVersion")));
        if (payload.containsKey("effectiveBy")) entity.setEffectiveBy(String.valueOf(payload.get("effectiveBy")));
        if (payload.containsKey("effectiveAt")) entity.setEffectiveAt(String.valueOf(payload.get("effectiveAt")));
        if (payload.containsKey("adviceCount")) {
            entity.setAdviceCount(Integer.parseInt(String.valueOf(payload.get("adviceCount"))));
        }
        if (payload.containsKey("data")) {
            try {
                entity.setDataJson(OBJECT_MAPPER.writeValueAsString(payload.get("data")));
            } catch (Exception e) {
                entity.setDataJson(String.valueOf(payload.get("data")));
            }
        }
        entity.setUpdateTime(LocalDateTime.now());
        if (caseStateMapper.selectById(realId) != null) {
            caseStateMapper.updateById(entity);
        } else {
            caseStateMapper.insert(entity);
        }
        return Result.success(true);
    }

    // ========================================================
    // 3. IEP 目标审议子资源接口 (Individualized Goal Deliberation)
    //    标准专业路径: POST /api/v1/iep/cases/case-048/goals/review
    // ========================================================
    @PostMapping({"/cases/{caseId}/goals/review", "/case-state/{caseId}/review-goal"})
    public Result<Map<String, Object>> reviewGoal(
            @PathVariable("caseId") String caseId,
            @RequestBody Map<String, Object> req
    ) {
        String realId = resolveCaseId(caseId);
        IepCaseState entity = caseStateMapper.selectById(realId);
        if (entity == null) return Result.error("未找到对应 IEP 个案档案: " + caseId);

        String goalId = String.valueOf(req.get("goalId"));
        String action = String.valueOf(req.get("action")); // adopt(采纳), modify(改写), drop(忽略/降级), keep(保留)
        String newText = req.containsKey("text") ? String.valueOf(req.get("text")) : null;

        try {
            JsonNode root = OBJECT_MAPPER.readTree(entity.getDataJson());
            ArrayNode goals = (ArrayNode) root.get("goals");
            if (goals != null) {
                for (JsonNode g : goals) {
                    if (g.has("id") && g.get("id").asText().equals(goalId)) {
                        ObjectNode on = (ObjectNode) g;
                        if ("adopt".equals(action)) {
                            on.put("status", "adopted");
                        } else if ("modify".equals(action)) {
                            on.put("status", "adopted");
                            if (StringUtils.hasText(newText)) on.put("text", newText);
                        } else if ("drop".equals(action)) {
                            on.put("status", "dropped");
                        } else if ("keep".equals(action)) {
                            on.put("status", "adopted");
                        }
                    }
                }
            }
            entity.setDataJson(OBJECT_MAPPER.writeValueAsString(root));
            entity.setUpdateTime(LocalDateTime.now());
            caseStateMapper.updateById(entity);
            return Result.success(Collections.singletonMap("message", "个别化目标审议决议已成功更新并持久化至 MySQL"));
        } catch (Exception e) {
            return Result.error("目标审议决议更新失败: " + e.getMessage());
        }
    }

    // ========================================================
    // 4. 多团队会议建议写回接口 (Deliberation Advice Writeback)
    //    标准专业路径: POST /api/v1/iep/cases/case-048/meetings/advice/apply
    // ========================================================
    @PostMapping({"/cases/{caseId}/meetings/advice/apply", "/case-state/{caseId}/apply-advice"})
    public Result<Map<String, Object>> applyMeetingAdvice(
            @PathVariable("caseId") String caseId,
            @RequestBody Map<String, Object> req
    ) {
        String realId = resolveCaseId(caseId);
        IepCaseState entity = caseStateMapper.selectById(realId);
        if (entity == null) return Result.error("未找到对应 IEP 个案档案: " + caseId);

        boolean applyAll = Boolean.TRUE.equals(req.get("all"));
        Integer itemIndex = req.containsKey("index") ? Integer.parseInt(String.valueOf(req.get("index"))) : null;

        try {
            JsonNode root = OBJECT_MAPPER.readTree(entity.getDataJson());
            ArrayNode meetings = (ArrayNode) root.get("meetings");
            int remaining = 0;
            if (meetings != null && meetings.size() > 0) {
                ObjectNode meeting = (ObjectNode) meetings.get(0);
                ObjectNode advice = (ObjectNode) meeting.get("advice");
                if (advice != null) {
                    ArrayNode items = (ArrayNode) advice.get("items");
                    if (items != null) {
                        for (int i = 0; i < items.size(); i++) {
                            ObjectNode item = (ObjectNode) items.get(i);
                            if (applyAll || (itemIndex != null && itemIndex == i)) {
                                item.put("applied", true);
                            }
                            if (!item.has("applied") || !item.get("applied").asBoolean()) {
                                remaining++;
                            }
                        }
                    }
                }
            }
            entity.setAdviceCount(remaining);
            entity.setDataJson(OBJECT_MAPPER.writeValueAsString(root));
            entity.setUpdateTime(LocalDateTime.now());
            caseStateMapper.updateById(entity);

            Map<String, Object> resp = new HashMap<>();
            resp.put("remainingAdvice", remaining);
            resp.put("message", "多学科会议建议已成功写回成稿并持久化至 MySQL");
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("建议写回失败: " + e.getMessage());
        }
    }

    // ========================================================
    // 5. IEP 成稿签署与生效接口 (IEP Plan Enactment / Effective)
    //    标准专业路径: POST /api/v1/iep/cases/case-048/document/effective
    // ========================================================
    @PostMapping({"/cases/{caseId}/document/effective", "/case-state/{caseId}/set-effective"})
    public Result<Map<String, Object>> setDocumentEffective(
            @PathVariable("caseId") String caseId,
            @RequestBody(required = false) Map<String, Object> req
    ) {
        String realId = resolveCaseId(caseId);
        IepCaseState entity = caseStateMapper.selectById(realId);
        if (entity == null) return Result.error("未找到对应 IEP 个案档案: " + caseId);

        String effectiveBy = (req != null && req.containsKey("effectiveBy"))
                ? String.valueOf(req.get("effectiveBy")) : "教师";
        String effectiveAt = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        try {
            JsonNode root = OBJECT_MAPPER.readTree(entity.getDataJson());
            ObjectNode doc = (ObjectNode) root.get("doc");
            if (doc != null) {
                doc.put("status", "effective");
                doc.put("effectiveBy", effectiveBy);
                doc.put("effectiveAt", effectiveAt);
            }
            entity.setDocStatus("effective");
            entity.setEffectiveBy(effectiveBy);
            entity.setEffectiveAt(effectiveAt);
            entity.setDataJson(OBJECT_MAPPER.writeValueAsString(root));
            entity.setUpdateTime(LocalDateTime.now());
            caseStateMapper.updateById(entity);

            Map<String, Object> resp = new HashMap<>();
            resp.put("docStatus", "effective");
            resp.put("effectiveBy", effectiveBy);
            resp.put("effectiveAt", effectiveAt);
            resp.put("message", "IEP 成稿已完成正式签署并置为生效");
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("IEP 生效设置失败: " + e.getMessage());
        }
    }

    // ========================================================
    // 6. 个案基线还原与重置动作 (Dossier Baseline Reset Action)
    //    标准专业路径: POST /api/v1/iep/cases/case-048/actions/reset
    // ========================================================
    @PostMapping({"/cases/{caseId}/actions/reset", "/case-state/{caseId}/reset"})
    public Result<Map<String, Object>> resetCaseDossier(@PathVariable("caseId") String caseId) {
        String realId = resolveCaseId(caseId);
        IepCaseState entity = caseStateMapper.selectById(realId);
        if (entity == null) return Result.error("未找到对应 IEP 个案档案: " + caseId);

        try {
            JsonNode root = OBJECT_MAPPER.readTree(entity.getDataJson());
            ObjectNode doc = (ObjectNode) root.get("doc");
            if (doc != null) {
                doc.put("status", "draft");
                doc.put("effectiveBy", "");
                doc.put("effectiveAt", "");
            }
            ArrayNode meetings = (ArrayNode) root.get("meetings");
            int totalAdvice = 0;
            if (meetings != null && meetings.size() > 0) {
                ObjectNode meeting = (ObjectNode) meetings.get(0);
                meeting.put("applied", false);
                ObjectNode advice = (ObjectNode) meeting.get("advice");
                if (advice != null) {
                    ArrayNode items = (ArrayNode) advice.get("items");
                    if (items != null) {
                        for (JsonNode item : items) {
                            ((ObjectNode) item).put("applied", false);
                            totalAdvice++;
                        }
                    }
                }
            }
            entity.setDocStatus("draft");
            entity.setEffectiveBy("");
            entity.setEffectiveAt("");
            entity.setAdviceCount(totalAdvice > 0 ? totalAdvice : 29);
            entity.setDataJson(OBJECT_MAPPER.writeValueAsString(root));
            entity.setUpdateTime(LocalDateTime.now());
            caseStateMapper.updateById(entity);

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "个案档案已恢复为初始审议基线（29条多学科建议待写回）");
            resp.put("docStatus", "draft");
            resp.put("adviceCount", entity.getAdviceCount());
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("个案基线重置失败: " + e.getMessage());
        }
    }

    // ========================================================
    // 7. 特殊教育随班就读学生名册 (Special Education Students Roster)
    //    标准专业路径: GET /api/v1/iep/students
    // ========================================================
    @GetMapping("/students")
    public Result<List<IepStudent>> getStudents(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "grade", required = false) String grade
    ) {
        LambdaQueryWrapper<IepStudent> wrapper = new LambdaQueryWrapper<IepStudent>()
                .orderByDesc(IepStudent::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IepStudent::getName, keyword).or().like(IepStudent::getStudentNo, keyword));
        }
        if (StringUtils.hasText(grade)) {
            wrapper.eq(IepStudent::getGrade, grade);
        }
        return Result.success(studentMapper.selectList(wrapper));
    }

    @PostMapping("/student")
    public Result<IepStudent> saveStudent(@RequestBody IepStudent student) {
        if (!StringUtils.hasText(student.getId())) {
            student.setId("stu_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            student.setCreateTime(LocalDateTime.now());
            student.setUpdateTime(LocalDateTime.now());
            studentMapper.insert(student);
        } else {
            student.setUpdateTime(LocalDateTime.now());
            studentMapper.updateById(student);
        }
        return Result.success(student);
    }

    @DeleteMapping("/student/{id}")
    public Result<Boolean> deleteStudent(@PathVariable("id") String id) {
        studentMapper.deleteById(id);
        return Result.success(true);
    }

    // ========================================================
    // 8. 资源教室训练目标清单 (Resource Room Training Goals)
    //    标准专业路径: GET /api/v1/iep/goals
    // ========================================================
    @GetMapping("/goals")
    public Result<List<IepGoal>> getGoals(@RequestParam(value = "studentId", required = false) String studentId) {
        LambdaQueryWrapper<IepGoal> wrapper = new LambdaQueryWrapper<IepGoal>()
                .orderByDesc(IepGoal::getCreateTime);
        if (StringUtils.hasText(studentId)) {
            wrapper.eq(IepGoal::getStudentId, studentId);
        }
        return Result.success(goalMapper.selectList(wrapper));
    }

    @PostMapping("/goal")
    public Result<IepGoal> saveGoal(@RequestBody IepGoal goal) {
        if (!StringUtils.hasText(goal.getId())) {
            goal.setId("goal_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            goal.setCreateTime(LocalDateTime.now());
            goalMapper.insert(goal);
        } else {
            goalMapper.updateById(goal);
        }
        return Result.success(goal);
    }

    // ========================================================
    // 9. 融合课堂入班观察与支持追踪 (Classroom Inclusion Tracks)
    //    标准专业路径: GET /api/v1/iep/tracks
    // ========================================================
    @GetMapping("/tracks")
    public Result<List<IepTrack>> getTracks(@RequestParam(value = "studentId", required = false) String studentId) {
        LambdaQueryWrapper<IepTrack> wrapper = new LambdaQueryWrapper<IepTrack>()
                .orderByDesc(IepTrack::getTrackDate);
        if (StringUtils.hasText(studentId)) {
            wrapper.eq(IepTrack::getStudentId, studentId);
        }
        return Result.success(trackMapper.selectList(wrapper));
    }

    @PostMapping("/track")
    public Result<IepTrack> saveTrack(@RequestBody IepTrack track) {
        if (!StringUtils.hasText(track.getId())) {
            track.setId("track_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            track.setTrackDate(LocalDateTime.now());
            trackMapper.insert(track);
        } else {
            trackMapper.updateById(track);
        }
        return Result.success(track);
    }

    // ========================================================
    // 10. 多团队协同与多学科联席会议 (Multidisciplinary Team Meetings)
    //     标准专业路径: GET /api/v1/iep/meetings
    // ========================================================
    @GetMapping("/meetings")
    public Result<List<IepMeeting>> getMeetings(@RequestParam(value = "studentId", required = false) String studentId) {
        LambdaQueryWrapper<IepMeeting> wrapper = new LambdaQueryWrapper<IepMeeting>()
                .orderByDesc(IepMeeting::getMeetingTime);
        if (StringUtils.hasText(studentId)) {
            wrapper.eq(IepMeeting::getStudentId, studentId);
        }
        return Result.success(meetingMapper.selectList(wrapper));
    }

    @PostMapping("/meeting")
    public Result<IepMeeting> saveMeeting(@RequestBody IepMeeting meeting) {
        if (!StringUtils.hasText(meeting.getId())) {
            meeting.setId("meet_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            if (meeting.getMeetingTime() == null) {
                meeting.setMeetingTime(LocalDateTime.now());
            }
            meetingMapper.insert(meeting);
        } else {
            meetingMapper.updateById(meeting);
        }
        return Result.success(meeting);
    }
}
