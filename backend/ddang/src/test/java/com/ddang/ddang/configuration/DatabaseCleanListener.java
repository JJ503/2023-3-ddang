package com.ddang.ddang.configuration;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import java.util.List;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DatabaseCleanListener extends AbstractTestExecutionListener {

    @Override
    public void afterTestMethod(final TestContext testContext) {
        final EntityManager em = findEntityManager(testContext);
        final List<String> tableNames = calculateTableNames(em);

        clean(em, tableNames);
    }

    private EntityManager findEntityManager(final TestContext testContext) {
        return testContext.getApplicationContext()
                          .getBean(EntityManager.class);
    }

    private List<String> calculateTableNames(final EntityManager em) {
        return em.getMetamodel()
                 .getEntities()
                 .stream()
                 .filter(entityType -> entityType.getJavaType().getAnnotation(Entity.class) != null)
                 .map(this::calculateTableName)
                 .toList();
    }

    private String calculateTableName(final EntityType<?> entityType) {
        final Table tableAnnotation = entityType.getJavaType().getAnnotation(Table.class);

        if (tableAnnotation != null) {
            return tableAnnotation.name().toLowerCase();
        }

        return convertToSnakeCase(entityType.getName());
    }

    private String convertToSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }


    private void clean(final EntityManager em, final List<String> tableNames) {
        em.flush();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        for (final String tableName : tableNames) {
            em.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            em.createNativeQuery("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1").executeUpdate();
        }

        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }
}
