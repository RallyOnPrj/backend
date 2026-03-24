package com.gumraze.rallyon.backend.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.gumraze.rallyon.backend",
        importOptions = ImportOption.DoNotIncludeTests.class,
        cacheMode = CacheMode.FOREVER
)
class ModuleArchitectureTest {

    @ArchTest
    static final ArchRule non_region_modules_must_not_depend_on_region_internal =
            noClasses().that().resideInAnyPackage(
                            "..user..",
                            "..identity..",
                            "..courtManager.."
                    )
                    .should().dependOnClassesThat().resideInAnyPackage("..region.internal..");

    @ArchTest
    static final ArchRule non_identity_modules_must_not_depend_on_identity_oauth_adapters =
            noClasses().that().resideInAnyPackage(
                            "..user..",
                            "..courtManager..",
                            "..region.."
                    )
                    .should().dependOnClassesThat().resideInAnyPackage("..identity.adapter.out.oauth..");

    @ArchTest
    static final ArchRule non_identity_modules_must_not_depend_on_security_configuration_or_filters =
            noClasses().that().resideInAnyPackage(
                            "..user..",
                            "..courtManager..",
                            "..region.."
                    )
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..security.config..",
                            "..security.web.."
                    );

    @ArchTest
    static final ArchRule security_must_not_depend_on_identity =
            noClasses().that().resideInAnyPackage("com.gumraze.rallyon.backend.security..")
                    .should().dependOnClassesThat().resideInAnyPackage("..identity..");

    @ArchTest
    static final ArchRule application_layers_must_not_depend_on_repositories =
            noClasses().that().resideInAnyPackage(
                            "..user.application..",
                            "..identity.application..",
                            "..courtManager.application.."
                    )
                    .should().dependOnClassesThat().resideInAnyPackage("..repository..");

    @ArchTest
    static final ArchRule legacy_auth_and_user_service_packages_must_not_have_service_annotations =
            classes().that().resideInAnyPackage(
                            "..auth.service..",
                            "..auth.controller..",
                            "..user.service..",
                            "..user.controller.."
                    )
                    .should().notBeAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule legacy_auth_and_user_service_packages_must_not_have_rest_controller_annotations =
            classes().that().resideInAnyPackage(
                            "..auth.service..",
                            "..auth.controller..",
                            "..user.service..",
                            "..user.controller.."
                    )
                    .should().notBeAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule no_classes_should_depend_on_legacy_auth_package =
            noClasses().should().dependOnClassesThat().resideInAnyPackage("..auth..");
}
