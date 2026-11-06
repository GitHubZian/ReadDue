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

    /** 规则要求的最小可批天数（用于 UI 与校验）。 */
    public static int asDays(BigDecimal value) {
        return value.setScale(0, RoundingMode.CEILING).intValue();
    }
}
