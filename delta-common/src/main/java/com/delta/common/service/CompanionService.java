package com.delta.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CompanionDTO;
import com.delta.common.dto.ImportResultDTO;
import com.delta.common.vo.CompanionVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 陪玩师服务接口，管理陪玩师信息和状态
 *
 * @author delta
 */
public interface CompanionService {

    Page<CompanionVO> getPage(Integer page, Integer size, Long levelId, String nickname, Integer enabled);

    List<CompanionVO> getAllEnabled();

    List<CompanionVO> getAvailableByDateAndLevel(LocalDate date, Long levelId);

    CompanionVO getById(Long id);

    void create(CompanionDTO dto);

    void update(CompanionDTO dto);

    void delete(Long id);

    /**
     * 导出陪玩师Excel
     *
     * @param response HTTP响应
     * @param levelId  等级ID
     * @param nickname 昵称
     * @param enabled  启用状态
     */
    void exportCompanions(HttpServletResponse response, Long levelId, String nickname, Integer enabled);

    /**
     * 导入陪玩师Excel
     *
     * @param file 上传的Excel文件
     * @return 导入结果
     */
    ImportResultDTO importCompanions(MultipartFile file);
}
