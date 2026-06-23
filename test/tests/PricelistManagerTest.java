package tests;

import manager.PricelistManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.InMemoryRepositories.FakePricelistRepository;

import static org.junit.jupiter.api.Assertions.*;

class PricelistManagerTest {

    private PricelistManager pm;

    @BeforeEach
    void setUp() {
        pm = new PricelistManager(new FakePricelistRepository());
    }

    @Test
    void podrazumevanoTrajanjeKadNemaCenovnika() {
        assertEquals(3, pm.getDefaultRentalDays());
    }

    @Test
    void kreiranjeCenovnikaGaPostavljaKaoAktivan() {
        pm.createPricelist(5, 6000, 5000, 10, 15, 5, "adm");
        assertTrue(pm.getActivePricelist().isPresent());
        assertEquals(5, pm.getDefaultRentalDays());
    }

    @Test
    void noviCenovnikZatvaraPrethodni() {
        pm.createPricelist(5, 6000, 5000, 10, 15, 5, "adm");
        pm.createPricelist(4, 7000, 5000, 10, 15, 5, "adm");
        long aktivnih = pm.getAllPricelists().stream().filter(p -> p.getValidTo() == null).count();
        assertEquals(1, aktivnih);
        assertEquals(7000, pm.getActivePricelist().get().getAnnualSubscriptionPrice());
    }
}
