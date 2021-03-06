package com.crossover.trial.weather.exception;

/**
 * An internal exception marker
 */
public class WeatherException extends Exception {
	
	private static final long serialVersionUID = -3717617059428945841L;
	
	public WeatherException(String message) {
		super(message);
	}
}
