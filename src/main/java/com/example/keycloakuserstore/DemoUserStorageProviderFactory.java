package com.example.keycloakuserstore;

import com.example.keycloakuserstore.dao.UserDAO;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;
import lombok.extern.jbosslog.JBossLog;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.*;

/**
 * DemoUserStorageProviderFactory
 */
@JBossLog
public class DemoUserStorageProviderFactory implements UserStorageProviderFactory<DemoUserStorageProvider> {

    Map properties;
    EntityManagerFactory entityManagerFactory;

    @Override
    public void init(Config.Scope config) {
  
      // this configuration is pulled from the SPI configuration of this provider in the standalone[-ha] / domain.xml
      // see setup.cli
        properties = new HashMap();
        properties.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        properties.put("hibernate.connection.url", "jdbc:mysql://localhost/jee_test");
        properties.put("hibernate.connection.username", "root");
        properties.put("hibernate.connection.password", "root");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show-sql", "true");
        properties.put("hibernate.archive.autodetection", "class, hbm");
        properties.put("hibernate.hbm2ddl.auto", "create");

        /*entityManagerFactory = Persistence.createEntityManagerFactory("h2userstorage");*/
        entityManagerFactory = new HibernatePersistenceProvider().createContainerEntityManagerFactory(getPersistenceUnitInfo("h2userstorage"), properties);
        /*entityManagerFactory = Persistence.createEntityManagerFactory("h2userstorage",properties);*/

      String someProperty = config.get("someProperty");
      log.infov("Configured {0} with someProperty: {1}", this, someProperty);
    }

    private PersistenceUnitInfo getPersistenceUnitInfo(String name) {
        return new PersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return name;
            }

            @Override
            public String getPersistenceProviderClassName() {
                return "org.hibernate.jpa.HibernatePersistenceProvider";
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }

            @Override
            public DataSource getJtaDataSource() {
                return null;
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return null;
            }

            @Override
            public List<String> getMappingFileNames() {
                return Collections.emptyList();
            }

            @Override
            public List<URL> getJarFileUrls() {
                try {
                    Enumeration<URL> t = this.getClass()
                            .getClassLoader()
                            .getResources("");
                    while(t.hasMoreElements()) {
                        System.err.println(t.nextElement());
                    }
                    return Collections.list(this.getClass()
                            .getClassLoader()
                            .getResources(""));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public URL getPersistenceUnitRootUrl() {
                return null;
            }

            @Override
            public List<String> getManagedClassNames() {
                List<String> managedClasses = new LinkedList<>();
                managedClasses.add("com.example.keycloakuserstorage.models.User");
                return managedClasses;
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return false;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return null;
            }

            @Override
            public ValidationMode getValidationMode() {
                return null;
            }

            @Override
            public Properties getProperties() {
                return new Properties();
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public void addTransformer(ClassTransformer transformer) {

            }

            @Override
            public ClassLoader getNewTempClassLoader() {
                return null;
            }
        };
    }

    @Override
    public DemoUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        log.infov("Create new provider {0}", "demo-storage");
        UserDAO userDAO = new UserDAO(/*entityManagerFactory.createEntityManager()*/ entityManagerFactory.createEntityManager());
        return new DemoUserStorageProvider(session, model, userDAO);
    }

    @Override
    public String getId() {
        return "demo-user-provider";
    }
}