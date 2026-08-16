package com.example.petstore.mgmt.integration

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.spock.Testcontainers

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import spock.lang.Shared
import spock.lang.Specification

/**
 * 統合テストのベースクラス（Groovy版）
 * commonライブラリで提供され、各サービスで再利用可能
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseIntegrationSpec extends Specification {

	@LocalServerPort
	protected int port

	@Shared
	protected TestRestTemplate restTemplate = new TestRestTemplate()

	@Shared
	protected Flyway flyway

	// DBアサート用のJdbcTemplate
	@Autowired
	protected JdbcTemplate jdbcTemplate


	// デフォルトのMySQLコンテナ（各サービスでオーバーライド可能）
	@Shared
	static protected MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.0.46")
	.withDatabaseName("testdb")
	.withUsername("test")
	.withPassword("test")
	.withReuse(true)


	// ========== TestContainers設定 ==========

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		// 各サービスでオーバーライド可能
		registry.add("spring.datasource.url", mySQLContainer.&getJdbcUrl)
		registry.add("spring.datasource.username", mySQLContainer.&getUsername)
		registry.add("spring.datasource.password", mySQLContainer.&getPassword)
	}

	// ========== セットアップ/クリーンアップ ==========

	@SuppressWarnings("GroovyUnusedDeclaration")
	def setupSpec() {
		mySQLContainer.start()

		// db/migration: テーブル定義
		// db/sql: テストデータ投入
		flyway = Flyway.configure()
				.dataSource(mySQLContainer.getJdbcUrl(), mySQLContainer.getUsername(), mySQLContainer.getPassword())
				.locations("classpath:db/migration", "classpath:db/sql")
				.baselineOnMigrate(true)
				.cleanDisabled(false)
				.load()
		flyway.migrate()
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	def cleanup() {
		if (flyway != null) {
			flyway.clean()
			flyway.migrate()
		}
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
	def cleanupSpec() {
		if (mySQLContainer != null && mySQLContainer.isRunning()) {
			mySQLContainer.stop()
		}
	}

	// ========== ユーティリティメソッド ==========

	protected String getBaseUrl() {
		return "http://localhost:${port}/api/v1"
	}

	/**
	 * ログイベント（オブジェクト）をまるごとキャプチャする共通ヘルパー
	 */
	protected static List<ILoggingEvent> captureLogEvents(String loggerName = "com.example.petstore", Closure block) {
		Logger logger = (Logger) LoggerFactory.getLogger(loggerName)
		def appender = new ListAppender<ILoggingEvent>()
		appender.start()
		logger.addAppender(appender)

		try {
			block.call()
			return appender.list // イベントオブジェクトのリストをそのまま返す
		} finally {
			logger.detachAppender(appender)
		}
	}
}
