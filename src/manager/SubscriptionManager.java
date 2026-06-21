package manager;

import model.Subscription;
import model.enums.SubscriptionStatus;
import repository.SubscriptionRepository;
import util.AppContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubscriptionManager {

    private static final double ANNUAL_PRICE = AppContext.getInstance().getPricelistManager().getActivePricelist().get().getAnnualSubscriptionPrice();
    private static final int MAX_LATE_RETURNS_ALLOWED = 5;

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionManager(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<Subscription> getSubscriptionsForClient(String clientId) {
        return subscriptionRepository.findByClientId(clientId);
    }

    public Optional<Subscription> getActiveSubscription(String clientId) {
        return subscriptionRepository.findByClientId(clientId).stream()
                .filter(Subscription::isCurrentlyActive)
                .findFirst();
    }

    public boolean hasActiveSubscription(String clientId) {
        return getActiveSubscription(clientId).isPresent();
    }

    public Subscription requestSubscription(String clientId) {
        boolean hasPending = subscriptionRepository.findByClientId(clientId).stream()
                .anyMatch(s -> s.getStatus() == SubscriptionStatus.PENDING);

        if (hasPending) {
            throw new IllegalStateException("Već postoji zahtev za pretplatu koji čeka odobrenje.");
        }

        Subscription subscription = new Subscription(
                UUID.randomUUID().toString(),
                clientId,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                SubscriptionStatus.PENDING,
                ANNUAL_PRICE,
                null
        );
        subscriptionRepository.save(subscription);
        return subscription;
    }

    public void approveSubscription(String subscriptionId, String agentId, int lateReturnCount) {
        subscriptionRepository.findById(subscriptionId).ifPresent(s -> {
            if (lateReturnCount > MAX_LATE_RETURNS_ALLOWED) {
                s.setStatus(SubscriptionStatus.REJECTED);
                s.setAgentId(agentId);
                subscriptionRepository.save(s);
                throw new IllegalStateException(
                        "Zahtev je automatski odbijen — klijent je više od " + MAX_LATE_RETURNS_ALLOWED
                                + " puta kasnio sa vraćanjem vozila.");
            }
            s.setStatus(SubscriptionStatus.ACTIVE);
            s.setStartDate(LocalDate.now());
            s.setEndDate(LocalDate.now().plusYears(1));
            s.setAgentId(agentId);
            subscriptionRepository.save(s);
        });
    }

    public void rejectSubscription(String subscriptionId, String agentId) {
        subscriptionRepository.findById(subscriptionId).ifPresent(s -> {
            s.setStatus(SubscriptionStatus.REJECTED);
            s.setAgentId(agentId);
            subscriptionRepository.save(s);
        });
    }

    public List<Subscription> getPendingSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.PENDING)
                .toList();
    }
}
