package com.example.petstore.mgmt.mapper;

import com.example.petstore.mgmt.entity.CategoryEntity;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {
  List<CategoryEntity> findAll();

  Optional<CategoryEntity> findById(Long id);

  Optional<CategoryEntity> findByName(String name);

  void insert(CategoryEntity category);

  void update(CategoryEntity category);

  void deleteById(Long id);
}
