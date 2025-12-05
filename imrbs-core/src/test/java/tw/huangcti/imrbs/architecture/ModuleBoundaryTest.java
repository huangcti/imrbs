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
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * 模組邊界規則測試.
 * 
 * <p>確保各模組之間的依賴關係正確：</p>
 * <ul>
 *   <li>imrbs-core: 核心業務邏輯，不依賴其他模組</li>
 *   <li>imrbs-infrastructure: 基礎設施層，依賴 core</li>
 *   <li>imrbs-web: Web API 層，依賴 core 和 infrastructure</li>
 * </ul>
 */
@DisplayName("模組邊界規則測試")
class ModuleBoundaryTest {

    private static JavaClasses importedClasses;

    private static final String BASE_PACKAGE = "tw.huangcti.imrbs";

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(location -> !location.contains("$") 
                        || !location.contains("Builder"))  // 排除 Lombok Builder 生成的內部類別
                .importPackages(BASE_PACKAGE);
    }

    @Test
    @DisplayName("分層架構應符合 Clean Architecture 規範")
    void layeredArchitectureShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()  // 只檢查層級之間的依賴，忽略外部庫
                // 定義層級 (Infrastructure 和 Web 在其他模組中，此處設為可選)
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .optionalLayer("Infrastructure").definedBy("..infrastructure..")
                .optionalLayer("Web").definedBy("..web..")
                // 定義允許的依賴
                .whereLayer("Domain").mayNotAccessAnyLayer()
                .whereLayer("Application").mayOnlyAccessLayers("Domain")
                .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application")
                .whereLayer("Web").mayOnlyAccessLayers("Domain", "Application", "Infrastructure")
                .because("應遵循 Clean Architecture 的依賴方向");

        rule.check(importedClasses);
    }

    @Nested
    @DisplayName("UseCase 規則")
    class UseCaseRules {

        @Test
        @DisplayName("UseCase 類別應以 UseCase 結尾")
        void useCaseClassesShouldEndWithUseCase() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..application.usecase..")
                    .and().areNotInterfaces()
                    .and().areTopLevelClasses()  // 只檢查頂層類別，排除內部類別
                    .should().haveSimpleNameEndingWith("UseCase")
                    .because("UseCase 類別應以 UseCase 結尾");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("UseCase 類別應為 public")
        void useCaseClassesShouldBePublic() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..application.usecase..")
                    .and().haveSimpleNameEndingWith("UseCase")
                    .should().bePublic()
                    .because("UseCase 類別應為 public 以便被外部呼叫");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Repository 規則")
    class RepositoryRules {

        @Test
        @DisplayName("Repository 介面應定義在 Domain 層")
        void repositoryInterfacesShouldBeInDomainLayer() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Repository")
                    .and().areInterfaces()
                    .should().resideInAPackage("..domain.repository..")
                    .because("Repository 介面應定義在 Domain 層");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Repository 實作應在 Infrastructure 層")
        void repositoryImplementationsShouldBeInInfrastructureLayer() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("RepositoryImpl")
                    .or().haveSimpleNameEndingWith("JpaRepository")
                    .should().resideInAPackage("..infrastructure..")
                    .allowEmptyShould(true)
                    .because("Repository 實作應在 Infrastructure 層");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Controller 規則")
    class ControllerRules {

        @Test
        @DisplayName("Controller 類別應以 Controller 結尾")
        void controllerClassesShouldEndWithController() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..web.controller..")
                    .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().haveSimpleNameEndingWith("Controller")
                    .allowEmptyShould(true)
                    .because("Controller 類別應以 Controller 結尾");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Controller 不應直接呼叫 Repository")
        void controllersShouldNotCallRepositoriesDirectly() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..domain.repository..")
                    .allowEmptyShould(true)
                    .because("Controller 應透過 UseCase 存取資料，不應直接呼叫 Repository");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Entity 規則")
    class EntityRules {

        @Test
        @DisplayName("Domain Entity 不應使用 JPA 註解")
        void domainEntitiesShouldNotUseJpaAnnotations() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain.model..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("jakarta.persistence..", "javax.persistence..")
                    .because("Domain Entity 應保持與 JPA 無關");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Service 規則")
    class ServiceRules {

        @Test
        @DisplayName("Domain Service 應在 domain.service 套件中")
        void domainServicesShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("DomainService")
                    .should().resideInAPackage("..domain.service..")
                    .allowEmptyShould(true)
                    .because("Domain Service 應在正確的套件中");

            rule.check(importedClasses);
        }
    }
}
