package com.zian.backend.grace.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.grace.infrastructure.persistence.po.GraceEvidencePO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * {@code grace_evidence} 表的数据访问入口。
 */
@Mapper
public interface GraceEvidenceMapper extends BaseMapper<GraceEvidencePO> {

    /**
     * 查询某个读者全部宽限申请关联的文件 id（遗忘级联清理用；跨表，无法用单表 wrapper 表达）。
     *
     * @param readerId 读者账号
     * @return 文件 id 列表
     */
    @Select("""
            SELECT e.file_id
              FROM grace_evidence e
              JOIN grace_application a ON a.id = e.grace_id
             WHERE a.reader_id = #{readerId}
            """)
    List<Long> selectFileIdsByReader(@Param("readerId") long readerId);
}
