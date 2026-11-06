package com.zian.backend.borrow.domain.model;

import com.zian.backend.shared.common.ErrorCode;
import com.zian.backend.shared.exception.BusinessException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 借阅单聚合：借出、续借、宽限生效、归还的规则都在这里收口。
 *
 * <p>不变量：续借次数 ≤ maxRenew；有效应还 = dueAt + graceDays；同一副本同时只能有一条在借单（由仓储保证）。
 */
public class BorrowOrder {

    private final Long id;
    private final String orderNo;
    private final Long readerId;
    private final Long bookId;
    private final Long copyId;
    private final LocalDateTime borrowedAt;
    private LocalDate dueAt;
    private LocalDate effectiveDueAt;
    private int graceDays;
    private int renewCount;
    private LocalDateTime returnedAt;
    private int finalOverdueDays;
    private BorrowStatus status;
    private ReturnCondition returnCondition;
    private final Integer version;

    public BorrowOrder(
            Long id,
            String orderNo,
            Long readerId,
            Long bookId,
            Long copyId,
            LocalDateTime borrowedAt,
            LocalDate dueAt,
            LocalDate effectiveDueAt,
            int graceDays,
            int renewCount,
            LocalDateTime returnedAt,
            int finalOverdueDays,
            BorrowStatus status,
            ReturnCondition returnCondition,
            Integer version) {
        this.id = id;
        this.orderNo = orderNo;
        this.readerId = readerId;
        this.bookId = bookId;
        this.copyId = copyId;
        this.borrowedAt = borrowedAt;
        this.dueAt = dueAt;
        this.effectiveDueAt = effectiveDueAt;
        this.graceDays = graceDays;
        this.renewCount = renewCount;
        this.returnedAt = returnedAt;
        this.finalOverdueDays = finalOverdueDays;
        this.status = status;
        this.returnCondition = returnCondition;
        this.version = version;
    }

    /** 借出：应还日期 = 借出日 + 借期。 */
    public static BorrowOrder open(
            String orderNo, Long readerId, Long bookId, Long copyId, LocalDateTime borrowedAt, int loanDays) {
        LocalDate due = borrowedAt.toLocalDate().plusDays(loanDays);
        return new BorrowOrder(
                null,
                orderNo,
                readerId,
                bookId,
                copyId,
                borrowedAt,
                due,
                due,
                0,
                0,
                null,
                0,
                BorrowStatus.BORROWED,
                null,
                null);
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getReaderId() {
        return readerId;
    }

    public Long getBookId() {
        return bookId;
    }

    public Long getCopyId() {
        return copyId;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public LocalDate getDueAt() {
        return dueAt;
    }

    public LocalDate getEffectiveDueAt() {
        return effectiveDueAt;
    }

    public int getGraceDays() {
        return graceDays;
    }

    public int getRenewCount() {
        return renewCount;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public int getFinalOverdueDays() {
        return finalOverdueDays;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public ReturnCondition getReturnCondition() {
        return returnCondition;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isActive() {
        return status.isActive();
    }

    /** 逾期天数（以有效应还为基准，未逾期为 0）。 */
    public int overdueDays(LocalDate today) {
        if (effectiveDueAt == null || !today.isAfter(effectiveDueAt)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(effectiveDueAt, today);
    }

    /** 续借：次数与排队长度双重限制。 */
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

    /** 宽限生效：延长有效应还日期。 */
    public void applyGrace(int days) {
        if (days <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "宽限天数必须大于 0");
        }
        graceDays += days;
        effectiveDueAt = dueAt.plusDays(graceDays);
    }

    /** 归还：计算最终逾期并结束借阅。 */
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
