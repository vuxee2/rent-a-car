package tests;

import manager.ReportManager;
import model.*;
import model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.InMemoryRepositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportManagerTest {

    private FakeReservationRepository rr;
    private FakeRentalRepository renRepo;
    private FakeSubscriptionRepository subRepo;
    private ReportManager rep;

    private final LocalDate today = LocalDate.now();
    private final LocalDate monthStart = LocalDate.now().withDayOfMonth(1);

    @BeforeEach
    void setUp() {
        rr = new FakeReservationRepository();
        renRepo = new FakeRentalRepository();
        subRepo = new FakeSubscriptionRepository();
        FakeUserRepository ur = new FakeUserRepository();
        FakeVehicleModelRepository mr = new FakeVehicleModelRepository();

        rep = new ReportManager(rr, renRepo, ur, mr, subRepo);

        ur.save(new Agent("ag1", "A", "B", Gender.MALE, LocalDate.of(1990, 1, 1), "1", "a",
                "uag1", "p", "MASTER", 10, 60000, 1.0)); // plata 62400
        ur.save(new Client("stu", "C", "D", Gender.FEMALE, LocalDate.of(1995, 1, 1), "1", "a",
                "mail", "p", LocalDate.now().minusYears(5), ClientCategory.STUDENT));

        rr.save(new Reservation("r1", "stu", "m1", today, today.plusDays(3),
                ReservationStatus.CONFIRMED, 1000, "", LocalDateTime.now(), "ag1"));
        subRepo.save(new Subscription("s1", "stu", today, today.plusYears(1),
                SubscriptionStatus.ACTIVE, 6000, "ag1"));
        renRepo.save(new Rental("rt1", "r1", "v1", "ag1", 100, 300, today.minusDays(1), today, 500));
    }

    @Test
    void finansijskiIzvestajZbiraSvePrihode() {
        ReportManager.FinancialReport fin = rep.getFinancialReport(monthStart, today);
        assertEquals(6000, fin.subscriptionIncome());
        assertEquals(1000, fin.rentalIncome());
        assertEquals(500, fin.lateFeesIncome());
        assertEquals(7500, fin.totalIncome());
        assertEquals(62400, fin.salaryExpense(), 0.001);
        assertEquals(7500 - 62400, fin.getNetResult(), 0.001);
    }

    @Test
    void statusIzvestajBrojiPoStatusu() {
        Map<ReservationStatus, Long> st = rep.getReservationStatusReport(monthStart, today);
        assertEquals(1L, (long) st.get(ReservationStatus.CONFIRMED));
    }

    @Test
    void mesecniPrihodPoKategorijiImaDvanaestMeseci() {
        ReportManager.MonthlyRevenueReport mrep = rep.getMonthlyRevenueByCategory(12);
        assertEquals(12, mrep.monthLabels().size());
    }

    @Test
    void mesecniPrihodRaspoznajeKategorijuStudent() {
        ReportManager.MonthlyRevenueReport mrep = rep.getMonthlyRevenueByCategory(12);
        int last = mrep.monthLabels().size() - 1; // tekuci mesec
        // najam 1000 + pretplata 6000 + kazna 500 = 7500, sve na STUDENT
        assertEquals(7500.0, (double) mrep.revenuePerCategory().get("STUDENT").get(last));
        assertEquals(7500.0, (double) mrep.totalPerMonth().get(last));
    }
}
