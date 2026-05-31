package com.helen.eduedu.controller;

import com.helen.eduedu.common.R;
import com.helen.eduedu.security.RequireRole;
import com.helen.eduedu.service.DashboardService;
import com.helen.eduedu.vo.ClassRankVO;
import com.helen.eduedu.vo.DashboardOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据看板控制器
 */
@Tag(name = "数据看板")
@RestController
@RequestMapping("/api/dashboard")
@RequireRole({3})
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "总览数据")
    @GetMapping("/overview")
    public R<DashboardOverviewVO> getOverview() {
        return R.ok(dashboardService.getOverview());
    }

    @Operation(summary = "班级排行")
    @GetMapping("/class-rank")
    public R<List<ClassRankVO>> getClassRank() {
        return R.ok(dashboardService.getClassRank());
    }
}
