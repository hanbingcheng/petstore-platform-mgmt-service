package com.example.petstore.mgmt.service;

import com.example.petstore.common.exception.ResourceNotFoundException;
import com.example.petstore.common.logging.StartEndLog;
import com.example.petstore.mgmt.mapper.PetMapper;
import com.example.petstore.mgmt.message.MgmtMessageCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PetDeleteService {

  private final PetMapper petMapper;

  @StartEndLog
  public void execute(Long id) {
    if (petMapper.findById(id).isEmpty()) {
      throw new ResourceNotFoundException(
          MgmtMessageCode.PET_NOT_FOUND.getCode(), "Pet not found with id: " + id);
    }
    petMapper.deleteById(id);
  }
}
