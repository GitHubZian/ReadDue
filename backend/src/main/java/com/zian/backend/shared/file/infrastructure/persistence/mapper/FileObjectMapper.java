package com.zian.backend.shared.file.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zian.backend.shared.file.infrastructure.persistence.po.FileObjectPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@code file_object} 表的数据访问入口。
 */
@Mapper
public interface FileObjectMapper extends BaseMapper<FileObjectPO> {
}
