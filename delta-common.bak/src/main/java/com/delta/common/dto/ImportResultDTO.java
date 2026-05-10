package com.delta.common.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Excel导入结果DTO
 * <p>
 * 封装Excel导入操作的结果数据，包括成功数、失败数和总数。
 * </p>
 *
 * @author 刘建国
 */
@Data
public class ImportResultDTO {

    /** 导入成功条数 */
    private int success;

    /** 导入失败条数 */
    private int fail;

    /** 导入总条数 */
    private int total;

    /**
     * 构造导入结果
     *
     * @param success 成功条数
     * @param fail    失败条数
     */
    public ImportResultDTO(int success, int fail) {
        this.success = success;
        this.fail = fail;
        this.total = success + fail;
    }

    /**
     * 将导入结果转换为Map
     *
     * @return 包含success、fail、total的Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", success);
        map.put("fail", fail);
        map.put("total", total);
        return map;
    }
}
