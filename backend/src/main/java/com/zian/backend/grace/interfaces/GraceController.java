package com.zian.backend.grace.interfaces;

import com.zian.backend.grace.application.GraceService;
import com.zian.backend.grace.interfaces.dto.GraceDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.ResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者端宽限申请接口。
 */
@RestController
@RequestMapping("/v1/graces")
@RequiredArgsConstructor
public class GraceController {

    private final GraceService graceService;

    /**
     * 申请宽限：按场景、信用档位与预约队列计算建议天数，生成待审批单。
     *
     * @param request 借阅单 ID、场景与申请说明
     * @return 申请单号、计算天数、状态与快照过期时间
     */
    @PostMapping
    @PreAuthorize("hasAuthority('grace:apply')")
    public BaseResponse<GraceDtos.ApplicationView> apply(
            @Valid @RequestBody GraceDtos.ApplyRequest request) {
        return ResultUtils.success(graceService.apply(request));
    }

    /**
     * 查询当前读者的宽限申请。
     *
     * @param status 状态过滤（PENDING/APPROVED/REJECTED/EXPIRED）
     * @param page   页码
     * @param size   每页条数
     * @return 分页后的申请
     */
    @GetMapping("/self")
    @PreAuthorize("hasAuthority('grace:apply')")
    public BaseResponse<PageResult<GraceDtos.ApplicationView>> myApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(graceService.myApplications(status, PageQuery.of(page, size)));
    }
}
