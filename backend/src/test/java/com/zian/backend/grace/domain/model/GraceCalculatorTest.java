package com.zian.backend.grace.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 宽限天数公式 D = min(Dmax, round_step(Bs × Kt × Kq)) 的边界测试。
 */
class GraceCalculatorTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @Test
    @DisplayName("基础乘积：14 × 1.00 × 1.00 = 14 天")
    void computesProduct() {
        assertThat(GraceCalculator.compute(bd("14"), bd("1.00"), bd("1.00"), 14, bd("0.5")))
                .isEqualByComparingTo("14");
    }

    @Test
    @DisplayName("按 0.5 天步长四舍五入：7 × 0.85 × 0.7 = 4.165 → 4.0")
    void roundsToHalfDayStep() {
        assertThat(GraceCalculator.compute(bd("7"), bd("0.85"), bd("0.70"), 14, bd("0.5")))
                .isEqualByComparingTo("4.0");
    }

    @Test
    @DisplayName("四舍五入向下：10 × 0.85 × 0.85 = 7.225 → 7.0")
    void roundsDownWhenBelowHalfStep() {
        assertThat(GraceCalculator.compute(bd("10"), bd("0.85"), bd("0.85"), 14, bd("0.5")))
                .isEqualByComparingTo("7.0");
    }

    @Test
    @DisplayName("四舍五入向上：29 × 0.5 × 0.5 = 7.25 → 7.5")
    void roundsUpAtHalfStep() {
        assertThat(GraceCalculator.compute(bd("29"), bd("0.5"), bd("0.5"), 14, bd("0.5")))
                .isEqualByComparingTo("7.5");
    }

    @Test
    @DisplayName("超过 Dmax 时封顶")
    void capsAtDmax() {
        assertThat(GraceCalculator.compute(bd("30"), bd("1.00"), bd("1.00"), 14, bd("0.5")))
                .isEqualByComparingTo("14");
    }

    @Test
    @DisplayName("参数缺失返回 0 天")
    void handlesNullInputs() {
        assertThat(GraceCalculator.compute(null, bd("1"), bd("1"), 14, bd("0.5"))).isEqualByComparingTo("0");
        assertThat(GraceCalculator.compute(bd("7"), null, bd("1"), 14, bd("0.5"))).isEqualByComparingTo("0");
        assertThat(GraceCalculator.compute(bd("7"), bd("1"), null, 14, bd("0.5"))).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("步长非法时退化为 1 天")
    void fallsBackToOneDayStep() {
        assertThat(GraceCalculator.compute(bd("7"), bd("0.85"), bd("0.70"), 14, BigDecimal.ZERO))
                .isEqualByComparingTo("4");
    }

    @Test
    @DisplayName("接近 0 的结果不会为负")
    void neverNegative() {
        assertThat(GraceCalculator.compute(bd("0"), bd("0.55"), bd("0.70"), 14, bd("0.5")))
                .isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("asDays 向上取整，用于审批默认值")
    void asDaysCeilings() {
        assertThat(GraceCalculator.asDays(bd("4.0"))).isEqualTo(4);
        assertThat(GraceCalculator.asDays(bd("4.5"))).isEqualTo(5);
        assertThat(GraceCalculator.asDays(bd("0.5"))).isEqualTo(1);
    }
}
