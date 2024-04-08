package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.entity.*;
import org.hibernate.jpa.HibernatePersistenceProvider;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("unit");

    private static <T> T doInSession(final Function<EntityManager, T> function) {
        var em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            T result = function.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (final Exception e) {
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
        doInSession((em) -> {
            System.out.println("Two types of one-to-one relationships");
            //Classic one-to-one relationship
            var user = new User()
                    .setUsername("john_doe")
                    .setAddress(
                            new Address()
                                    .setCity("Kyiv")
                                    .setHouseNumber(1)
                                    .setStreet("Street")
                    );
            //One-to-one relationship using @MapsId
            user.setProfile(
                    new Profile()
                            .setPhotoUrl("https://example.com/photo.jpg")
                            .setActive(true)
            );
           /*
                If I had found the user, then I could have set a new profile. Due to dirty checking and cascade type
                "PERSIST" it would be generated a "persist" action and saved into the DB.
           */
            em.persist(user);
        });

        doInSession((em) -> {
            //Many-to-many
            System.out.println("Many-to-many including self-joining many-to-many");
            var employee1 = new Employee() //manager
                    .setName("John");
            var employee2 = new Employee() //subordinate
                    .setName("Jane");

            employee2.getManagers().add(employee1);

            var guild1 = new Guild()
                    .setName("G22");

            var guild2 = new Guild()
                    .setName("G33");

            guild1.addEmployee(employee1);
            guild1.addEmployee(employee2);
            guild2.addEmployee(employee1);
            guild2.addEmployee(employee2);

            em.persist(guild1);
            em.persist(guild2);
        });


        doInSession(em -> {
           /*
                3 queries: one for retrieving the employee, one (later) for retrieving the managers and one (later) for retrieving the subordinates.
                Guilds are loaded at once in the main query (it depends), because the fetch type is "EAGER".
                There are two options: loading at once and later. The eager one loads all the data at once, while
                the lazy one loads it later. Eager loading can load much redundant data (it can pull data from other nested
                tables recursively). It is challenging to establish the correct way of loading.
           */
            System.out.println("Retrieving an employee");
            var employee = em.find(Employee.class, 1L);
            System.out.println("Id of employee: " + employee.getId());
            System.out.println("Name of employee: " + employee.getName());
            System.out.println("Printing all guilds of employee: ");
            List<Guild> guilds = employee.getGuilds();
            guilds.forEach(o -> System.out.println(o.getName()));

            System.out.println("Printing all managers of employee: ");
            Set<Employee> managers = employee.getManagers();
            managers.forEach(o -> System.out.println(o.getName()));
            if (managers.isEmpty()) {
                System.out.println("There are no managers");
            }

            System.out.println("Printing all subordinates of employee: ");
            Set<Employee> subordinates = employee.getSubordinates();
            subordinates.forEach(o -> System.out.println(o.getName()));
        });

        doInSession((em) -> {
            //One query
            System.out.println("Retrieving a user using fetch type EAGER");
            var user = em.find(User.class, 1L);
            System.out.println("Id of user: " + user.getId());
            System.out.println("Name of user: " + user.getUsername());
            var profile = user.getProfile();
            System.out.println(profile.getPhotoUrl());
            var address = user.getAddress();
            System.out.println(address.getCity());
        });

        doInSession((em) -> {
            System.out.println("Retrieving all employees and their managers. N+1 problem"); //Redundancy of queries
            //Firstly, SQL queries are generated, JDBC connection is got, the queries are sent via the network, and the result is received using mappings.
            //Setting a fetch type "EAGER" in the entity won't be a solution. If we know we have to load all data, we should use "EAGER".
            //It is related to lazy and eager (collection) (the type influences on findById method, not on a custom query during the problem) loading.
            //N+1 problem says all employees are loaded at once, and then all guilds are loaded while invoking.
            //When the type is eager, there is still a problem. We need to load all data at once using "fetch".
            var employees = em.createQuery(
                    "select distinct e from Employee e", Employee.class
            ).getResultList();

            employees.stream().map(Employee::getGuilds)
                    .flatMap(Collection::stream)
                    .map(Guild::getName)
                    .forEach(System.out::println);
        });

        doInSession((em) -> {
            System.out.println("Retrieving all employees and their managers. Without N+1 problem");
            var employees = em.createQuery(
                    "select distinct e from Employee e " +
                            "inner join fetch e.guilds", Employee.class
            ).getResultList();

            employees.stream().map(Employee::getGuilds)
                    .flatMap(Collection::stream)
                    .map(Guild::getName)
                    .forEach(System.out::println);
        });

        doInSession((em) -> { //Type "EAGER" works fine.
            System.out.println("Retrieving an employee and his guilds");
            var employee = em.find(Employee.class, 1L);
            System.out.println(employee.getGuilds()
                    .stream()
                    .map(Guild::getName)
                    .collect(Collectors.joining(",")));
        });
    }
}