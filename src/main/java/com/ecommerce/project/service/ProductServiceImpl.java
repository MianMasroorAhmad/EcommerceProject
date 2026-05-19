package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;

    @Value("${project.image}")
    private String path;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ModelMapper modelMapper,
                              FileService fileService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
    }

    private double calculateSpecialPrice(double price, double discount) {
        return price - (price * (discount * 0.01));
    }

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        productRepository.findByProductNameAndCategory(productDTO.getProductName(), category)
                .ifPresent(p -> {
                    throw new APIException(String.format("Product with product name: %s already exists!", p.getProductName()));
                });

        Product productToSave = modelMapper.map(productDTO, Product.class);
        productToSave.setImage("default.png");
        productToSave.setSpecialPrice(calculateSpecialPrice(productDTO.getPrice(), productDTO.getDiscount()));
        productToSave.setCategory(category);
        Product savedProduct = productRepository.save(productToSave);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productsPage = productRepository.findAll(pageDetails);
        List<Product> products = productsPage.getContent();
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return ProductResponse.builder()
                .content(productDTOs)
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .lastPage(productsPage.isLast())
                .build();
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productsPage = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
        List<Product> products = productsPage.getContent();

        if(products.isEmpty()) {
            throw new APIException(String.format("Category %s does not have any products", category.getCategoryName()));
        }

        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        return ProductResponse.builder()
                .content(productDTOs)
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .lastPage(productsPage.isLast())
                .build();
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productsPage = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%", pageDetails); // %keyword% => anything containing keyword, % keyword => anything ending in keyword, only keyword => exact match with keyword, keyword % => starting with keyword || % is used in LIKE operator in SQL as a wildcard to say match anything here
        List<Product> products = productsPage.getContent();

        if(products.isEmpty()) {
            throw new APIException(String.format("Products not found with keyword: %s", keyword));
        }

        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return ProductResponse.builder()
                .content(productDTOs)
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .lastPage(productsPage.isLast())
                .build();
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.findByProductNameAndCategory(productDTO.getProductName(), productFromDB.getCategory())
                .ifPresent(p -> {
                    if (!p.getProductId().equals(productId)) {
                        throw new APIException(String.format("Product with product name: %s already exists in this category!", p.getProductName()));
                    }
                });

        Product product = modelMapper.map(productDTO, Product.class);
        productFromDB.setProductName(product.getProductName());
        productFromDB.setDescription(product.getDescription());
        productFromDB.setQuantity(product.getQuantity());
        productFromDB.setPrice(product.getPrice());
        productFromDB.setDiscount(product.getDiscount());
        productFromDB.setSpecialPrice(calculateSpecialPrice(product.getPrice(), product.getDiscount()));

        Product savedProduct = productRepository.save(productFromDB);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        productRepository.delete(productFromDB);
        return modelMapper.map(productFromDB, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) {
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileService.uploadImage(path, image);

        productFromDB.setImage(fileName);

        Product savedProduct = productRepository.save(productFromDB);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }
}
