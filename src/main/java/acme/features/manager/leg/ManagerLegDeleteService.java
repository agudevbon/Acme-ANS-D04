
package acme.features.manager.leg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.airports.Airport;
import acme.entities.flights.Flight;
import acme.entities.flights.Leg;
import acme.entities.flights.LegStatus;
import acme.realms.Manager;

@GuiService
public class ManagerLegDeleteService extends AbstractGuiService<Manager, Leg> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private ManagerLegRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		int legId;
		Leg leg;
		Manager manager;

		legId = super.getRequest().getData("id", int.class);
		leg = this.repository.findLegById(legId);
		manager = leg == null ? null : leg.getManager();
		status = leg != null && super.getRequest().getPrincipal().hasRealm(manager) && leg.getDraftMode();

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Leg object;
		int id;

		id = super.getRequest().getData("id", int.class);
		object = this.repository.findLegById(id);

		super.getBuffer().addData(object);

	}

	@Override
	public void bind(final Leg leg) {
		assert leg != null;

		super.bindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status");
	}

	@Override
	public void validate(final Leg leg) {
		;
	}

	@Override
	public void perform(final Leg leg) {
		this.repository.delete(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		Dataset dataset;
		SelectChoices flightChoices;
		List<Flight> managerFlights = this.repository.findDraftingFlightByManagerId(leg.getManager().getId());
		flightChoices = SelectChoices.from(managerFlights, "tag", leg.getFlight());

		SelectChoices aircraftChoices;
		List<Aircraft> aircrafts = this.repository.findAircrafts();
		aircraftChoices = SelectChoices.from(aircrafts, "registrationNumber", leg.getAircraft());

		SelectChoices departureChoices;
		SelectChoices arrivalChoices;
		List<Airport> airports = this.repository.findAirports();
		departureChoices = SelectChoices.from(airports, "iataCode", leg.getDeparture());
		arrivalChoices = SelectChoices.from(airports, "iataCode", leg.getArrival());

		SelectChoices statusChoices;
		statusChoices = SelectChoices.from(LegStatus.class, leg.getStatus());

		dataset = super.unbindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status");

		dataset.put("duration", leg.getDuration());
		dataset.put("statuss", statusChoices);
		dataset.put("flights", flightChoices);
		dataset.put("flight", flightChoices.getSelected().getKey());
		dataset.put("aircrafts", aircraftChoices);
		dataset.put("aircraft", aircraftChoices.getSelected().getKey());
		dataset.put("departures", departureChoices);
		dataset.put("departure", departureChoices.getSelected().getKey());
		dataset.put("arrivals", arrivalChoices);
		dataset.put("arrival", arrivalChoices.getSelected().getKey());

		super.getResponse().addData(dataset);

	}

}
