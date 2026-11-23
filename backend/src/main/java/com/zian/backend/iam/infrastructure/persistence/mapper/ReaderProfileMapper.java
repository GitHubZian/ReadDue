package com.zian.backend.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.iam.infrastructure.persistence.po.ReaderProfilePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 读者档案与隐私授权状态 的 MyBatis-Plus Mapper；自定义 SQL 写在同名 XML 或本接口默认方法中。 */
@Mapper
public interface ReaderProfileMapper extends BaseMapper<ReaderProfilePO> {

    /**
     * 读者累计借阅单数（含已归还）；只读计数，不改借阅状态。
     *
     * @param readerId 读者用户 ID
     * @return 单数
     */
    @Select("SELECT COUNT(*) FROM borrow_order WHERE reader_id = #{readerId}")
    long countBorrowed(@Param("readerId") long readerId);
}
