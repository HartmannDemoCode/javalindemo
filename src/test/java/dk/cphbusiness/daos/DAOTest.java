package dk.cphbusiness.daos;

import dk.bugelhartmann.UserDTO;
import dk.cphbusiness.persistence.daos.ConnectorDAO;
import dk.cphbusiness.persistence.daos.DAO;
import dk.cphbusiness.persistence.model.*;
import dk.cphbusiness.security.ISecurityController;
import dk.cphbusiness.security.ISecurityDAO;
import dk.cphbusiness.security.SecurityController;
import dk.cphbusiness.security.SecurityDAO;
import dk.cphbusiness.security.entities.Role;
import dk.cphbusiness.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import dk.cphbusiness.persistence.HibernateConfig;
import lombok.SneakyThrows;
import org.hibernate.boot.jaxb.internal.stax.HbmEventReader;
import org.junit.jupiter.api.*;
import rest.TestUtils;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DAOTest {
    private static EntityManagerFactory emf;
    private static DAO<Person> personDao;
    private static DAO<Address> addressDao;
    private static DAO<Zip> zipDao;
    private static DAO<Hobby> hobbyDAO;
    private static ConnectorDAO<Person,Address> personAddressConnectorDAO;
    private static ConnectorDAO<Address,Zip> addressZipConnectorDAO;
    private static ConnectorDAO<Person,Hobby> personHobbyConnectorDAO;
    private static ISecurityDAO securityDAO;
    private static ISecurityController securityController;

    Person p1, p2, p3;
    Address a1, a2, a3;
    Phone ph1, ph2;
    Hobby h1;
    User user, admin, superUser = null;


    @BeforeAll
    static void setUpAll() {
        HibernateConfig.setTestMode(true);
        emf = HibernateConfig.getEntityManagerFactory();
        personDao = new DAO<>(Person.class, emf);
        addressDao = new DAO<>(Address.class, emf);
        hobbyDAO = new DAO<>(Hobby.class, emf);
        zipDao = new DAO<>(Zip.class, emf);
        securityDAO = new SecurityDAO(emf);
        personAddressConnectorDAO = new ConnectorDAO<>(Person.class, Address.class, emf);
        addressZipConnectorDAO = new ConnectorDAO<>(Address.class, Zip.class, emf);
        personHobbyConnectorDAO = new ConnectorDAO<>(Person.class, Hobby.class, emf);
        securityController = SecurityController.getInstance();
    }

    @AfterAll
    static void tearDownAll() {
        HibernateConfig.setTestMode(false);
    }

    @BeforeEach
    void setUp() {
        TestUtils utils = new TestUtils();
        // Create 3 users and 2 roles: user, admin and super and user and admin roles
        utils.createUsersAndRoles(emf);
        // Populate the persons with addresses, Phone and Hobbies
        Map<String, IJPAEntity> populated = utils.createPersonEntities(emf);
        p1 = (Person) populated.get("Person1");
        p2 = (Person) populated.get("Person2");
        p3 = (Person) populated.get("Person3");
        a1 = (Address) populated.get("Address1");
        a2 = (Address) populated.get("Address2");
        a3 = (Address) populated.get("Address3");
        ph1 = (Phone) populated.get("Phone1");
        ph2 = (Phone) populated.get("Phone2");
        h1 = (Hobby) populated.get("Hobby1");
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
    @DisplayName("Test that we can create Hobbies")
    void createHobbies() {
        Person toBeCreated = new Person("Kurt", "Kurtson", "kurt@mail.com", LocalDate.now());
        Hobby hobby = new Hobby("Horseback riding", Hobby.HobbyCategory.COMPETITION);
        Person person = personDao.create(toBeCreated);
        hobby.addAssociation(toBeCreated);

        System.out.println("HOBBY: "+hobby.toString());
        assert hobby.getId() != null;
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