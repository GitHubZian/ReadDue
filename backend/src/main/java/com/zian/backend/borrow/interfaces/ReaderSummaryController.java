package com.zian.backend.borrow.interfaces;

import com.zian.backend.borrow.application.ReaderSummaryService;
import com.zian.backend.borrow.domain.model.ReaderSummary;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者个人中心接口：首页摘要。
 */
@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
public class ReaderSummaryController {

    private final ReaderSummaryService readerSummaryService;

    /**
     * 当前读者的首页摘要：在借/三天内到期/逾期/配额/本学期归还/信用档位。
     *
     * @return 摘要视图；数值字段均为整数，信用档位附展示名
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('borrow:read:self')")
    public BaseResponse<SummaryView> summary() {
        return ResultUtils.success(SummaryView.of(readerSummaryService.summary()));
    }

    /**
     * 首页摘要视图。
     *
     * @param borrowed         在借册数
     * @param dueIn3Days       未来 3 天内到期册数
     * @param overdue          逾期未还册数
     * @param quota            同时在借上限
     * @param returnedThisTerm 本学期已归还册数
     * @param creditLevel      信用档位 0–3
     * @param creditLabel      档位展示名
     */
    public record SummaryView(
            int borrowed,
            int dueIn3Days,
            int overdue,
            int quota,
            int returnedThisTerm,
            int creditLevel,
            String creditLabel) {

        /**
         * 把领域摘要转成接口视图。
         *
         * @param summary 领域摘要
         * @return 视图
         */
        static SummaryView of(ReaderSummary summary) {
            return new SummaryView(
                    summary.borrowed(),
                    summary.dueIn3Days(),
                    summary.overdue(),
                    summary.quota(),
                    summary.returnedThisTerm(),
                    summary.creditLevel(),
                    summary.creditLabel());
        }
    }
}
