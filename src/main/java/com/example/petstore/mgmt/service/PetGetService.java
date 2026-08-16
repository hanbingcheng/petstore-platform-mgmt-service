package com.example.petstore.mgmt.service;

import com.example.petstore.common.exception.ResourceNotFoundException;
import com.example.petstore.common.logging.StartEndLog;
import com.example.petstore.mgmt.entity.PetEntity;
import com.example.petstore.mgmt.mapper.PetMapper;
import com.example.petstore.mgmt.message.MgmtMessageCode;
import com.example.petstore.mgmt.model.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetGetService {

  private final PetMapper petMapper;

  @StartEndLog
  public Pet execute(Long id) {
    PetEntity entity =
        petMapper
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        MgmtMessageCode.PET_NOT_FOUND.getCode(), "Pet not found with id: " + id));

    return new Pet()
        .id(entity.getId())
        .name(entity.getName())
        .status(Pet.StatusEnum.fromValue(entity.getStatus()))
        .tags(entity.getTags());
  }
}
