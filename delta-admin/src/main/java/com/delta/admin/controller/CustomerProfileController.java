package com.delta.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delta.common.dto.CustomerOrderRecordDTO;
import com.delta.common.dto.CustomerProfileUpdateDTO;
import com.delta.common.service.CustomerProfileService;
import com.delta.common.vo.CustomerOrderRecordVO;
import com.delta.common.vo.CustomerProfileVO;
import com.delta.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 客户画像管理控制器
 *
 * @author delta
 */
@Tag(name = "客户画像管理", description = "客户画像和消费记录管理接口")
@RestController
@RequestMapping("/v1/customer-profiles")
@RequiredArgsConstructor
public class CustomerProfileController extends BaseController {

    private final CustomerProfileService customerProfileService;

    @Operation(summary = "分页查询客户画像")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Page<CustomerProfileVO>> getProfilePage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "memberLevel", required = false) String memberLevel,
            @RequestParam(name = "riskLevel", required = false) String riskLevel,
            @RequestParam(name = "lifecycleStage", required = false) String lifecycleStage,
            @RequestParam(name = "rfmSegment", required = false) String rfmSegment,
            @RequestParam(name = "keyword", required = false) String keyword) {
        Page<CustomerProfileVO> pageResult = customerProfileService.getProfilePage(page, size, memberLevel, riskLevel, lifecycleStage, rfmSegment, keyword);
        return Result.success(pageResult);
    }

    @Operation(summary = "根据客户ID获取画像")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<CustomerProfileVO> getProfileByUserId(@PathVariable("userId") String userId) {
        CustomerProfileVO vo = customerProfileService.getProfileByUserId(decodeId(userId));
        return Result.success(vo);
    }

    @Operation(summary = "更新客户画像")
    @PutMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> updateProfile(@Valid @RequestBody CustomerProfileUpdateDTO dto) {
        customerProfileService.updateProfile(dto);
        return Result.success();
    }

    @Operation(summary = "添加消费记录")
    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> addOrderRecord(@Valid @RequestBody CustomerOrderRecordDTO dto) {
        customerProfileService.addOrderRecord(dto);
        return Result.success();
    }

    @Operation(summary = "分页查询消费记录")
    @GetMapping("/orders/page")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER', 'CS_STAFF')")
    public Result<Page<CustomerOrderRecordVO>> getOrderRecordPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "orderType", required = false) String orderType,
            @RequestParam(name = "status", required = false) String status) {
        Long decodedUserId = userId != null ? decodeId(userId) : null;
        Page<CustomerOrderRecordVO> pageResult = customerProfileService.getOrderRecordPage(page, size, decodedUserId, orderType, status);
        return Result.success(pageResult);
    }

    @Operation(summary = "刷新客户画像数据")
    @PostMapping("/refresh/{userId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'CS_LEADER')")
    public Result<Void> refreshProfile(@PathVariable("userId") String userId) {
        customerProfileService.refreshProfile(decodeId(userId));
        return Result.success();
    }
}
