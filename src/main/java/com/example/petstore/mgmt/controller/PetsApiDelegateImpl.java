package com.example.petstore.mgmt.controller;

import com.example.petstore.mgmt.api.PetsApiDelegate;
import com.example.petstore.mgmt.model.CreatePetRequest;
import com.example.petstore.mgmt.model.Pet;
import com.example.petstore.mgmt.model.UpdatePetRequest;
import com.example.petstore.mgmt.service.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetsApiDelegateImpl implements PetsApiDelegate {

  private final PetListService petListService;
  private final PetCreateService petCreateService;
  private final PetGetService petGetService;
  private final PetUpdateService petUpdateService;
  private final PetDeleteService petDeleteService;

  @Override
  public ResponseEntity<List<Pet>> getPets(String status, Long categoryId) {
    return ResponseEntity.ok(petListService.execute(status, categoryId));
  }

  @Override
  public ResponseEntity<Pet> createPet(CreatePetRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(petCreateService.execute(request));
  }

  @Override
  public ResponseEntity<Pet> getPetById(Long petId) {
    return ResponseEntity.ok(petGetService.execute(petId));
  }

  @Override
  public ResponseEntity<Void> updatePet(Long petId, UpdatePetRequest request) {
    petUpdateService.execute(petId, request);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> deletePet(Long petId) {
    petDeleteService.execute(petId);
    return ResponseEntity.noContent().build();
  }
}
