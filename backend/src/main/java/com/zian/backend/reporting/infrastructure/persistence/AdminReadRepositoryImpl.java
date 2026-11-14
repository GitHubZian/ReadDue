package com.zian.backend.reporting.infrastructure.persistence;

import com.zian.backend.reporting.domain.repository.AdminReadRepository;
import com.zian.backend.reporting.infrastructure.persistence.mapper.AdminQueryMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 读模型端口的 MyBatis 适配器：应用层只看到 {@link AdminReadRepository}，SQL 细节留在 Mapper。
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
    public List<Map<String, Object>> borrowTrend(LocalDateTime from) {
        return mapper.borrowTrend(from);
    }

    @Override
    public List<Map<String, Object>> returnTrend(LocalDateTime from) {
        return mapper.returnTrend(from);
    }

    @Override
    public List<Map<String, Object>> notices() {
        return mapper.notices();
    }

    @Override
    public List<Map<String, Object>> dueTodayItems() {
        return mapper.dueTodayItems();
    }

    @Override
    public List<Map<String, Object>> graces(String status, String keyword, int limit, long offset) {
        return mapper.graces(status, keyword, limit, offset);
    }

    @Override
    public long countGraces(String status, String keyword) {
        return mapper.countGraces(status, keyword);
    }

    @Override
    public List<Map<String, Object>> appeals(
            String status, String category, String keyword, int limit, long offset) {
        return mapper.appeals(status, category, keyword, limit, offset);
    }

    @Override
    public long countAppeals(String status, String category, String keyword) {
        return mapper.countAppeals(status, category, keyword);
    }

    @Override
    public List<Map<String, Object>> reservations(String status, String keyword, int limit, long offset) {
        return mapper.reservations(status, keyword, limit, offset);
    }

    @Override
    public long countReservations(String status, String keyword) {
        return mapper.countReservations(status, keyword);
    }

    @Override
    public List<Map<String, Object>> overdue(String status, String keyword, int limit, long offset) {
        return mapper.overdue(status, keyword, limit, offset);
    }

    @Override
    public long countOverdue(String status, String keyword) {
        return mapper.countOverdue(status, keyword);
    }

    @Override
    public List<Map<String, Object>> notifyTasks(String status, String keyword, int limit, long offset) {
        return mapper.notifyTasks(status, keyword, limit, offset);
    }

    @Override
    public long countNotifyTasks(String status, String keyword) {
        return mapper.countNotifyTasks(status, keyword);
    }

    @Override
    public List<Map<String, Object>> users(
            String role, String status, String keyword, int limit, long offset) {
        return mapper.users(role, status, keyword, limit, offset);
    }

    @Override
    public long countUsers(String role, String status, String keyword) {
        return mapper.countUsers(role, status, keyword);
    }

    @Override
    public List<Map<String, Object>> auditLogs(String category, String keyword, int limit, long offset) {
        return mapper.auditLogs(category, keyword, limit, offset);
    }

    @Override
    public long countAuditLogs(String category, String keyword) {
        return mapper.countAuditLogs(category, keyword);
    }

    @Override
    public Map<String, Object> auditLog(Long id) {
        return mapper.auditLog(id);
    }

    @Override
    public List<Map<String, Object>> circulationBorrows() {
        return mapper.circulationBorrows();
    }

    @Override
    public List<Map<String, Object>> circulationReturns() {
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
    public List<Map<String, Object>> tiers() {
        return mapper.tiers();
    }

    @Override
    public List<Map<String, Object>> calendar() {
        return mapper.calendar();
    }
}
