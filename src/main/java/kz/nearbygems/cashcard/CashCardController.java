package kz.nearbygems.cashcard;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/cashcards")
class CashCardController {

  private final CashCardRepository cardRepository;
  private final static Sort DEFAULT = Sort.by(Sort.Direction.ASC, "amount");

  private CashCardController(CashCardRepository cashCardRepository) {
    this.cardRepository = cashCardRepository;
  }

  @GetMapping("/{requestedId}")
  private ResponseEntity<CashCard> findById(@PathVariable Long requestedId,
                                            Principal principal) {
    return Optional.ofNullable(cardRepository.findByIdAndOwner(requestedId, principal.getName()))
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  private ResponseEntity<List<CashCard>> findAll(Pageable pageable,
                                                 Principal principal) {
    Page<CashCard> page = cardRepository.findByOwner(principal.getName(),
                                                     PageRequest.of(pageable.getPageNumber(),
                                                                    pageable.getPageSize(),
                                                                    pageable.getSortOr(DEFAULT)));
    return ResponseEntity.ok(page.getContent());
  }

  @PostMapping
  private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest,
                                              UriComponentsBuilder ucb,
                                              Principal principal) {

    CashCard cashCardWithOwner = new CashCard(null,
                                              newCashCardRequest.amount(),
                                              principal.getName());

    CashCard savedCashCard = cardRepository.save(cashCardWithOwner);

    URI locationOfNewCashCard = ucb
        .path("cashcards/{id}")
        .buildAndExpand(savedCashCard.id())
        .toUri();

    return ResponseEntity.created(locationOfNewCashCard).build();
  }

}