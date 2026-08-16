package com.example.petstore.mgmt.service

import com.example.petstore.common.exception.ResourceNotFoundException
import com.example.petstore.mgmt.entity.PetEntity
import com.example.petstore.mgmt.mapper.PetMapper
import com.example.petstore.mgmt.model.Pet

import spock.lang.Specification
import spock.lang.Subject

class PetGetServiceSpec extends Specification {

	@Subject
	PetGetService petGetService

	PetMapper petMapper = Mock()

	def setup() {
		petGetService = new PetGetService(petMapper)
	}

	def "IDでペットを取得できること"() {
		given:
		def entity = PetEntity.builder()
				.id(1L).name("Dog").status("available").tags(["tag1"])
				.build()

		when:
		petMapper.findById(1L) >> Optional.of(entity)
		def result = petGetService.execute(1L)

		then:
		result.id == 1L
		result.name == "Dog"
		result.status == Pet.StatusEnum.fromValue("available")
		result.tags == ["tag1"]
	}

	def "存在しないIDを指定するとResourceNotFoundExceptionが発生すること"() {
		when:
		petMapper.findById(999L) >> Optional.empty()
		petGetService.execute(999L)

		then:
		thrown(ResourceNotFoundException)
	}
}
