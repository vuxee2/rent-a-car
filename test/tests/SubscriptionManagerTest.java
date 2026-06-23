package tests;

import manager.PricelistManager;
import manager.SubscriptionManager;
import model.Subscription;
import model.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.InMemoryRepositories.FakePricelistRepository;
import tests.InMemoryRepositories.FakeSubscriptionRepository;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionManagerTest {

    private FakeSubscriptionRepository subRepo;
    private PricelistManager pm;
    private SubscriptionManager sm;

    @BeforeEach
    void setUp() {
        subRepo = new FakeSubscriptionRepository();
        pm = new PricelistManager(new FakePricelistRepository());
        sm = new SubscriptionManager(subRepo, pm);
    }

    @Test
    void zahtevBezCenovnikaBacaJasnuGresku() {
        // Regresija za pad pri pokretanju (problem #6): nema vise static initializer-a
        assertThrows(IllegalStateException.class, () -> sm.requestSubscription("cl1"));
    }

    @Test
    void zahtevKoristiCenuIzAktivnogCenovnika() {
        pm.createPricelist(3, 6000, 5000, 10, 15, 5, "adm");
        Subscription s = sm.requestSubscription("cl1");
        assertEquals(SubscriptionStatus.PENDING, s.getStatus());
        assertEquals(6000, s.getPaidAmount());
    }

    @Test
    void duplikatZahtevaBaca() {
        pm.createPricelist(3, 6000, 5000, 10, 15, 5, "adm");
        sm.requestSubscription("cl1");
        assertThrows(IllegalStateException.class, () -> sm.requestSubscription("cl1"));
    }

    @Test
    void odobravanjeSaPrevisekasnjenjaOdbija() {
        pm.createPricelist(3, 6000, 5000, 10, 15, 5, "adm");
        Subscription s = sm.requestSubscription("cl1");
        assertThrows(IllegalStateException.class, () -> sm.approveSubscription(s.getId(), "ag1", 6));
        assertEquals(SubscriptionStatus.REJECTED, subRepo.findById(s.getId()).get().getStatus());
    }

    @Test
    void uspesnoOdobravanjeAktiviraPretplatu() {
        pm.createPricelist(3, 6000, 5000, 10, 15, 5, "adm");
        Subscription s = sm.requestSubscription("cl2");
        sm.approveSubscription(s.getId(), "ag1", 2);
        assertEquals(SubscriptionStatus.ACTIVE, subRepo.findById(s.getId()).get().getStatus());
        assertTrue(sm.hasActiveSubscription("cl2"));
    }
}
