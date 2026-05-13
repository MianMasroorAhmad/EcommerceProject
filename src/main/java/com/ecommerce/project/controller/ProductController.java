package com.ecommerce.project.controller;

import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping("/admin/categories/{categoryId}/products")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO, @PathVariable Long categoryId){
        ProductDTO savedProductDTO = productService.addProduct(productDTO, categoryId);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getProducts(@RequestParam(required = false) String keyword){
        ProductResponse products = keyword != null
                ? productService.searchProductByKeyword(keyword)
                : productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductByCategory(@PathVariable Long categoryId){
        ProductResponse products = productService.searchByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    // TODO: If i want to update category as well. Perhaps just create ProductRequestDTO and then in that include categoryId, do a null check on it in service layer and if present update category otherwise dont
    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO, @PathVariable Long productId){
        ProductDTO updatedProductDTO = productService.updateProduct(productDTO, productId);
        return ResponseEntity.ok(updatedProductDTO);
    }

    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId, @RequestParam("image") MultipartFile image) {
        ProductDTO updatedProductImage = productService.updateProductImage(productId, image);
        return ResponseEntity.ok(updatedProductImage);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){
        ProductDTO deletedProductDTO = productService.deleteProduct(productId);
        return ResponseEntity.ok(deletedProductDTO);
    }
}
