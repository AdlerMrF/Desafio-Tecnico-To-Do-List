package com.adler.todolist.bdd;

import com.adler.todolist.TestcontainersConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class CucumberSpringConfiguration {
}