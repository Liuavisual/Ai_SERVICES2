package com.delta.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer rowNum;
}
