package com.zian.backend.grace.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 宽限天数计算器（纯函数，必须单测覆盖）。
 *
 * <p>公式：D = min(Dmax, round_step(Bs × Kt × Kq))，round_step 默认 0.5 天，四舍五入。
 */
public final class GraceCalculator {

    private GraceCalculator() {
    }

    /**
     * 按 Bs×Kt×Kq 计算宽限天数：先按步长四舍五入再乘步长，最后以 Dmax 封顶。
     */
    public static BigDecimal compute(
            BigDecimal baseDays, BigDecimal kt, BigDecimal kq, int dmax, BigDecimal roundStep) {
        if (baseDays == null || kt == null || kq == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal step = roundStep == null || roundStep.signum() <= 0 ? BigDecimal.ONE : roundStep;
        BigDecimal raw = baseDays.multiply(kt).multiply(kq);
        BigDecimal steps = raw.divide(step, 0, RoundingMode.HALF_UP);
        BigDecimal rounded = steps.multiply(step);
        BigDecimal capped = rounded.min(BigDecimal.valueOf(dmax));
        return capped.max(BigDecimal.ZERO).stripTrailingZeros();
    }

    /**
     * 把计算天数向上取整为整数天。
     */
    public static int asDays(BigDecimal value) {
        return value.setScale(0, RoundingMode.CEILING).intValue();
    }
}
