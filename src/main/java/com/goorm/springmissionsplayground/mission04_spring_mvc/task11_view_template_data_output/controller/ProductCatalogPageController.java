package com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.domain.ProductShowcaseItem;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.service.ProductCatalogPageService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission04/task11/products")
public class ProductCatalogPageController {

    private static final String VIEW_NAME = "mission04/task11/product-catalog";

    private final ProductCatalogPageService productCatalogPageService;

    public ProductCatalogPageController(ProductCatalogPageService productCatalogPageService) {
        this.productCatalogPageService = productCatalogPageService;
    }

    @GetMapping
    public String showProductCatalog(
            @RequestParam(defaultValue = "all") String category,
            Model model
    ) {
        String selectedCategory = productCatalogPageService.resolveCategory(category);
        Map<String, String> categoryOptions = productCatalogPageService.categoryOptions();
        List<ProductShowcaseItem> products = productCatalogPageService.findProducts(selectedCategory);

        model.addAttribute("pageTitle", "뷰 템플릿으로 출력하는 제품 목록");
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("selectedCategoryLabel", categoryOptions.get(selectedCategory));
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("featuredCount", productCatalogPageService.countFeatured(products));
        model.addAttribute("lowStockCount", productCatalogPageService.countLowStock(products));
        model.addAttribute("renderedAt", productCatalogPageService.renderedAt());
        return VIEW_NAME;
    }
}
