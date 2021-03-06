package com.crossover.trial.weather.rest.impl;

import static com.crossover.trial.weather.rest.impl.RestWeatherQueryEndpoint.airportDataMap;
import static com.crossover.trial.weather.rest.impl.RestWeatherQueryEndpoint.atmosphericInformation;
import static com.crossover.trial.weather.rest.impl.RestWeatherQueryEndpoint.findAirportData;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.domain.AirportData;
import com.crossover.trial.weather.domain.AtmosphericInformation;
import com.crossover.trial.weather.domain.DataPoint;
import com.crossover.trial.weather.domain.DataPointType;
import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.rest.WeatherCollectorEndpoint;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport
 * weather collection sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
	public final static Logger LOGGER = Logger.getLogger(RestWeatherCollectorEndpoint.class.getName());

	/** shared gson json to object factory */
	public final static Gson gson = new Gson();

	@Override
	public Response ping() {
		return Response.status(Response.Status.OK).entity("ready").build();
	}

	@Override
	public Response updateWeather(String iataCode, String pointType, String datapointJson) {
		try {
			addDataPoint(iataCode, pointType, gson.fromJson(datapointJson, DataPoint.class));
		} catch (WeatherException e) {
			e.printStackTrace();
		}
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response getAirports() {
		Set<String> retval = airportDataMap.keySet();
		return Response.status(Response.Status.OK).entity(retval).build();
	}

	@Override
	public Response getAirport(String iata) {
		AirportData ad = findAirportData(iata);
		return Response.status(Response.Status.OK).entity(ad).build();
	}

	@Override
	public Response addAirport(String iata, String latString, String longString) {
		addAirport(iata, Double.valueOf(latString), Double.valueOf(longString));
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response addAirports(List<AirportData> airportDatas) {
		for (AirportData airport : airportDatas) {
			addAirport(airport.getIata(), airport.getLatitude(), airport.getLongitude());
		}

		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response deleteAirport(String iata) {
		airportDataMap.remove(iata);
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response exit() {
		System.exit(0);
		return Response.noContent().build();
	}
	//
	// Internal support methods
	//

	/**
	 * Update the airports weather data with the collected data.
	 *
	 * @param iataCode
	 *            the 3 letter IATA code
	 * @param pointType
	 *            the point type {@link DataPointType}
	 * @param dp
	 *            a datapoint object holding pointType data
	 *
	 * @throws WeatherException
	 *             if the update can not be completed
	 */
	private void addDataPoint(String iataCode, String pointType, DataPoint dp) throws WeatherException {
		AtmosphericInformation ai = atmosphericInformation.get(iataCode);
		updateAtmosphericInformation(ai, pointType, dp);
	}

	/**
	 * update atmospheric information with the given data point for the given point
	 * type
	 *
	 * @param ai
	 *            the atmospheric information object to update
	 * @param pointType
	 *            the data point type as a string
	 * @param dp
	 *            the actual data point
	 */
	private void updateAtmosphericInformation(AtmosphericInformation ai, String pointType, DataPoint dp)
			throws WeatherException {
		final DataPointType dptype = DataPointType.valueOf(pointType.toUpperCase());

		switch (dptype) {
			case WIND:
				if (dp.getMean() >= 0) {
					ai.setWind(dp);
					ai.setLastUpdateTime(System.currentTimeMillis());
				}
				break;
			case TEMPERATURE:
				if (dp.getMean() >= -50 && dp.getMean() < 100) {
					ai.setTemperature(dp);
					ai.setLastUpdateTime(System.currentTimeMillis());
				}
				break;
			case HUMIDTY:
				if (dp.getMean() >= 0 && dp.getMean() < 100) {
					ai.setHumidity(dp);
					ai.setLastUpdateTime(System.currentTimeMillis());
				}
				break;
			case PRESSURE:
				if (dp.getMean() >= 650 && dp.getMean() < 800) {
					ai.setPressure(dp);
					ai.setLastUpdateTime(System.currentTimeMillis());
				}
				break;
			case CLOUDCOVER:
				if (dp.getMean() >= 0 && dp.getMean() < 100) {
					ai.setCloudCover(dp);
					ai.setLastUpdateTime(System.currentTimeMillis());
				}
				break;
			case PRECIPITATION:
				if (dp.getMean() >= 0 && dp.getMean() < 100) {
					ai.setPrecipitation(dp);
					ai.setLastUpdateTime(System.currentTimeMillis());
				}
				break;
			default:
				throw new WeatherException("couldn't update atmospheric data");
		}
	}

	/**
	 * Add a new known airport to our list.
	 *
	 * @param iataCode
	 *            3 letter code
	 * @param latitude
	 *            in degrees
	 * @param longitude
	 *            in degrees
	 *
	 * @return the added airport
	 */
	public static AirportData addAirport(String iataCode, double latitude, double longitude) {
		AirportData ad = new AirportData();
		ad.setIata(iataCode);
		ad.setLatitude(latitude);
		ad.setLongitude(longitude);
		airportDataMap.put(iataCode, ad);

		AtmosphericInformation ai = new AtmosphericInformation();
		atmosphericInformation.put(iataCode, ai);
		
		return ad;
	}
}
