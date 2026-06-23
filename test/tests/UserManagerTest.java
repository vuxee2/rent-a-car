package tests;

import manager.UserManager;
import model.Agent;
import model.Client;
import model.enums.ClientCategory;
import model.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.InMemoryRepositories.FakeUserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager um;

    @BeforeEach
    void setUp() {
        um = new UserManager(new FakeUserRepository());
    }

    private Agent agent(String id, int years) {
        return new Agent(id, "A", "B", Gender.MALE, LocalDate.of(1990, 1, 1), "1", "a",
                "u" + id, "p", "MASTER", years, 60000, 1.0);
    }

    @Test
    void plataSeRacunaPoFormuli() {
        Agent a = agent("ag1", 10);
        um.registerEmployee(a);
        // osnova * (koeficijent + 0.004 * godineStaza) = 60000 * (1.0 + 0.04) = 62400
        assertEquals(62400.0, a.calculateSalary(), 0.001);
    }

    @Test
    void registracijaAgentaJeVidljiva() {
        um.registerEmployee(agent("ag1", 5));
        assertEquals(1, um.getAllAgents().size());
    }

    @Test
    void duplikatKorisnickogImenaBacaIzuzetak() {
        um.registerEmployee(agent("ag1", 5)); // username "uag1"
        Agent dup = agent("ag2", 3);
        dup.setUsername("uag1");
        assertThrows(IllegalStateException.class, () -> um.registerEmployee(dup));
    }

    @Test
    void registracijaKlijenta() {
        Client c = new Client("cl1", "C", "D", Gender.FEMALE, LocalDate.of(1995, 1, 1), "1", "a",
                "mail", "p", LocalDate.now().minusYears(3), ClientCategory.STUDENT);
        um.registerClient(c);
        assertEquals(1, um.getAllClients().size());
    }
}
