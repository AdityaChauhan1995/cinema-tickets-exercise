package uk.gov.dwp.uc.pairtest.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;


class TicketServiceImplTest {

    private TicketServiceImpl ticketService;
    private MockTicketPaymentService paymentService;
    private MockSeatReservationService reservationService;

    @BeforeEach
    void setUp() {
        paymentService = new MockTicketPaymentService();
        reservationService = new MockSeatReservationService();
        ticketService = new TicketServiceImpl(paymentService, reservationService);
    }

    @Test
    void purchaseTickets_ValidRequest_SuccessfullyPurchasesTickets() {
        TicketRequest[] ticketRequests = {
                new TicketRequest(TicketRequest.Type.ADULT, 2),
                new TicketRequest(TicketRequest.Type.CHILD, 1),
                new TicketRequest(TicketRequest.Type.INFANT, 1)
        };
        long accountId = 12345;
        TicketPurchaseRequest purchaseRequest = new TicketPurchaseRequest(accountId,ticketRequests);


        assertDoesNotThrow(() -> ticketService.purchaseTickets(purchaseRequest));
        assertTrue(paymentService.paymentMade);
        assertTrue(reservationService.seatsReserved);
    }
    
    @Test
    void purchaseTickets_ValidRequestAdult_SuccessfullyPurchasesTickets() {
        TicketRequest[] ticketRequests = {
                new TicketRequest(TicketRequest.Type.ADULT, 20)
        };
        long accountId = 78546;
        TicketPurchaseRequest purchaseRequest = new TicketPurchaseRequest(accountId,ticketRequests);


        assertDoesNotThrow(() -> ticketService.purchaseTickets(purchaseRequest));
        assertTrue(paymentService.paymentMade);
        assertTrue(reservationService.seatsReserved);
    }

    @Test
    void purchaseTickets_InvalidRequest_ThrowsException() {
        TicketRequest[] ticketRequests = {}; // Empty request
        long accountId = 12345;
        TicketPurchaseRequest purchaseRequest = new TicketPurchaseRequest(accountId,ticketRequests);


        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(purchaseRequest));
        assertFalse(paymentService.paymentMade);
        assertFalse(reservationService.seatsReserved);
    }

    @Test
    void purchaseTickets_InvalidChildWithoutAdult_ThrowsException() {
        TicketRequest[] ticketRequests = {
                new TicketRequest(TicketRequest.Type.CHILD, 2)
        };
        long accountId = 545619;

        TicketPurchaseRequest purchaseRequest = new TicketPurchaseRequest(accountId,ticketRequests);

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(purchaseRequest));
        assertFalse(paymentService.paymentMade);
        assertFalse(reservationService.seatsReserved);
    }
    
    @Test
    void purchaseTickets_InvalidInfantWithoutAdult_ThrowsException() {
        TicketRequest[] ticketRequests = {
                new TicketRequest(TicketRequest.Type.INFANT, 2)
        };
        long accountId = 678345;

        TicketPurchaseRequest purchaseRequest = new TicketPurchaseRequest(accountId,ticketRequests);

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(purchaseRequest));
        assertFalse(paymentService.paymentMade);
        assertFalse(reservationService.seatsReserved);
    }
    
    @Test
    void purchaseTickets_InvalidChildInfantWithoutAdult_ThrowsException() {
        TicketRequest[] ticketRequests = {
                new TicketRequest(TicketRequest.Type.INFANT, 1),
                new TicketRequest(TicketRequest.Type.CHILD, 2)
        };
        long accountId = 678345;

        TicketPurchaseRequest purchaseRequest = new TicketPurchaseRequest(accountId,ticketRequests);

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(purchaseRequest));
        assertFalse(paymentService.paymentMade);
        assertFalse(reservationService.seatsReserved);
    }
    
    @Test
    void purchaseTickets_InvalidNumberOfTickets_ThrowsException() {
        TicketRequest[] ticketRequests = {
        		new TicketRequest(TicketRequest.Type.ADULT, 18),
                new TicketRequest(TicketRequest.Type.INFANT, 1),
                new TicketRequest(TicketRequest.Type.CHILD, 2)
        };
        long accountId = 678345;

        TicketPurchaseRequest purchaseRequest = new TicketPurchaseRequest(accountId,ticketRequests);

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(purchaseRequest));
        assertFalse(paymentService.paymentMade);
        assertFalse(reservationService.seatsReserved);
    }

    // Mock TicketPaymentService implementation
    private static class MockTicketPaymentService implements TicketPaymentService {
        private boolean paymentMade = false;

        @Override
        public void makePayment(long accountId, int amount) {
            // Simulate successful payment
            paymentMade = true;
        }
    }

    // Mock SeatReservationService implementation
    private static class MockSeatReservationService implements SeatReservationService {
        private boolean seatsReserved = false;

        @Override
        public void reserveSeat(long accountId, int totalSeatsToAllocate) {
            seatsReserved = true;
        }
    }
}