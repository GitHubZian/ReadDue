package com.zian.backend.reservation.interfaces;

import com.zian.backend.reservation.application.ReservationService;
import com.zian.backend.reservation.domain.model.Reservation;
import com.zian.backend.reservation.interfaces.dto.ReservationDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.PageQuery;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.ResultUtils;
import com.zian.backend.shared.common.Times;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者端预约接口。
 */
@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 创建预约：无可借副本且无重复预约时进入排队。
     *
     * @param request 书目 ID
     * @return 预约记录（含排队序号）
     */
    @PostMapping
    @PreAuthorize("hasAuthority('reservation:create')")
    public BaseResponse<ReservationDtos.ReservationItem> create(
            @Valid @RequestBody ReservationDtos.CreateRequest request) {
        Reservation reservation = reservationService.create(request.bookId());
        return ResultUtils.success(new ReservationDtos.ReservationItem(
                reservation.getId(),
                reservation.getBookId(),
                null,
                null,
                reservation.getQueueNo(),
                reservation.getStatus().name(),
                Times.iso(reservation.getReadyAt()),
                Times.iso(reservation.getExpiresAt())));
    }

    /**
     * 查询当前读者的预约。
     *
     * @param status 状态过滤（WAITING/READY/FULFILLED/EXPIRED/CANCELLED）
     * @param page   页码
     * @param size   每页条数
     * @return 分页后的预约
     */
    @GetMapping("/self")
    @PreAuthorize("hasAuthority('reservation:create')")
    public BaseResponse<PageResult<ReservationDtos.ReservationItem>> myReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(
                reservationService.myReservations(status, PageQuery.of(page, size)));
    }

    /**
     * 取消本人的预约。
     *
     * @param id 预约 ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('reservation:create')")
    public BaseResponse<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResultUtils.success(null);
    }
}
