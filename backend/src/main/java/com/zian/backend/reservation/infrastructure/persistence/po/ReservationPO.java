package com.zian.backend.reservation.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code reservation}（预约队列（Kq 供需系数来源））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("reservation")
@Data
public class ReservationPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long bookId;

    private Long readerId;

    /** 排队序号，重排后整体递减 */
    private Integer queueNo;

    /** 枚举：WAITING/READY/FULFILLED/CANCELLED/EXPIRED */
    private String status;

    /** 到馆可取时间 */
    private LocalDateTime readyAt;

    /** 保留截止（到馆后 48h） */
    private LocalDateTime expiresAt;

    private LocalDateTime fulfilledAt;

    private String cancelReason;

    /** 乐观锁版本号（队列重排） */
    @Version
    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
