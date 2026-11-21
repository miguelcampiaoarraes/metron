package com.metron.controller;

import com.metron.dto.CategoryDto;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserContext userContext;

    public CategoryController(CategoryService categoryService, CurrentUserContext userContext) {
        this.categoryService = categoryService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(@RequestParam(required = false) String type) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(categoryService.findAllAvailable(user, type));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        User user = userContext.getCurrentUser();
        CategoryDto created = categoryService.create(user, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        User user = userContext.getCurrentUser();
        CategoryDto updated = categoryService.update(user, id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long id) {
        User user = userContext.getCurrentUser();
        categoryService.delete(user, id);
        return ResponseEntity.ok(Map.of("message", "Categoria removida com sucesso."));
    }
}
