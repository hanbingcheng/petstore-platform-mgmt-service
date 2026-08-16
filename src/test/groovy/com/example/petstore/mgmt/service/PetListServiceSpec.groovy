package com.example.petstore.mgmt.service

import com.example.petstore.mgmt.entity.PetEntity
import com.example.petstore.mgmt.mapper.PetMapper
import com.example.petstore.mgmt.model.Pet

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class PetListServiceSpec extends Specification {

	@Subject
	PetListService petListService

	PetMapper petMapper = Mock()

	def setup() {
		petListService = new PetListService(petMapper)
	}

	def "ペット一覧を取得できること"() {
		given:
		def entity1 = PetEntity.builder().id(1L).name("Dog").status("available").tags(["friendly"]).build()
		def entity2 = PetEntity.builder().id(2L).name("Cat").status("available").tags([]).build()

		when:
		petMapper.findAll(null, null) >> [entity1, entity2]
		def result = petListService.execute(null, null)

		then:
		result.size() == 2
		result[0].id == 1L
		result[0].name == "Dog"
		result[0].status == Pet.StatusEnum.fromValue("available")
		result[0].tags == ["friendly"]
		result[1].name == "Cat"
	}

	def "ステータスでフィルタリングできること"() {
		given:
		def entity = PetEntity.builder().id(1L).name("Puppy").status("available").build()

		when:
		petMapper.findAll("available", null) >> [entity]
		def result = petListService.execute("available", null)

		then:
		result.size() == 1
		result[0].status == Pet.StatusEnum.fromValue("available")
	}

	@Unroll
	def "カテゴリID「#categoryId」でペットをフィルタリングできること"() {
		given:
		def entity = PetEntity.builder().id(1L).name("Pet").status("available").build()

		when:
		petMapper.findAll(null, categoryId) >> (expectedCount > 0 ? [entity] : [])
		def result = petListService.execute(null, categoryId)

		then:
		result.size() == expectedCount

		where:
		categoryId || expectedCount
		1L        || 1
		2L        || 1
		999L      || 0
	}
}
