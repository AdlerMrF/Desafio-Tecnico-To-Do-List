package com.adler.todolist;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/** Garante, a cada build, que as dependências entre as camadas seguem a Onion Architecture. */
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.adler.todolist");
    }

    @Test
    void asCamadasDevemSeguirAOnionArchitecture() {
        onionArchitecture()
                .domainModels("..domain.model..", "..domain.exception..", "..domain.query..")
                .domainServices("..domain.repository..")
                .applicationServices("..application..")
                .adapter("persistencia", "..infrastructure..")
                .adapter("web", "..web..")
                .check(classes);
    }

    @Test
    void oDominioEAAplicacaoNaoDevemDependerDeFrameworks() {
        noClasses()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "jakarta.servlet..",
                        "org.hibernate..")
                .check(classes);
    }
}
