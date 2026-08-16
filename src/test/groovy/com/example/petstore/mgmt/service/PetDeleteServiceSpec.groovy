package com.example.petstore.mgmt.service

import com.example.petstore.common.exception.ResourceNotFoundException
import com.example.petstore.mgmt.entity.PetEntity
import com.example.petstore.mgmt.mapper.PetMapper

import spock.lang.Specification
import spock.lang.Subject

class PetDeleteServiceSpec extends Specification {

	@Subject
	PetDeleteService petDeleteService

	PetMapper petMapper = Mock()

	def setup() {
		petDeleteService = new PetDeleteService(petMapper)
	}

	def "ペットを削除できること"() {
		given:
		def entity = PetEntity.builder().id(1L).name("To Delete").build()

		when:
		petMapper.findById(1L) >> Optional.of(entity)
		petDeleteService.execute(1L)

		then:
		1 * petMapper.deleteById(1L)
	}

	def "存在しないペットを削除するとResourceNotFoundExceptionが発生すること"() {
		when:
		petMapper.findById(999L) >> Optional.empty()
		petDeleteService.execute(999L)

		then:
		thrown(ResourceNotFoundException)
	}
}
