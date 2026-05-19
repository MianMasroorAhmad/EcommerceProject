package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder){
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoriesPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoriesPage.getContent();
        if(categories.isEmpty()){
            throw new APIException("No Categories created so far");
        }
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        return CategoryResponse.builder()
                .content(categoryDTOS)
                .pageNumber(categoriesPage.getNumber())
                .pageSize(categoriesPage.getSize())
                .totalElements(categoriesPage.getTotalElements())
                .totalPages(categoriesPage.getTotalPages())
                .lastPage(categoriesPage.isLast())
                .build();
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.findByCategoryName(category.getCategoryName()).ifPresent(c -> {
            throw new APIException(String.format("Category with category name: %s already exists!", c.getCategoryName()));
        });
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory,CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        categoryRepository.delete(categoryToDelete);
        return modelMapper.map(categoryToDelete, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        categoryRepository.findByCategoryName(categoryDTO.getCategoryName()).ifPresent(c -> {
            throw new APIException(String.format("Category with category name: %s already exists!", c.getCategoryName()));
        });

        Category category = modelMapper.map(categoryDTO, Category.class);
        // category currently doesn't have an ID. If we save it without setting ID, then JPA will simply insert a NEW record, if we do SET the ID which is the Primary key before saving, JPA will UPDATE the existing record
        category.setCategoryId(categoryId);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }
}
