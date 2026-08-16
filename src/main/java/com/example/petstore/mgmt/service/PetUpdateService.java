package com.example.petstore.mgmt.service;

import com.example.petstore.common.exception.ResourceNotFoundException;
import com.example.petstore.common.logging.StartEndLog;
import com.example.petstore.mgmt.entity.PetEntity;
import com.example.petstore.mgmt.mapper.PetMapper;
import com.example.petstore.mgmt.message.MgmtMessageCode;
import com.example.petstore.mgmt.model.Pet;
import com.example.petstore.mgmt.model.UpdatePetRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PetUpdateService {

  private final PetMapper petMapper;

  @StartEndLog
  public Pet execute(Long id, UpdatePetRequest request) {
    PetEntity entity =
        petMapper
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        MgmtMessageCode.PET_NOT_FOUND.getCode(), "Pet not found with id: " + id));

    if (request.getName() != null) entity.setName(request.getName());
    if (request.getStatus() != null) entity.setStatus(request.getStatus().getValue());

    petMapper.update(entity);

    return new Pet()
        .id(entity.getId())
        .name(entity.getName())
        .status(Pet.StatusEnum.fromValue(entity.getStatus()))
        .tags(entity.getTags());
  }
}
