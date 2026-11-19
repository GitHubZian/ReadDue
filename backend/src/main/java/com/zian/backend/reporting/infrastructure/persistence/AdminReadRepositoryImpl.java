package com.zian.backend.reporting.infrastructure.persistence;

import com.zian.backend.reporting.domain.model.AppealRow;
import com.zian.backend.reporting.domain.model.AuditLogRow;
import com.zian.backend.reporting.domain.model.CalendarRow;
import com.zian.backend.reporting.domain.model.CirculationRow;
import com.zian.backend.reporting.domain.model.DueTodayRow;
import com.zian.backend.reporting.domain.model.GraceEvidenceRow;
import com.zian.backend.reporting.domain.model.GraceRow;
import com.zian.backend.reporting.domain.model.NoticeRow;
import com.zian.backend.reporting.domain.model.NotifyTaskRow;
import com.zian.backend.reporting.domain.model.OverdueRow;
import com.zian.backend.reporting.domain.model.ReservationRow;
import com.zian.backend.reporting.domain.model.TierRow;
import com.zian.backend.reporting.domain.model.TrendRow;
import com.zian.backend.reporting.domain.model.UserRow;
import com.zian.backend.reporting.domain.repository.AdminReadRepository;
import com.zian.backend.reporting.infrastructure.persistence.mapper.AdminQueryMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 读模型端口的 MyBatis 适配器：应用层只看到 {@link AdminReadRepository}，SQL 与结果行映射留在 Mapper。
 */
@Repository
@RequiredArgsConstructor
public class AdminReadRepositoryImpl implements AdminReadRepository {

    private final AdminQueryMapper mapper;

    @Override
    public int countPendingGraces() {
        return mapper.countPendingGraces();
    }

    @Override
    public int countPendingAppeals() {
        return mapper.countPendingAppeals();
    }

    @Override
    public int countDueToday() {
        return mapper.countDueToday();
    }

    @Override
    public int countOverdueBooks() {
        return mapper.countOverdueBooks();
    }

    @Override
    public int countOverdueReaders() {
        return mapper.countOverdueReaders();
    }

    @Override
    public int countSlaBreach() {
        return mapper.countSlaBreach();
    }

    @Override
    public long sumOverdueDays() {
        return mapper.sumOverdueDays();
    }

    @Override
    public List<TrendRow> borrowTrend(LocalDateTime from) {
        return mapper.borrowTrend(from);
    }

    @Override
    public List<TrendRow> returnTrend(LocalDateTime from) {
        return mapper.returnTrend(from);
    }

    @Override
    public List<NoticeRow> notices() {
        return mapper.notices();
    }

    @Override
    public List<DueTodayRow> dueTodayItems() {
        return mapper.dueTodayItems();
    }

    @Override
    public List<GraceRow> graces(String status, String keyword, int limit, long offset) {
        return mapper.graces(status, keyword, limit, offset);
    }

    @Override
    public long countGraces(String status, String keyword) {
        return mapper.countGraces(status, keyword);
    }

    @Override
    public GraceRow findGrace(long id) {
        return mapper.findGrace(id);
    }

    @Override
    public List<GraceEvidenceRow> graceEvidence(List<Long> graceIds) {
        return mapper.graceEvidence(graceIds);
    }

    @Override
    public List<AppealRow> appeals(
            String status, String category, String keyword, int limit, long offset) {
        return mapper.appeals(status, category, keyword, limit, offset);
    }

    @Override
    public long countAppeals(String status, String category, String keyword) {
        return mapper.countAppeals(status, category, keyword);
    }

    @Override
    public List<ReservationRow> reservations(String status, String keyword, int limit, long offset) {
        return mapper.reservations(status, keyword, limit, offset);
    }

    @Override
    public long countReservations(String status, String keyword) {
        return mapper.countReservations(status, keyword);
    }

    @Override
    public List<OverdueRow> overdue(String status, String keyword, int limit, long offset) {
        return mapper.overdue(status, keyword, limit, offset);
    }

    @Override
    public long countOverdue(String status, String keyword) {
        return mapper.countOverdue(status, keyword);
    }

    @Override
    public List<NotifyTaskRow> notifyTasks(String status, String keyword, int limit, long offset) {
        return mapper.notifyTasks(status, keyword, limit, offset);
    }

    @Override
    public long countNotifyTasks(String status, String keyword) {
        return mapper.countNotifyTasks(status, keyword);
    }

    @Override
    public List<UserRow> users(
            String role, String status, String keyword, int limit, long offset) {
        return mapper.users(role, status, keyword, limit, offset);
    }

    @Override
    public long countUsers(String role, String status, String keyword) {
        return mapper.countUsers(role, status, keyword);
    }

    @Override
    public List<AuditLogRow> auditLogs(String category, String keyword, int limit, long offset) {
        return mapper.auditLogs(category, keyword, limit, offset);
    }

    @Override
    public long countAuditLogs(String category, String keyword) {
        return mapper.countAuditLogs(category, keyword);
    }

    @Override
    public AuditLogRow auditLog(Long id) {
        return mapper.auditLog(id);
    }

    @Override
    public List<CirculationRow> circulationBorrows() {
        return mapper.circulationBorrows();
    }

    @Override
    public List<CirculationRow> circulationReturns() {
        return mapper.circulationReturns();
    }

    @Override
    public int countBorrowsToday() {
        return mapper.countBorrowsToday();
    }

    @Override
    public int countReturnsToday() {
        return mapper.countReturnsToday();
    }

    @Override
    public List<TierRow> tiers() {
        return mapper.tiers();
    }

    @Override
    public List<CalendarRow> calendar() {
        return mapper.calendar();
    }
}
