package uk.gov.dwp.uc.pairtest;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.paymentgateway.TicketPaymentService;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


public class TicketServiceImpl implements TicketService {

	/**
	 * Should only have private methods other than the one below.
	 */

	private final TicketPaymentService paymentService;
	private final SeatReservationService seatReservationService;

	public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService seatReservationService) {
		this.paymentService = paymentService;
		this.seatReservationService = seatReservationService;
	}
	/**
	 * purchaseTickets method doesn't return anything and takes TicktPurchaseRequest as an argument that contains the account id of the person making this purchase request
	 * This method call getSeatDivision method which returns a array list containing seat bifurcation amongst Adult, Child and Infant
	 * Based on this count then the validity of the request is calculated via validateRequest method. 
	 * Once Validated then seat reservation is made via reserveSeat of seatReservationService and payment is made via makePayment method of paymentService
	 * If the request is invalid then InvalidPurchaseException is thrown with "Invalid ticket purchase request" as a message
	 */
	@Override
	public void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {

		ArrayList<Integer> seatDivisionList = getSeatsDivision(ticketPurchaseRequest);
		int totalTickets = seatDivisionList.get(0),
				adultTickets = seatDivisionList.get(1),
				childTickets = seatDivisionList.get(2),
				infantTickets = seatDivisionList.get(3),
				totalSeatsToBook = seatDivisionList.get(4);

		String errorMessage  = validateRequest(totalTickets,adultTickets,childTickets,infantTickets);

		if(errorMessage == null) {
			seatReservationService.reserveSeat(ticketPurchaseRequest.getAccountId(),totalSeatsToBook);
			int totalCostToPay =  totalCostToPay(adultTickets,childTickets,infantTickets);
			paymentService.makePayment(ticketPurchaseRequest.getAccountId(), totalCostToPay);
		}else{
			throw new InvalidPurchaseException(errorMessage);
		}
	}
	/**
	 * getSeatsDivision takes TicketPurchaseRequest as an argument and returns array list containing seat bifurcation amongst Adult, Child and Infant
	 * TicketTypeRequests array from the TicketPurchaseRequest is iterated and count of respective individuals are updated
	 * Then total tickets count, adult ticket count, child ticket count, infant ticket count and total seat to book count are all added in a array list
	 * this arraylist is finally returned back.
	 */
	private ArrayList<Integer> getSeatsDivision(TicketPurchaseRequest ticketPurchaseRequest) {

		int totalTickets = 0, adultTickets = 0, childTickets = 0, infantTickets = 0 ;
		ArrayList<Integer> seatDivisionList = new ArrayList<Integer>();
		if(ticketPurchaseRequest != null) {
			for(TicketRequest ticketRequest: ticketPurchaseRequest.getTicketTypeRequests()) {

				if(ticketRequest.getNoOfTickets()<0) {
					totalTickets = 0; 
					adultTickets = 0;
					childTickets = 0; 
					infantTickets = 0;
					break;
				}
				totalTickets += ticketRequest.getNoOfTickets();
				if(ticketRequest.getTicketType() == Type.ADULT) {
					adultTickets += ticketRequest.getNoOfTickets();
				}else if(ticketRequest.getTicketType() == Type.CHILD) {
					childTickets += ticketRequest.getNoOfTickets();
				}else {
					infantTickets += ticketRequest.getNoOfTickets();
				}
			}
		}
		seatDivisionList.add(totalTickets);
		seatDivisionList.add(adultTickets);
		seatDivisionList.add(childTickets);
		seatDivisionList.add(infantTickets);
		seatDivisionList.add(totalTickets-infantTickets); // total Seats To Book

		return seatDivisionList;
	}
	/**
	 * validateRequest returns a error string and receives total ticket count, adult ticket count, child ticket count and infant ticket count as arguments
	 * it checks that the total Tickets should be at least 1 and at most 20 and there should be at least 1 adult with child and infant
	 * if any validation fails then suitable error message is returned and all validation are passed then null is returned.
	 */
	private String validateRequest(int totalTickets, int adultTickets, int childTickets, int infantTickets) {

		if(totalTickets<=0 || totalTickets>20) {
			return "One can buy at least 1 ticket and at most 20 tickets";
		} else if(totalTickets>adultTickets && adultTickets == 0) {
			return "At least one adult ticket has to be bought while purchasing child or infant tickets";
		} else {
			return null;
		}
	}
	/**
	 * totalCostToPay method returns an integer and takes adult ticket count, child ticket count and infant ticket as arguments.
	 * It calculates the ticket price in accordance with there respective cost per individual and return the total cost to pay.
	 */
	private int totalCostToPay(int adultTickets, int childTickets, int infantTickets) {
		Map<Type,Integer> priceMap = getPriceMap();
		return adultTickets*(priceMap.get(Type.ADULT)) + childTickets*(priceMap.get(Type.CHILD)) + infantTickets*(priceMap.get(Type.INFANT));
	}
	/**
	 * getPriceMap method returns an map containing Type of individual with respective to their cost
	 */
	private Map<Type,Integer> getPriceMap() {
		Map<Type,Integer> priceMap = new HashMap<Type, Integer>();
		priceMap.put(Type.ADULT, 20);
		priceMap.put(Type.CHILD,10);
		priceMap.put(Type.INFANT,0);
		return priceMap;

	}


}
