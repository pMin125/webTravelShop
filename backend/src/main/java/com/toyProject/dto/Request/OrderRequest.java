package com.toyProject.dto.Request;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderRequest {
    private List<Long> selectedProductIds;

}
