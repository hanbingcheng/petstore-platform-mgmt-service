package com.example.petstore.mgmt.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetEntity {
  private Long id;
  private String name;
  private Long categoryId;
  private String status;
  private List<String> tags;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
