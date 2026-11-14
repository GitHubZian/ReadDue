package com.zian.backend.borrow.interfaces;

import com.zian.backend.borrow.application.BorrowService;
import com.zian.backend.borrow.interfaces.dto.BorrowDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者端借阅接口：我的借阅、续借。
 */
@RestController
@RequestMapping("/v1/borrows")
@RequiredArgsConstructor
public class ReaderBorrowController {

    private final BorrowService borrowService;

    /**
     * 查询当前读者自己的借阅记录，按借出时间倒序。
     *
     * @param status 状态过滤（BORROWED/OVERDUE/RETURNED，空表示全部）
     * @param page   页码，从 1 开始
     * @param size   每页条数，默认 20，上限 200
     * @param sort   排序参数，当前仅接收不参与排序
     * @return 分页后的借阅记录
     */
    @GetMapping("/self")
    @PreAuthorize("hasAnyAuthority('borrow:read:self','borrow:read:all')")
    public BaseResponse<PageResult<BorrowDtos.BorrowItem>> myBorrows(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(borrowService.myBorrows(status, PageQuery.of(page, size, sort)));
    }

    /**
     * 续借：校验续借次数、预约队列与逾期状态，成功后顺延应还日期。
     *
     * @param id 借阅单 ID
     * @return 新的应还日期、有效应还日期与已续借次数
     */
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyAuthority('borrow:renew','borrow:read:all')")
    public BaseResponse<BorrowDtos.RenewResult> renew(@PathVariable Long id) {
        return ResultUtils.success(borrowService.renew(id));
    }
}
