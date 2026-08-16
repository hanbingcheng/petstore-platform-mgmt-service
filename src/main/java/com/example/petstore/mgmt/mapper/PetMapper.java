package com.example.petstore.mgmt.mapper;

import com.example.petstore.mgmt.entity.PetEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PetMapper {
  List<PetEntity> findAll(@Param("status") String status, @Param("categoryId") Long categoryId);

  Optional<PetEntity> findById(Long id);

  Optional<PetEntity> findByNameAndCategoryId(
      @Param("name") String name, @Param("categoryId") Long categoryId);

  boolean existsByNameAndCategoryId(
      @Param("name") String name, @Param("categoryId") Long categoryId);

  void insert(PetEntity pet);

  void update(PetEntity pet);

  void updateStatus(@Param("id") Long id, @Param("status") String status);

  void deleteById(Long id);

  long countByCategoryId(Long categoryId);
}
