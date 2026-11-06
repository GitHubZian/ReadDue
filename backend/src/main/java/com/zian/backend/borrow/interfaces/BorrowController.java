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

    @PostMapping("/borrows")
    @PreAuthorize("hasAuthority('borrow:create')")
    public BaseResponse<BorrowDtos.BorrowResult> borrow(@RequestBody @Valid BorrowDtos.BorrowRequest request) {
        return ResultUtils.success(borrowService.borrow(request));
    }

    @PostMapping("/returns")
    @PreAuthorize("hasAuthority('borrow:return')")
    public BaseResponse<BorrowDtos.ReturnResult> returnBook(@RequestBody @Valid BorrowDtos.ReturnRequest request) {
        return ResultUtils.success(borrowService.returnBook(request));
    }
}
