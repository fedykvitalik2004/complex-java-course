package org.example;

import jakarta.persistence.*;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import org.example.entity.Person;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.sql.DataSource;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Main {
    //This is an entry point into JPA and Hibernate
    //This is a JPA's interface
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("unit");

    private static <T> T doInSession(final Function<EntityManager, T> function) {
        final EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            final T t = function.apply(em);
            em.getTransaction().commit();
            return t;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private static void doInSession(final Consumer<EntityManager> function) {
        doInSession(em -> {
            function.accept(em);
            return null;
        });
    }

    public static void main(String[] args) {
        System.out.println("Persisting a person inside a session");
        doInSession(em -> {
            var person = new Person();
            person.setName("Vitalii");
            person.setAge(28);
            em.persist(person);
        });

        System.out.println("Persisting with a runnable"); //It does not work.
        ((Runnable) () -> {
            try (var em = emf.createEntityManager()) {
                var person = new Person();
                person.setName("Mykyta");
                person.setAge(16);
                em.persist(person);
            }
        }).run();

        System.out.println("Finding a person inside a session");
        var person = doInSession(em -> {
            return em.find(Person.class, 1L);
        });


        System.out.println("Finding with a supplier");
        System.out.println(((Supplier<Person>) () -> {
            try (var em = emf.createEntityManager()) {
                return em.find(Person.class, 1L); //Hibernate creates a prepared statement to find a person
            }
        }).get());

        System.out.println("Removing a person with a runnable"); //It doesn't work
        ((Runnable) () -> {
            try (var em = emf.createEntityManager()) {
                em.remove(em.find(Person.class, 1L));
            }
        }).run();

        System.out.println("Updating a person with a runnable"); //It doesn't work without a transaction
        ((Runnable) () -> {
            try (var em = emf.createEntityManager()) {
                em.find(Person.class, 1L)
                        .setAge(30);
            }
        }).run();

        System.out.println("Updating a person inside a session");
        doInSession(em -> {
            em.find(Person.class, 1L)
                    .setAge(30);
        });

        System.out.println("Finding using a JPQL query");
        doInSession(em -> {
            System.out.println(em.createQuery(
                    "SELECT p FROM Person p " +
                            "WHERE p.name = :name",
                            Person.class) //It is similar to SQL, because it uses JPQL and classes
                    .setParameter("name", "Vitalii")
                    .getSingleResult()
            );
        });

        System.out.println("Finding using a native query");
        doInSession(em -> { //Native SQL
            em.createNativeQuery("SELECT * FROM persons AS p " +
                            "WHERE p.name = :name ", Person.class)
                    .setParameter("name", "Vitalii")
                    .getSingleResult();//It is similar to SQL, because it uses JPQL and classes
        });
        //Можна також створювати EntityManagerFactory через обʼєкт PersistenceProvider.
        //HibernatePersistenceProvider implements PersistenceProvider, which can create an EntityManagerFactory
/*        var containerEntityManagerFactory = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(new PersistenceUnitInfo() {
                    @Override
                    public String getPersistenceUnitName() {
                        return null;
                    }

                    @Override
                    public String getPersistenceProviderClassName() {
                        return null;
                    }

                    @Override
                    public PersistenceUnitTransactionType getTransactionType() {
                        return null;
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
                        return null;
                    }

                    @Override
                    public List<URL> getJarFileUrls() {
                        return null;
                    }

                    @Override
                    public URL getPersistenceUnitRootUrl() {
                        return null;
                    }

                    @Override
                    public List<String> getManagedClassNames() {
                        return null;
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
                        return null;
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
                }, new HashMap<>());*/
    }
}