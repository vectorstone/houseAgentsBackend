package com.house.agents.service;

import com.house.agents.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author Gavin
 * @since 2023-07-30
 */
public interface DictService extends IService<Dict> {

    void importDict(MultipartFile file);
    void exportDicts(HttpServletResponse response);

    List<Dict> getByPid(Long pid);

    void saveWithSync(Dict dict);

    void removeByIdWithSync(Dict dict);

    void updateByIdWithSync(Dict dict);

    Map<String, Object> getDictList();

    List<Dict> listByDictCode(String dictCode);

}
