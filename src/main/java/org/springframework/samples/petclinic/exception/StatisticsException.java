package org.springframework.samples.petclinic.exception;

/**
 * Custom exception for handling errors during statistics calculation.
 * This exception is thrown when database errors or other issues occur
 * while calculating pet statistics.
 */
public class StatisticsException extends RuntimeException {

    /**
     * Constructs a new StatisticsException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public StatisticsException(String message) {
        super(message);
    }

    /**
     * Constructs a new StatisticsException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception (which is saved for later retrieval)
     */
    public StatisticsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new StatisticsException with the specified cause.
     *
     * @param cause the cause of the exception (which is saved for later retrieval)
     */
    public StatisticsException(Throwable cause) {
        super(cause);
    }
}