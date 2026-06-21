package manager;

import model.Pricelist;
import repository.PricelistRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PricelistManager {

    private final PricelistRepository pricelistRepository;

    public PricelistManager(PricelistRepository pricelistRepository) {
        this.pricelistRepository = pricelistRepository;
    }

    public Optional<Pricelist> getActivePricelist() {
        return pricelistRepository.findActiveOn(LocalDate.now());
    }

    public List<Pricelist> getAllPricelists() {
        return pricelistRepository.findAll();
    }

    public Pricelist createPricelist(int defaultRentalDays, double annualSubscriptionPrice,
                                      double lateReturnFeePerDay, double studentDiscount,
                                      double pensionerDiscount, double companyDiscount,
                                      String adminId) {

        getActivePricelist().ifPresent(current -> {
            current.setValidTo(LocalDate.now().minusDays(1));
            pricelistRepository.save(current);
        });

        Pricelist pricelist = new Pricelist(
                UUID.randomUUID().toString(),
                LocalDate.now(),
                null,
                defaultRentalDays,
                annualSubscriptionPrice,
                lateReturnFeePerDay,
                studentDiscount,
                pensionerDiscount,
                companyDiscount,
                adminId
        );

        pricelistRepository.save(pricelist);
        return pricelist;
    }

    public int getDefaultRentalDays() {
        return getActivePricelist().map(Pricelist::getDefaultRentalDays).orElse(3);
    }
}
