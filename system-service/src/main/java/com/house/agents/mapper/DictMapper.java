package com.house.agents.mapper;

import com.house.agents.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author Gavin
 * @since 2023-07-30
 */
public interface DictMapper extends BaseMapper<Dict> {
    List<Dict> listByDictCode(String dictCode);

    String getDictNameByDictCodeAndValue(@Param("dictCode") String education, @Param("value") Integer value);
}
