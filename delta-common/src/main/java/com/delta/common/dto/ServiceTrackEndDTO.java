package com.delta.common.dto;

import lombok.Data;

@Data
public class ServiceTrackEndDTO {

    private Long serviceCompanionId;
    private String serviceCompanionName;
    private Integer serviceDuration;
    private String serviceResult;
}
