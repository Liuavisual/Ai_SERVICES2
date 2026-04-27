package com.delta.common.dto;

import lombok.Data;

@Data
public class ServiceTrackBookDTO {

    private Long bookedCompanionId;
    private String bookedCompanionName;
    private String bookedServiceType;
    private String bookedTimeSlot;
    private Long relatedOrderId;
}
