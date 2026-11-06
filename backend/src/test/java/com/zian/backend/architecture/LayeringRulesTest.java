package com.zian.backend.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 分层架构守卫：domain 纯净、application 不碰基础设施、interfaces 不直连 Mapper。
 */
@AnalyzeClasses(packages = "com.zian.backend", importOptions = ImportOption.DoNotIncludeTests.class)
class LayeringRulesTest {

    @ArchTest
    static final ArchRule domain_should_be_framework_free = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework..",
                    "org.apache.ibatis..",
                    "com.baomidou..",
                    "com.fasterxml.jackson..",
                    "org.apache.ibatis.annotations..")
            .because("domain 层必须是纯 Java，不依赖 Spring / MyBatis / Jackson");

    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("应用层只能通过领域接口访问持久化，不能直接依赖 infrastructure 实现");

    @ArchTest
    static final ArchRule interfaces_should_not_depend_on_infrastructure = noClasses()
            .that()
            .resideInAPackage("..interfaces..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("Controller 只能调用应用服务，禁止直接使用 Mapper / PO / 仓储实现");

    @ArchTest
    static final ArchRule application_should_not_depend_on_controllers = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat(
                    DescribedPredicate.<com.tngtech.archunit.core.domain.JavaClass>not(
                                    resideInAPackage("..interfaces.dto.."))
                            .and(resideInAPackage("..interfaces..")))
            .because("应用服务不能反向依赖 Controller（DTO 除外）");
}
