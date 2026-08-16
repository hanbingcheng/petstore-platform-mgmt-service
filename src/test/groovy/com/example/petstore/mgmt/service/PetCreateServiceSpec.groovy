package com.example.petstore.mgmt.service

import com.example.petstore.common.exception.DuplicateResourceException
import com.example.petstore.mgmt.entity.PetEntity
import com.example.petstore.mgmt.mapper.PetMapper
import com.example.petstore.mgmt.model.CreatePetRequest
import com.example.petstore.mgmt.model.Pet

import spock.lang.Specification
import spock.lang.Subject

class PetCreateServiceSpec extends Specification {

	@Subject
	PetCreateService petCreateService

	PetMapper petMapper = Mock()

	def setup() {
		petCreateService = new PetCreateService(petMapper)
	}

	def "ペットを作成できること"() {
		given:
		def request = new CreatePetRequest()
				.name("Hamster")
				.categoryId(1L)
				.status(CreatePetRequest.StatusEnum.fromValue("available"))

		when:
		petMapper.existsByNameAndCategoryId("Hamster", 1L) >> false
		petMapper.insert(_ as PetEntity) >> { PetEntity e -> e.setId(100L) }
		def result = petCreateService.execute(request)

		then:
		result.id == 100L
		result.name == "Hamster"
		result.status == Pet.StatusEnum.fromValue("available")
	}

	def "ステータス未指定時はデフォルトでavailableになること"() {
		given:
		def request = new CreatePetRequest()
				.name("DefaultPet")
				.categoryId(1L)
				.status(null)

		when:
		petMapper.existsByNameAndCategoryId("DefaultPet", 1L) >> false
		petMapper.insert(_ as PetEntity) >> { PetEntity e -> e.setId(1L) }
		def result = petCreateService.execute(request)

		then:
		result.status == Pet.StatusEnum.fromValue("available")
	}

	def "同じ名前のペットを作成するとDuplicateResourceExceptionが発生すること"() {
		given:
		def request = new CreatePetRequest()
				.name("Duplicate")
				.categoryId(1L)

		when:
		petMapper.existsByNameAndCategoryId("Duplicate", 1L) >> true
		petCreateService.execute(request)

		then:
		thrown(DuplicateResourceException)
	}
}
