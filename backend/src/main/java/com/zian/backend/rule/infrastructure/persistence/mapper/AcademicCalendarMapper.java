package com.zian.backend.rule.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.rule.infrastructure.persistence.po.AcademicCalendarPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** 校历场景区间（客观、零隐私成本） 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface AcademicCalendarMapper extends BaseMapper<AcademicCalendarPO> {

    /**
     * 软删除：deleted 写本行主键，并记录删除时间与操作者。
     *
     * <p>全局 logic-delete 只会把 deleted 置为逻辑删除值，无法同时写审计列，故此处手写 SQL。
     *
     * @param id         主键
     * @param operatorId 操作者 ID
     * @return 受影响行数，0 表示不存在或已删除
     */
    @Update(
            """
            UPDATE academic_calendar
               SET deleted = #{id}, deleted_at = NOW(), deleted_by = #{operatorId}
             WHERE id = #{id} AND deleted = 0
            """)
    int softDelete(@Param("id") long id, @Param("operatorId") long operatorId);
}
