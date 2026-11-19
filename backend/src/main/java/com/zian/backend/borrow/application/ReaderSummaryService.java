package com.zian.backend.borrow.application;

import com.zian.backend.borrow.domain.model.ReaderSummary;
import com.zian.backend.borrow.domain.repository.ReaderSummaryRepository;
import com.zian.backend.iam.application.ReaderCreditService;
import com.zian.backend.rule.application.AcademicCalendarService;
import com.zian.backend.rule.application.RuleService;
import com.zian.backend.shared.security.UserContext;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读者首页摘要：一次请求返回在借、临期、逾期、配额、本学期归还与信用档位。
 *
 * <p>边界固定为当前登录读者（{@link UserContext#requireId()}），请求参数里没有 readerId，
 * 因此不存在按 ID 读他人摘要的入口。
 */
@Service
@RequiredArgsConstructor
public class ReaderSummaryService {

    private static final String QUOTA_KEY = "BORROW.MAX_ACTIVE";
    private static final int DEFAULT_QUOTA = 5;
    private static final int DUE_SOON_DAYS = 3;

    private final ReaderSummaryRepository summaryRepository;
    private final ReaderCreditService readerCreditService;
    private final RuleService ruleService;
    private final AcademicCalendarService academicCalendarService;

    /**
     * 汇总当前读者的首页数据。
     *
     * @return 首页摘要
     */
    @Transactional(readOnly = true)
    public ReaderSummary summary() {
        long readerId = UserContext.requireId();
        LocalDate today = LocalDate.now();
        ReaderSummaryRepository.Counts counts = summaryRepository.counts(
                readerId, today, today.plusDays(DUE_SOON_DAYS), termStart(today).atStartOfDay());        int tier = readerCreditService.ktTier(readerId);
        return new ReaderSummary(
                (int) counts.borrowed(),
                (int) counts.dueIn3Days(),
                (int) counts.overdue(),
                ruleService.intValue(QUOTA_KEY, DEFAULT_QUOTA),
                (int) counts.returnedThisTerm(),
                tier,
                creditLabel(tier));
    }

    /**
     * 本学期起点：优先取校历 {@code NORMAL} 场景覆盖今天的区间起点；校历未维护时回退到学期边界近似。
     *
     * @param today 业务今天
     * @return 学期起点
     */
    LocalDate termStart(LocalDate today) {
        return academicCalendarService.semesterStart(today).orElseGet(() -> fallbackTermStart(today));
    }

    /**
     * 学期边界近似：9 月 1 日起算秋季学期、2 月 1 日起算春季学期，1 月沿用上一年秋季。
     *
     * @param today 业务今天
     * @return 近似学期起点
     */
    static LocalDate fallbackTermStart(LocalDate today) {
        // ponytail: 校历为空时的兜底；有 NORMAL 区间就以上面的 semesterStart 为准
        int month = today.getMonthValue();
        if (month >= 8) {
            return LocalDate.of(today.getYear(), 9, 1);
        }
        if (month == 1) {
            return LocalDate.of(today.getYear() - 1, 9, 1);
        }
        return LocalDate.of(today.getYear(), 2, 1);
    }

    /**
     * 信用档位展示名。
     *
     * @param tier 档位 0–3
     * @return 展示名；越界按最低档处理
     */
    static String creditLabel(int tier) {
        return switch (tier) {
            case 0 -> "优秀";
            case 1 -> "良好";
            case 2 -> "一般";
            default -> "受限";
        };
    }

}
