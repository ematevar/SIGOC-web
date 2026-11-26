package com.sigoc.sigoc_web;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Funciones de aptitud arquitectónica (Fitness Functions) que verifican automáticamente
 * las invariantes de la Arquitectura Hexagonal en cada compilación.
 */
@AnalyzeClasses(packages = "com.sigoc.sigoc_web", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArquitecturaHexagonalFitnessTest {

    @ArchTest
    static final ArchRule dominioNoDependeDeAplicacionNiInfraestructuraNiFramework =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..application..",
                            "..infrastructure..",
                            "org.springframework..",
                            "jakarta.."
                    );

    @ArchTest
    static final ArchRule aplicacionNoDependeDeInfraestructuraNiFramework =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..infrastructure..",
                            "org.springframework..",
                            "jakarta.."
                    );

    @ArchTest
    static final ArchRule controladoresNoDependenDeCasosDeUsoConcretos =
            noClasses().that().resideInAPackage("..infrastructure.controllers..")
                    .should().dependOnClassesThat().resideInAPackage("..application.usecases..");

    @ArchTest
    static final ArchRule controladoresNoDependenDeRepositoriosNiEntidades =
            noClasses().that().resideInAPackage("..infrastructure.controllers..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..infrastructure.repositories..",
                            "..infrastructure.entities.."
                    );

    @ArchTest
    static final ArchRule controladoresNoDeclaranTransactional =
            methods().that().areDeclaredInClassesThat().resideInAPackage("..infrastructure.controllers..")
                    .should().notBeAnnotatedWith(Transactional.class);
}
