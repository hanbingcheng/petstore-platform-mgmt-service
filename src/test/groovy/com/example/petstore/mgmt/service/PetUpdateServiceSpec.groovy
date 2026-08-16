package com.example.petstore.mgmt.service

import com.example.petstore.common.exception.ResourceNotFoundException
import com.example.petstore.mgmt.entity.PetEntity
import com.example.petstore.mgmt.mapper.PetMapper
import com.example.petstore.mgmt.model.Pet
import com.example.petstore.mgmt.model.UpdatePetRequest

import spock.lang.Specification
import spock.lang.Subject

class PetUpdateServiceSpec extends Specification {

	@Subject
	PetUpdateService petUpdateService

	PetMapper petMapper = Mock()

	def setup() {
		petUpdateService = new PetUpdateService(petMapper)
	}

	def "ペットの名前とステータスを更新できること"() {
		given:
		def entity = PetEntity.builder()
				.id(1L).name("Old Name").status("available")
				.build()
		def request = new UpdatePetRequest()
				.name("New Name")
				.status(UpdatePetRequest.StatusEnum.fromValue("sold"))

		when:
		petMapper.findById(1L) >> Optional.of(entity)
		def result = petUpdateService.execute(1L, request)

		then:
		1 * petMapper.update(_ as PetEntity)
		result.name == "New Name"
		result.status == Pet.StatusEnum.fromValue("sold")
	}

	def "名前のみ更新できること"() {
		given:
		def entity = PetEntity.builder()
				.id(1L).name("Old Name").status("available")
				.build()
		def request = new UpdatePetRequest().name("New Name")

		when:
		petMapper.findById(1L) >> Optional.of(entity)
		def result = petUpdateService.execute(1L, request)

		then:
		result.name == "New Name"
		result.status == Pet.StatusEnum.fromValue("available")
	}

	def "ステータスのみ更新できること"() {
		given:
		def entity = PetEntity.builder()
				.id(1L).name("old name").status("available")
				.build()
		def request = new UpdatePetRequest().status(UpdatePetRequest.StatusEnum.fromValue("sold"))

		when:
		petMapper.findById(1L) >> Optional.of(entity)
		def result = petUpdateService.execute(1L, request)

		then:
		result.name == "old name"
		result.status == Pet.StatusEnum.fromValue("sold")
	}

	def "存在しないペットを更新するとResourceNotFoundExceptionが発生すること"() {
		given:
		def request = new UpdatePetRequest().name("New Name")

		when:
		petMapper.findById(999L) >> Optional.empty()
		petUpdateService.execute(999L, request)

		then:
		thrown(ResourceNotFoundException)
	}
}
