package com.example.petstore.mgmt.service;

import com.example.petstore.common.exception.DuplicateResourceException;
import com.example.petstore.common.logging.StartEndLog;
import com.example.petstore.mgmt.entity.PetEntity;
import com.example.petstore.mgmt.mapper.PetMapper;
import com.example.petstore.mgmt.message.MgmtMessageCode;
import com.example.petstore.mgmt.model.CreatePetRequest;
import com.example.petstore.mgmt.model.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PetCreateService {

  private final PetMapper petMapper;

  @StartEndLog
  public Pet execute(CreatePetRequest request) {
    if (petMapper.existsByNameAndCategoryId(request.getName(), request.getCategoryId())) {
      throw new DuplicateResourceException(
          MgmtMessageCode.PET_DUPLICATE.getCode(),
          "Pet already exists with name: " + request.getName());
    }

    PetEntity entity =
        PetEntity.builder()
            .name(request.getName())
            .categoryId(request.getCategoryId())
            .status(request.getStatus() != null ? request.getStatus().getValue() : "available")
            .build();

    petMapper.insert(entity);

    return new Pet()
        .id(entity.getId())
        .name(entity.getName())
        .status(Pet.StatusEnum.fromValue(entity.getStatus()))
        .tags(entity.getTags());
  }
}
