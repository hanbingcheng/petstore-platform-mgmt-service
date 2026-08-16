package com.example.petstore.mgmt.integration

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

import com.example.petstore.mgmt.model.CreatePetRequest
import com.example.petstore.mgmt.model.Pet as ApiPet
import com.example.petstore.mgmt.model.UpdatePetRequest

import ch.qos.logback.classic.Level

class PetApiIntegrationSpec extends BaseIntegrationSpec {


	// ========================================
	// テスト: ペット作成→取得の一連フロー
	// ========================================
	def "ペットを作成して、そのペットを取得できること"() {
		given:
		def request = new CreatePetRequest()
				.name("Integration Test Pet")
				.categoryId(1L)
				.status(CreatePetRequest.StatusEnum.AVAILABLE)
				.tags(["test", "integration"])

		when:

		def createResponse = null
		def logEvents = captureLogEvents  {
			createResponse = restTemplate.postForEntity("${getBaseUrl()}/pets", request, ApiPet)
		}

		then:
		// レスポンス検証
		createResponse.statusCode == HttpStatus.CREATED
		createResponse.body.name == "Integration Test Pet"
		createResponse.body.id != null

		// [DB Assert] petsテーブルに正常に登録されたことを検証
		def petId = createResponse.body.id
		def petRows = jdbcTemplate.queryForList("SELECT * FROM pets WHERE id = ?", petId)
		assert petRows.size() == 1
		assert petRows[0].name == "Integration Test Pet"
		assert petRows[0].category_id == 1
		assert petRows[0].status == "available"

		// ログの検証
		logEvents[0].level == Level.INFO
		logEvents[0].formattedMessage == "[I000001] [PetCreateService]処理開始1: input={request=class CreatePetRequest {\n    name: Integration Test Pet\n    categoryId: 1\n    status: available\n    tags: [test, integration]\n}}"
		logEvents[logEvents.size()-1].level == Level.INFO
		logEvents[logEvents.size()-1].formattedMessage == "[I000002] [PetCreateService]処理完了: result=class Pet {\n    id: 1\n    name: Integration Test Pet\n    category: null\n    status: available\n    tags: null\n    createdAt: null\n}"

		when:
		def getResponse = null
		logEvents = captureLogEvents {
			getResponse = restTemplate.getForEntity(
					"${getBaseUrl()}/pets/${petId}",
					ApiPet
					)
		}

		then:
		getResponse.statusCode == HttpStatus.OK
		getResponse.body.name == "Integration Test Pet"
		getResponse.body.status.value == "available"
		logEvents[0].level == Level.INFO
		logEvents[0].formattedMessage == "[I000001] [PetGetService]処理開始1: input={id=1}"
		logEvents[logEvents.size()-1].level == Level.INFO
		logEvents[logEvents.size()-1].formattedMessage == "[I000002] [PetGetService]処理完了: result=class Pet {\n    id: 1\n    name: Integration Test Pet\n    category: null\n    status: available\n    tags: []\n    createdAt: null\n}"
	}

	// ========================================
	// テスト: ペット一覧取得（フィルタリング）
	// ========================================
	def "ペット一覧をステータスでフィルタリングできること"() {
		given:
		def pet1 = new CreatePetRequest()
				.name("Available Pet")
				.categoryId(1L)
				.status(CreatePetRequest.StatusEnum.AVAILABLE)
		restTemplate.postForEntity("${getBaseUrl()}/pets", pet1, ApiPet)

		def pet2 = new CreatePetRequest()
				.name("Sold Pet")
				.categoryId(1L)
				.status(CreatePetRequest.StatusEnum.AVAILABLE)
		restTemplate.postForEntity("${getBaseUrl()}/pets", pet2, ApiPet)

		when:
		def response = null
		def logEvents = captureLogEvents {
			response = restTemplate.getForEntity(
					"${getBaseUrl()}/pets?status=available",
					ApiPet[].class
					)
		}

		then:
		response.statusCode == HttpStatus.OK
		response.body.findAll { it.status.value == "available" }.size() >= 1
		logEvents[0].level == Level.INFO
		logEvents[0].formattedMessage == "[I000001] [PetListService]処理開始1: input={status=available, categoryId=null}"
		logEvents[logEvents.size()-1].level == Level.INFO
		logEvents[logEvents.size() - 1].formattedMessage == """[I000002] [PetListService]処理完了: result=[class Pet {
    id: 1
    name: Available Pet
    category: null
    status: available
    tags: []
    createdAt: null
}, class Pet {
    id: 2
    name: Sold Pet
    category: null
    status: available
    tags: []
    createdAt: null
}]"""
	}

	// ========================================
	// テスト: バリデーションエラー
	// ========================================
	def "空の名前でペットを作成すると400エラーが返ること"() {
		given:
		def request = new CreatePetRequest()
				.name(null)
				.categoryId(1L)

		when:
		def response = null
		def logEvents = captureLogEvents {
			response = restTemplate.postForEntity(
					"${getBaseUrl()}/pets",
					request,
					Map
					)
		}

		then:
		// レスポンス検証
		response.statusCode == HttpStatus.BAD_REQUEST
		response.body.status == 400
		response.body.message == "Validation failed"
		// [DB Assert] リクエストが不正なため、DBへのインサートは一切行われていないこと(0件)を確認
		def count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pets", Integer.class)
		assert count == 0
		// ログ検証
		logEvents[0].toString() == "[WARN] Request body validation failed: Validation failed for argument [0] in public default org.springframework.http.ResponseEntity<com.example.petstore.mgmt.model.Pet> com.example.petstore.mgmt.api.PetsApi.createPet(com.example.petstore.mgmt.model.CreatePetRequest): [Field error in object 'createPetRequest' on field 'name': rejected value [null]; codes [NotNull.createPetRequest.name,NotNull.name,NotNull.java.lang.String,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [createPetRequest.name,name]; arguments []; default message [name]]; default message [null は許可されていません]] "
	}

	// ========================================
	// テスト: 重複チェック
	// ========================================
	def "同じ名前のペットを登録しようとすると409エラーが返ること"() {
		given:
		def petName = "Duplicate Pet"

		def request1 = new CreatePetRequest()
				.name(petName)
				.categoryId(1L)
		restTemplate.postForEntity("${getBaseUrl()}/pets", request1, ApiPet)

		def request2 = new CreatePetRequest()
				.name(petName)
				.categoryId(1L)

		when:
		def response = null
		def logEvents = captureLogEvents {
			response = restTemplate.postForEntity("${getBaseUrl()}/pets", request2, Map)
		}
		then:
		// レスポンス検証
		response.statusCode == HttpStatus.CONFLICT
		response.body.message.contains("Pet already exists")
		// [DB Assert] 2回目のインサートはコンフリクトで弾かれ、テーブルには1件しか存在しないことを担保
		def count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pets WHERE name = ?", Integer.class, petName)
		assert count == 1
		// ログ検証
		logEvents[0].level == Level.INFO
		logEvents[0].formattedMessage == """[I000001] [PetCreateService]処理開始1: input={request=class CreatePetRequest {
    name: Duplicate Pet
    categoryId: 1
    status: available
    tags: []
}}"""
		logEvents[logEvents.size() - 2].level == Level.ERROR
		logEvents[logEvents.size() - 2].formattedMessage == "[E000017] [PetCreateService]処理異常終了: Pet already exists with name: Duplicate Pet"
		logEvents[logEvents.size() - 1].level == Level.WARN
		logEvents[logEvents.size() - 1].formattedMessage == "Duplicate resource: Pet already exists with name: Duplicate Pet"
	}

	// ========================================
	// テスト: ペット更新
	// ========================================
	def "ペットを更新できること"() {
		given:
		def createRequest = new CreatePetRequest()
				.name("Before Update")
				.categoryId(1L)
		def createResponse = restTemplate.postForEntity(
				"${getBaseUrl()}/pets",
				createRequest,
				ApiPet
				)
		def petId = createResponse.body.id

		def updateRequest = new UpdatePetRequest()
				.name("After Update")
				.status(UpdatePetRequest.StatusEnum.SOLD)

		when:
		def updateResponse = null
		def logEvents = captureLogEvents {
			updateResponse = restTemplate.exchange(
					"${getBaseUrl()}/pets/${petId}",
					HttpMethod.PUT,
					new HttpEntity(updateRequest),
					ApiPet
					)
		}
		then:
		// レスpン須検証
		updateResponse.statusCode == HttpStatus.OK
		// [DB Assert] テーブル内の値が「After Update」「sold」に正しく物理更新されているかをアサート
		def updatedRows = jdbcTemplate.queryForList("SELECT * FROM pets WHERE id = ?", petId)
		assert updatedRows.size() == 1
		assert updatedRows[0].name == "After Update"
		assert updatedRows[0].status == "sold"
		// ログ検証
		logEvents[0].level == Level.INFO
		logEvents[0].formattedMessage == """[I000001] [PetUpdateService]処理開始1: input={id=1, request=class UpdatePetRequest {
    name: After Update
    status: sold
}}"""
		logEvents[logEvents.size() - 1].level == Level.INFO
		logEvents[logEvents.size() - 1].formattedMessage == """[I000002] [PetUpdateService]処理完了: result=class Pet {
    id: 1
    name: After Update
    category: null
    status: sold
    tags: []
    createdAt: null
}"""
	}

	// ========================================
	// テスト: ペット削除
	// ========================================
	def "ペットを削除できること"() {
		given:
		def createRequest = new CreatePetRequest()
				.name("Delete Test Pet")
				.categoryId(1L)
		def createResponse = restTemplate.postForEntity(
				"${getBaseUrl()}/pets",
				createRequest,
				ApiPet
				)
		def petId = createResponse.body.id

		when:
		def deleteResponse = null
		def logEvents = captureLogEvents {
			deleteResponse = restTemplate.exchange(
					"${getBaseUrl()}/pets/${petId}",
					HttpMethod.DELETE,
					null,
					Void
					)
		}

		then:
		// レスポンス検証
		deleteResponse.statusCode == HttpStatus.NO_CONTENT
		// [DB Assert] 論理削除・物理削除に関わらず、指定IDの有効レコードがDB上に存在しなくなっていることをアサート
		def remainingCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pets WHERE id = ?", Integer.class, petId)
		assert remainingCount == 0
		// ログ検証
		logEvents[0].level == Level.INFO
		logEvents[0].formattedMessage == "[I000001] [PetDeleteService]処理開始1: input={id=1}"
		logEvents[logEvents.size() - 1].level == Level.INFO
		logEvents[logEvents.size() - 1].formattedMessage == "[I000002] [PetDeleteService]処理完了: result=null"

		when:
		def getResponse = null
		logEvents = captureLogEvents {
			getResponse = restTemplate.getForEntity(
					"${getBaseUrl()}/pets/${petId}",
					Map
					)
		}

		then:
		// レスポンス検証
		getResponse.statusCode == HttpStatus.NOT_FOUND
		// [DB Assert] 論理削除・物理削除に関わらず、指定IDの有効レコードがDB上に存在しなくなっていることをアサート
		def remainingCount2 = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pets WHERE id = ?", Integer.class, petId)
		assert remainingCount2 == 0
		// ログ検証
		logEvents[0].level == Level.INFO
		logEvents[0].formattedMessage == "[I000001] [PetGetService]処理開始1: input={id=1}"
		logEvents[logEvents.size() - 2].level == Level.ERROR
		logEvents[logEvents.size() - 2].formattedMessage == "[E000017] [PetGetService]処理異常終了: Pet not found with id: 1"
		logEvents[logEvents.size() - 1].level == Level.WARN
		logEvents[logEvents.size() - 1].formattedMessage == "Resource not found: Pet not found with id: 1"
	}

	// ========================================
	// テスト: 存在しないペットを取得
	// ========================================
	def "存在しないペットIDを指定すると404エラーが返ること"() {
		when:
		def response = null
		def logEvents = captureLogEvents {
			response = restTemplate.getForEntity(
					"${getBaseUrl()}/pets/99999",
					Map
					)
		}

		then:
		// レスポンス検証
		response.statusCode == HttpStatus.NOT_FOUND
		response.body.message.contains("Pet not found")
		// [DB Assert] 当然ながら実在しないクエリによって、DBのデータ総数に一切の変化がないことを担保
		def totalCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pets", Integer.class)
		assert totalCount == 0
		// ログ検証
		logEvents[0].level == Level.INFO
		logEvents[0].formattedMessage == "[I000001] [PetGetService]処理開始1: input={id=99999}"
		logEvents[logEvents.size() - 2].level == Level.ERROR
		logEvents[logEvents.size() - 2].formattedMessage == "[E000017] [PetGetService]処理異常終了: Pet not found with id: 99999"
		logEvents[logEvents.size() - 1].level == Level.WARN
		logEvents[logEvents.size() - 1].formattedMessage == "Resource not found: Pet not found with id: 99999"
	}
}
