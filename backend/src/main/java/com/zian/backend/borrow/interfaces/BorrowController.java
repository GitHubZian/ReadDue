package com.zian.backend.borrow.interfaces;

import com.zian.backend.borrow.application.BorrowService;
import com.zian.backend.borrow.interfaces.dto.BorrowDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 借还台写接口。
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /**
     * 办理借出：校验读者状态与副本可借性，生成借阅单并把副本置为在借。
     *
     * @param request 读者学号与副本条码
     * @return 借阅单号、读者与书目信息、应还日期
     */
    @PostMapping("/borrows")
    @PreAuthorize("hasAuthority('borrow:create')")
    public BaseResponse<BorrowDtos.BorrowResult> borrow(@RequestBody @Valid BorrowDtos.BorrowRequest request) {
        return ResultUtils.success(borrowService.borrow(request));
    }

    /**
     * 办理归还：结束借阅单、恢复副本状态，并按逾期天数计算罚金。
     *
     * @param request 副本条码
     * @return 书目、读者、罚金与逾期天数
     */
    @PostMapping("/returns")
    @PreAuthorize("hasAuthority('borrow:return')")
    public BaseResponse<BorrowDtos.ReturnResult> returnBook(@RequestBody @Valid BorrowDtos.ReturnRequest request) {
        return ResultUtils.success(borrowService.returnBook(request));
    }
}
