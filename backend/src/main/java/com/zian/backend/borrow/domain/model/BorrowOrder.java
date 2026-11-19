package com.zian.backend.borrow.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 借阅单聚合：借出、续借、宽限生效、归还的规则都在这里收口。
 *
 * <p>不变量：续借次数 ≤ maxRenew；有效应还 = dueAt + graceDays；同一副本同时只能有一条在借单（由仓储保证）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowOrder {

    private Long id;
    private String orderNo;
    private Long readerId;
    private Long bookId;
    private Long copyId;
    private LocalDateTime borrowedAt;
    private LocalDate dueAt;
    private LocalDate effectiveDueAt;
    private int graceDays;
    private int renewCount;
    private LocalDateTime returnedAt;
    private int finalOverdueDays;
    private BorrowStatus status;
    private ReturnCondition returnCondition;
    private Integer version;

    /**
     * 创建在借单：应还日 = 借出日 + 借期，有效应还日同日。
     *
     * @param orderNo    借阅单号
     * @param readerId   读者 ID
     * @param bookId     书目 ID
     * @param copyId     副本 ID
     * @param borrowedAt 借出时间
     * @param loanDays   借期天数
     * @return 新建的在借单
     */
    public static BorrowOrder open(
            String orderNo, Long readerId, Long bookId, Long copyId, LocalDateTime borrowedAt, int loanDays) {
        LocalDate due = borrowedAt.toLocalDate().plusDays(loanDays);
        return BorrowOrder.builder()
                .orderNo(orderNo)
                .readerId(readerId)
                .bookId(bookId)
                .copyId(copyId)
                .borrowedAt(borrowedAt)
                .dueAt(due)
                .effectiveDueAt(due)
                .graceDays(0)
                .renewCount(0)
                .finalOverdueDays(0)
                .status(BorrowStatus.BORROWED)
                .build();
    }

    /**
     * 是否未结单（BORROWED / OVERDUE）。
     */
    public boolean isActive() {
        return status != null && status.isActive();
    }

    /**
     * 按有效应还日计算逾期天数，未到期返回 0。
     *
     * @param today 当天日期
     * @return 逾期天数
     */
    public int overdueDays(LocalDate today) {
        if (effectiveDueAt == null || !today.isAfter(effectiveDueAt)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(effectiveDueAt, today);
    }

    /**
     * 续借：校验次数与队列后顺延应还日，超出上限抛业务异常。
     *
     * @param maxRenew    允许的最大续借次数
     * @param queueLen    当前预约排队人数
     * @param queueBlock  排队人数阈值
     * @param periodDays  每次续借顺延天数
     */
    public void renew(int maxRenew, int queueLen, int queueBlock, int periodDays) {
        if (!isActive()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该借阅单已结束，无法续借");
        }
        if (renewCount >= maxRenew) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "已达最大续借次数（" + maxRenew + " 次）");
        }
        if (queueLen >= queueBlock) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该书目预约排队已达 " + queueLen + " 人，暂不可续借");
        }
        renewCount++;
        dueAt = dueAt.plusDays(periodDays);
        effectiveDueAt = dueAt.plusDays(graceDays);
    }

    /**
     * 累加宽限天数并重算有效应还日。
     *
     * @param days 本次批准天数
     */
    public void applyGrace(int days) {
        if (days <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "宽限天数必须大于 0");
        }
        graceDays += days;
        effectiveDueAt = dueAt.plusDays(graceDays);
    }

    /**
     * 归还结单：按条件置为 RETURNED/LOST 并返回逾期天数。
     *
     * @param returnedAt 归还时间
     * @param today      当天日期
     * @param condition  归还时副本状况
     * @return 最终逾期天数
     */
    public int returnOn(LocalDateTime returnedAt, LocalDate today, ReturnCondition condition) {
        if (!isActive()) {
            throw new BusinessException(ErrorCode.RULE_VIOLATION, "该借阅单不在借出状态，无法归还");
        }
        this.returnedAt = returnedAt;
        this.returnCondition = condition;
        this.finalOverdueDays = overdueDays(today);
        this.status = condition == ReturnCondition.LOST ? BorrowStatus.LOST : BorrowStatus.RETURNED;
        return this.finalOverdueDays;
    }
}
