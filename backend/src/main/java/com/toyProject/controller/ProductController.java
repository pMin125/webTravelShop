package com.toyProject.controller;

import com.toyProject.dto.PopularTravelDto;
import com.toyProject.dto.ProductDto;
import com.toyProject.entity.Product;
import com.toyProject.entity.UserEntity;
import com.toyProject.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    //상품 등록
    @PostMapping("/productAdd")
    public Product addProduct(@RequestBody ProductDto productDto) {
       return productService.addProduct(productDto);
    }

    // 상품 리스트
//    @GetMapping("/productList")
//    public List<ProductDto> productList(){
//        return productService.productList();
//    }

    @GetMapping("/productList")
    public List<ProductDto> productListV2() {
        return productService.productListV2();
    }

    // 상품 상세
    @GetMapping("/products/{id}")
    public Product productList(@PathVariable(name = "id") Long id) {
        return productService.productDetail(id);
    }

    // 인기 상품 캐싱
    @GetMapping("/popular")
    public ResponseEntity<List<PopularTravelDto>> getPopularProducts() {
        List<PopularTravelDto> popularTravels = productService.getPopularTravels();
        return ResponseEntity.ok(popularTravels);
    }
}
