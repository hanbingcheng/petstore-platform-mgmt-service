package com.example.petstore.mgmt.service;

import com.example.petstore.common.logging.StartEndLog;
import com.example.petstore.mgmt.mapper.PetMapper;
import com.example.petstore.mgmt.model.Pet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetListService {

  private final PetMapper petMapper;

  @StartEndLog
  public List<Pet> execute(String status, Long categoryId) {
    return petMapper.findAll(status, categoryId).stream()
        .map(
            entity ->
                new Pet()
                    .id(entity.getId())
                    .name(entity.getName())
                    .status(Pet.StatusEnum.fromValue(entity.getStatus()))
                    .tags(entity.getTags()))
        .collect(Collectors.toList());
  }
}
