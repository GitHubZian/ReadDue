package com.zian.backend.appeal.interfaces;

import com.zian.backend.appeal.application.AppealService;
import com.zian.backend.appeal.interfaces.dto.AppealDtos;
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
 * 读者端申诉接口。
 */
@RestController
@RequestMapping("/v1/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    /**
     * 提交申诉：校验申诉对象归属后建单，SLA 自提交时刻起算。
     *
     * @param request 申诉类型、对象 ID 与理由
     * @return 工单编号、状态与 SLA 截止时间
     */
    @PostMapping
    @PreAuthorize("hasAuthority('appeal:create')")
    public BaseResponse<AppealDtos.MyTicketView> create(
            @Valid @RequestBody AppealDtos.CreateRequest request) {
        return ResultUtils.success(appealService.create(request));
    }

    /**
     * 查询当前读者的申诉工单列表。
     *
     * @param status 状态过滤（PENDING/PROCESSING/RESOLVED/REJECTED）
     * @param page   页码，从 1 开始
     * @param size   每页条数
     * @param sort   排序参数，当前仅接收不参与排序
     * @return 分页后的工单
     */
    @GetMapping("/self")
    @PreAuthorize("hasAuthority('appeal:create')")
    public BaseResponse<PageResult<AppealDtos.MyTicketView>> myTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(appealService.myTickets(status, PageQuery.of(page, size, sort)));
    }
}
