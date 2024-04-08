package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
        doInSession(em -> { //case 1
            var person = new Person();
            person.setFirstName("John");
            person.setLastName("Doe");
            person.setEmail("john_doe@example.com");

            var note = new Note()
                    .setText("My first note");
            person.getNotes().add(
                    note
            );

            System.out.println("Persisting a person");
            em.persist(person);
            System.out.println("Persisting a note");
            em.persist(note);
        });

        doInSession(em -> { //case 2
            var user = new User();
            user.setFirstName("John");
            user.setLastName("Doe");

            System.out.println("Persisting an user");
            em.persist(user);

            var role = new Role();
            role.setName("admin");
            role.setUser(user);

            System.out.println("Persisting a role");
            em.persist(role);
        });

        doInSession(em -> { //case 3
            final List<Passport> passports = new ArrayList<>(
                    Arrays.asList(
                            new Passport().setNumber("54321"),
                            new Passport().setNumber("67890")
                    )
            );

            var human = new Human();
            human.setCountry("USA");
            human.addPassports(passports);

            System.out.println("Persisting a human");
            em.persist(human);
            System.out.println("Persisting passports");
            passports.forEach(em::persist);
        });

        doInSession(em -> { //Passports are retrieved by human id (2 queries).
            System.out.println("Retrieving a human");
            var human = em.find(Human.class, 1L);
            System.out.println("Retrieving passports");
            human.getPassports().forEach(
                    System.out::println
            );
        });

        doInSession(em -> {
            /*
                During comparing Hibernate notices that the collection has been changed, and a cascade is set.
                Two mechanisms "Dirty Checking" (checks if the entity has been changed) and cascading.
             */
            System.out.println("\033[1;93m" + "Retrieving a human");
            var human = em.find(Human.class, 1L);
            var passport = new Passport()
                    .setNumber("53900");

            System.out.println("\033[1;93m" + "Persisting a passport");
            human.addPassport(passport);
            //No need to persist the passport.
        });

        System.out.println("Different types of persisting");
        doInSession(em -> {
            System.out.println("1. persist(newPerson) - persist new person with new notes, so notes are inserted due to cascade");
            var passport = new Passport()
                    .setNumber("67903");

            var human = new Human()
                    .setCountry("Italy")
                    .setPassports(List.of(
                            passport
                    ));

            passport.setHuman(human);

            em.persist(human);
        });

        doInSession(em -> {
            System.out.println("2. persist(newNote) - persist new note linked to the existing person");

            var human = em.find(Human.class, 1L);
            var passport = new Passport()
                    .setNumber("45104")
                    .setHuman(human);

            em.persist(passport);
        });

        doInSession(em -> {
            //In the case, Hibernate notices that the collection has been changed and a cascade type is "Persist".
            System.out.println("3. no persist - create new note and add it to the existing person");
            var human = em.find(Human.class, 2L);
            human.addPassport(new Passport().setNumber("09135"));
        });

        doInSession(em -> {
            //A proxy is created, sets the id inside it and returns the proxy with the person. Hibernate doesn't load from the database
            System.out.println("4. * persist new note by person's id without loading the person to the session");
            var human = em.getReference(Human.class, 2L);
            var passport = new Passport()
                    .setNumber("90391")
                    .setHuman(human);
            em.persist(passport);
        });
    }
}
