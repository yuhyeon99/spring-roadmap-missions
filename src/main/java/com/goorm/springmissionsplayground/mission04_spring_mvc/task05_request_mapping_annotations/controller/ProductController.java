package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto.ProductCreateRequest;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto.ProductResponse;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.service.ProductCatalogService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/mission04/task05/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ProductResponse> list() {
        return productCatalogService.listProducts();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest request) {
        ProductResponse created = productCatalogService.createProduct(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
