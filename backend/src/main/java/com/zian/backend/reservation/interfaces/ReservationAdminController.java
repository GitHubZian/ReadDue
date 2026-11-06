package com.zian.backend.reservation.interfaces;

import com.zian.backend.reservation.application.ReservationService;
import com.zian.backend.reservation.interfaces.dto.ReservationActionView;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预约管理写接口。
 */
@RestController
@RequestMapping("/v1/admin/reservations")
@RequiredArgsConstructor
public class ReservationAdminController {

    private final ReservationService reservationService;

    @PostMapping("/{id}/promote")
    @PreAuthorize("hasAuthority('reservation:manage')")
    public BaseResponse<ReservationActionView> promote(@PathVariable Long id) {
        return ResultUtils.success(ReservationActionView.of(reservationService.promote(id)));
    }

    @PostMapping("/{id}/expire")
    @PreAuthorize("hasAuthority('reservation:manage')")
    public BaseResponse<ReservationActionView> expire(@PathVariable Long id) {
        return ResultUtils.success(ReservationActionView.of(reservationService.expire(id)));
    }
}
