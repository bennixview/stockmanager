package com.bewi.stockmanager.portfolio;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.jmolecules.archunit.JMoleculesDddRules;
import org.jmolecules.ddd.annotation.Repository;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * The architecture, as an executable rule rather than a diagram in a wiki.
 *
 * <p>These are the constraints the whole design rests on: the domain owes nothing to a framework,
 * the application layer talks to the outside only through its own ports, and this system never
 * reaches into the other self-contained system's code.
 */
@AnalyzeClasses(packages = "com.bewi.stockmanager.portfolio",
        importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule theHexagonPointsInwards = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("com.bewi.stockmanager.portfolio.domain..")
            .layer("Application").definedBy("com.bewi.stockmanager.portfolio.application..")
            .layer("Adapters").definedBy("com.bewi.stockmanager.portfolio.adapter..")
            .layer("Configuration").definedBy("com.bewi.stockmanager.portfolio.config..",
                    "com.bewi.stockmanager.portfolio")

            .whereLayer("Configuration").mayNotBeAccessedByAnyLayer()
            .whereLayer("Adapters").mayOnlyBeAccessedByLayers("Configuration")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapters", "Configuration")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapters", "Configuration");

    @ArchTest
    static final ArchRule theDomainKnowsNoFramework = noClasses()
            .that().resideInAPackage("..portfolio.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "com.fasterxml..", "tools.jackson..",
                    "org.hibernate..", "org.slf4j..")
            .because("the domain model has to be usable, and testable, without any of it");

    @ArchTest
    static final ArchRule theApplicationLayerKnowsNoFramework = noClasses()
            .that().resideInAPackage("..portfolio.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "com.fasterxml..", "tools.jackson..",
                    "org.hibernate..")
            .because("use cases are wired in the configuration, not annotated into a container");

    @ArchTest
    static final ArchRule useCasesAreReachedThroughPorts = noClasses()
            .that().resideInAPackage("..portfolio.adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..portfolio.application.service..")
            .because("driving adapters depend on the inbound ports, never on their implementations");

    @ArchTest
    static final ArchRule nothingReachesIntoTheOtherSelfContainedSystem = noClasses()
            .that().resideInAPackage("com.bewi.stockmanager.portfolio..")
            .should().dependOnClassesThat().resideInAPackage("com.bewi.stockmanager.marketdata..")
            .because("self-contained systems integrate over HTTP and links, not over a shared classpath");

    @ArchTest
    static final ArchRule thereAreNoCycles = slices()
            .matching("com.bewi.stockmanager.portfolio.(*)..")
            .should().beFreeOfCycles();

    /** The jMolecules building blocks have rules of their own; this checks we honour them. */
    @ArchTest
    static final ArchRule dddBuildingBlocksAreUsedCorrectly = JMoleculesDddRules.all();

    @ArchTest
    static final ArchRule repositoriesAreDomainPorts = classes()
            .that().areAnnotatedWith(Repository.class)
            .should().beInterfaces()
            .andShould().resideInAPackage("..portfolio.domain..")
            .because("a repository is a domain concept; where the rows live is the adapter's business");

    @ArchTest
    static final ArchRule dependenciesAreInjectedThroughConstructors = noFields()
            .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
            .because("field injection hides what a class needs and cannot be final");
}
