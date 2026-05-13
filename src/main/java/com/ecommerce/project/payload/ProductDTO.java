package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId; // This gets overwritten by the DB
    @NotBlank
    @Size(min=3, message = "Product name must have at least 3 characters")
    private String productName;
    private String image;
    @NotBlank
    @Size(min=6, message = "Product description must have at least 6 characters")
    private String description;
    private Integer quantity;
    private double price;
    private double discount;
    private double specialPrice; // This gets overwritten by the service layer business logic
    // TODO: Perhaps create a ProductRequestDTO which doesnt take in productId and specialPrice so that there is more clarity for user of API as to what to send it as the RequestBody
}
