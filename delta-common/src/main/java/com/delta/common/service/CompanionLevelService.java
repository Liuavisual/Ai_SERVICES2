package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CompanionLevelDTO;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.vo.CompanionLevelVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 陪玩师等级服务接口，管理等级体系和定价
 *
 * @author 刘建国
 */
public interface CompanionLevelService {

    Page<CompanionLevelVO> getPage(Integer page, Integer size, String levelName);

    List<CompanionLevelVO> getAllEnabled();

    CompanionLevelVO getById(Long id);

    void create(CompanionLevelDTO dto);

    void update(CompanionLevelDTO dto);

    void delete(Long id);

    /**
     * 导出陪玩师等级Excel
     *
     * @param response  HTTP响应
     * @param levelName 等级名称
     */
    void exportCompanionLevels(HttpServletResponse response, String levelName);

    /**
     * 导入陪玩师等级Excel
     *
     * @param file 上传的Excel文件
     * @return 导入结果
     */
    ImportResultDTO importCompanionLevels(MultipartFile file);
}
