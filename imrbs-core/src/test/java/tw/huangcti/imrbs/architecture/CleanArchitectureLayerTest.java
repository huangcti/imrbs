package tw.huangcti.imrbs.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Clean Architecture 層級依賴規則測試.
 * 
 * <p>確保專案遵循 Clean Architecture 的依賴方向：</p>
 * <ul>
 *   <li>Domain 層不依賴任何外部層</li>
 *   <li>Application 層只依賴 Domain 層</li>
 *   <li>Infrastructure 層可以依賴 Domain 和 Application 層</li>
 *   <li>Web 層可以依賴所有內部層</li>
 * </ul>
 */
@DisplayName("Clean Architecture 層級依賴規則測試")
class CleanArchitectureLayerTest {

    private static JavaClasses importedClasses;

    private static final String BASE_PACKAGE = "tw.huangcti.imrbs";
    private static final String DOMAIN_PACKAGE = BASE_PACKAGE + ".domain..";
    private static final String APPLICATION_PACKAGE = BASE_PACKAGE + ".application..";
    private static final String INFRASTRUCTURE_PACKAGE = BASE_PACKAGE + ".infrastructure..";
    private static final String WEB_PACKAGE = BASE_PACKAGE + ".web..";

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(location -> !location.contains("$") 
                        || !location.contains("Builder"))  // 排除 Lombok Builder 生成的內部類別
                .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("Domain 層規則")
    class DomainLayerRules {

        @Test
        @DisplayName("Domain 層不應依賴 Application 層")
        void domainShouldNotDependOnApplication() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(APPLICATION_PACKAGE)
                    .because("Domain 層是最內層，不應依賴 Application 層");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Domain 層不應依賴 Infrastructure 層")
        void domainShouldNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Domain 層不應依賴 Infrastructure 層");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Domain 層不應依賴 Web 層")
        void domainShouldNotDependOnWeb() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(WEB_PACKAGE)
                    .because("Domain 層不應依賴 Web 層");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Domain 層不應使用 Spring 框架註解")
        void domainShouldNotUseSpringAnnotations() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework..")
                    .because("Domain 層應保持框架無關");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Application 層規則")
    class ApplicationLayerRules {

        @Test
        @DisplayName("Application 層不應依賴 Infrastructure 層")
        void applicationShouldNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Application 層不應依賴 Infrastructure 層的具體實作");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Application 層不應依賴 Web 層")
        void applicationShouldNotDependOnWeb() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(WEB_PACKAGE)
                    .because("Application 層不應依賴 Web 層");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Application 層可以依賴 Domain 層")
        void applicationCanDependOnDomain() {
            ArchRule rule = classes()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            DOMAIN_PACKAGE,
                            APPLICATION_PACKAGE,
                            "java..",
                            "jakarta..",
                            "lombok..",
                            "org.slf4j.."
                    )
                    .because("Application 層只應依賴 Domain 層和基本 Java 類別");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Infrastructure 層規則")
    class InfrastructureLayerRules {

        @Test
        @DisplayName("Infrastructure 層不應依賴 Web 層")
        void infrastructureShouldNotDependOnWeb() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(WEB_PACKAGE)
                    .allowEmptyShould(true)
                    .because("Infrastructure 層不應依賴 Web 層");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Web 層規則")
    class WebLayerRules {

        @Test
        @DisplayName("只有 Web 層可以使用 @RestController")
        void onlyWebLayerShouldHaveRestControllers() {
            ArchRule rule = classes()
                    .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().resideInAPackage(WEB_PACKAGE)
                    .allowEmptyShould(true)
                    .because("@RestController 只應出現在 Web 層");

            rule.check(importedClasses);
        }
    }
}
