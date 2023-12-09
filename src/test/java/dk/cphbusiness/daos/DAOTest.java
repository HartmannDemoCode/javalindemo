package dk.cphbusiness.daos;

import dk.cphbusiness.persistence.daos.ConnectorDAO;
import dk.cphbusiness.persistence.daos.DAO;
import dk.cphbusiness.persistence.model.Address;
import dk.cphbusiness.persistence.model.Person;
import dk.cphbusiness.persistence.model.Zip;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import dk.cphbusiness.persistence.HibernateConfig;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DAOTest {
    private static EntityManagerFactory emf;
    private static DAO<Person> personDao;
    private static DAO<Address> addressDao;
    private static DAO<Zip> zipDao;
    private static ConnectorDAO<Person,Address> personAddressConnectorDAO;
    private static ConnectorDAO<Address,Zip> addressZipConnectorDAO;
    Person p1, p2, p3;
    Address a1, a2, a3;

    @BeforeAll
    static void setUpAll() {
        HibernateConfig.setTestMode(true);
        emf = HibernateConfig.getEntityManagerFactory();
        personDao = new DAO<>(Person.class, emf);
        addressDao = new DAO<>(Address.class, emf);
        zipDao = new DAO<>(Zip.class, emf);
        personAddressConnectorDAO = new ConnectorDAO<>(Person.class, Address.class, emf);
        addressZipConnectorDAO = new ConnectorDAO<>(Address.class, Zip.class, emf);
    }

    @AfterAll
    static void tearDownAll() {
        HibernateConfig.setTestMode(false);
    }

    @BeforeEach
    void setUp() {
        EntityManager em = emf.createEntityManager();
        p1 = new Person("Hans", "Hansen", "hans@mail.com", LocalDate.now());
        p2 = new Person("Jette", "Hansen", "jette@mail.com", LocalDate.now());
        p3 = new Person("Freya", "Hansen", "freya@mail.com", LocalDate.now());
        Zip z1 = new Zip("2750", "Ballerup");
        Zip z2 = new Zip("3050", "Humlebæk");
        a1 = new Address("Hansvej 111",z1);
        a2 = new Address("Fjellvej 232",z2);
        p1.setAddress(a1);
        p2.setAddress(a2);
        p3.setAddress(a2);
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Phone").executeUpdate();
        em.createQuery("DELETE FROM Person").executeUpdate();
        em.createQuery("DELETE FROM Address").executeUpdate();
        em.createQuery("DELETE FROM Zip").executeUpdate();
        em.createQuery("DELETE FROM Hobby").executeUpdate();
        em.persist(p1);
        em.persist(p2);
        em.persist(p3);
        em.persist(z1);
        em.persist(z2);
        em.persist(a1);
        em.persist(a2);
        em.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Test that we can create a person")
    void create() {
        Person toBeCreated = new Person("Kurt", "Kurtson", "kurt@mail.com", LocalDate.now());
        Person person = personDao.create(toBeCreated);
        assert person.getId() != null;
    }

    @Test
    @DisplayName("Test that we can get all persons")
    void getAll() {
        assertEquals(3, personDao.getAll().size());
    }

    @Test
    @DisplayName("Test that we can get a person by id")
    void getById() {
        Person person = personDao.findById(p1.getId());
        assert person.getId() != null && person.getId().equals(p1.getId());
    }

    @Test
    @DisplayName("Test that we can update a person")
    void update() {
        Person person = personDao.findById(p1.getId());
        person.setFirstName("Hansine");
        personDao.update(person);
        Person updated = personDao.findById(p1.getId());
        assertEquals("Hansine", updated.getFirstName());
    }

    @Test
    @DisplayName("Test that we can delete a person")
    void delete() {
        Person person = personDao.findById(p1.getId());
        personDao.delete(person);
        assertEquals(2, personDao.getAll().size());
    }

    @Test
    @DisplayName("Test that we can add an address to a person")
    void addAddress() {
        Person person = personDao.findById(p1.getId());
        Zip z3 = new Zip("3400", "Hillerød");
        Address a4 = new Address("Svanevej 123",z3);
        a4 = addressDao.create(a4);

        addressZipConnectorDAO.addAssociation(a4, z3);
        personAddressConnectorDAO.addAssociation(person, a4);
        Person updated = personDao.findById(p1.getId());
        assertEquals("Svanevej 123", updated.getAddress().getStreet());
    }

    @Test
    @DisplayName("Test that we can remove an address from a person")
    void removeAddress() {
        var person = personDao.findById(p1.getId());
        var address = addressDao.findById(a1.getId());
        personAddressConnectorDAO.removeAssociation(person, address);
        person = personDao.findById(p1.getId());
        assert person.getAddress() == null;
    }

}