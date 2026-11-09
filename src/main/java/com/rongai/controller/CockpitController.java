package com.rongai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongai.common.Result;
import com.rongai.entity.CockpitLine;
import com.rongai.entity.CockpitScene;
import com.rongai.entity.CockpitTask;
import com.rongai.mapper.CockpitLineMapper;
import com.rongai.mapper.CockpitSceneMapper;
import com.rongai.mapper.CockpitTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 融AI 数字驾驶舱控制器
 * 支持专业 RESTful 路径: /api/v1/cockpit/** 与 /api/cockpit/**
 */
@RestController
@RequestMapping({"/api/v1/cockpit", "/api/cockpit"})
public class CockpitController {

    @Autowired
    private CockpitSceneMapper sceneMapper;

    @Autowired
    private CockpitLineMapper lineMapper;

    @Autowired
    private CockpitTaskMapper taskMapper;

    @GetMapping({"/scenes", "/scenes/list"})
    public Result<List<CockpitScene>> getScenes() {
        List<CockpitScene> scenes = sceneMapper.selectList(
                new LambdaQueryWrapper<CockpitScene>().orderByAsc(CockpitScene::getSortOrder)
        );
        return Result.success(scenes);
    }

    @GetMapping({"/scene/{sceneKey}", "/scenes/{sceneKey}"})
    public Result<Map<String, Object>> getSceneDetail(@PathVariable("sceneKey") String sceneKey) {
        CockpitScene scene = sceneMapper.selectOne(
                new LambdaQueryWrapper<CockpitScene>().eq(CockpitScene::getSceneKey, sceneKey)
        );
        if (scene == null) {
            return Result.error("未找到对应场景信息: " + sceneKey);
        }

        List<CockpitLine> lines = lineMapper.selectList(
                new LambdaQueryWrapper<CockpitLine>()
                        .eq(CockpitLine::getSceneKey, sceneKey)
                        .orderByAsc(CockpitLine::getSortOrder)
        );

        for (CockpitLine line : lines) {
            List<CockpitTask> tasks = taskMapper.selectList(
                    new LambdaQueryWrapper<CockpitTask>()
                            .eq(CockpitTask::getLineId, line.getId())
                            .orderByAsc(CockpitTask::getSortOrder)
            );
            line.setTasks(tasks);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("scene", scene);
        result.put("lines", lines);
        return Result.success(result);
    }
}
