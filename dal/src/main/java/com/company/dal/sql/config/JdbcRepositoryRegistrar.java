package com.company.dal.sql.config;

import com.company.dal.sql.annotation.EnableJdbcRepositories;
import com.company.dal.sql.annotation.UseClasspathSql;
import com.company.dal.sql.factory.JdbcRepositoryFactoryBean;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

public class JdbcRepositoryRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(
      AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    Map<String, Object> attributes =
        importingClassMetadata.getAnnotationAttributes(EnableJdbcRepositories.class.getName());

    String[] basePackages = (String[]) attributes.get("basePackages");

    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false) {

          @Override
          protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isIndependent()
                && beanDefinition.getMetadata().isInterface();
          }
        };

    scanner.addIncludeFilter(new AnnotationTypeFilter(UseClasspathSql.class));

    for (String basePackage : basePackages) {
      Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);

      for (BeanDefinition candidate : candidates) {
        String className = candidate.getBeanClassName();

        try {
          Class<?> repositoryType =
              ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());

          BeanDefinition definition =
              BeanDefinitionBuilder.genericBeanDefinition(JdbcRepositoryFactoryBean.class)
                  .addPropertyValue("repositoryInterface", repositoryType)
                  .getBeanDefinition();

          // Fully-qualified name prevents clashes between same-named interfaces.
          registry.registerBeanDefinition(repositoryType.getName(), definition);

        } catch (ClassNotFoundException exception) {
          throw new IllegalStateException("Could not load JDBC repository " + className, exception);
        }
      }
    }
  }
}
