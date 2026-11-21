package com.metron.repository;

import com.metron.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL ORDER BY c.name ASC")
    List<Category> findAllAvailableForUser(@Param("userId") Long userId);

    List<Category> findByUserId(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);
}
