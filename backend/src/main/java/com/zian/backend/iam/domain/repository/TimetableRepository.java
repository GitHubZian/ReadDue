package com.zian.backend.iam.domain.repository;

import com.zian.backend.iam.domain.model.Course;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 读者课表仓储（M5-F）。
 */
public interface TimetableRepository {

    /**
     * 读取读者当前有效课表，按星期与开始时间排序。
     *
     * @param userId 读者 id
     * @return 课程列表
     */
    List<Course> findActive(long userId);

    /**
     * 覆盖式保存课表：先软删除旧记录，再插入新记录。
     *
     * @param userId  读者 id
     * @param courses 新课程列表（可为空，表示清空）
     */
    void replace(long userId, List<Course> courses);

    /**
     * 读者课表最近更新时间。
     *
     * @param userId 读者 id
     * @return 最近更新时间；从未保存过返回 null
     */
    LocalDateTime lastUpdated(long userId);
}
