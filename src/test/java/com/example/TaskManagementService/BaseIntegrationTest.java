//package com.example.TaskManagementService;
//
//import com.example.TaskManagementService.config.TestConfig;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.BeforeEach;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@Testcontainers
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Transactional
//@Rollback
//public abstract class BaseIntegrationTest {
//
//    static final PostgreSQLContainer<?> POSTGRES;
//
//    static {
//        POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
//                .withDatabaseName("testdb")
//                .withUsername("test")
//                .withPassword("test");
//        POSTGRES.start();
//    }
//
//    @DynamicPropertySource
//    static void overrideProps(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
//        registry.add("spring.datasource.username", POSTGRES::getUsername);
//        registry.add("spring.datasource.password", POSTGRES::getPassword);
//    }
//}
