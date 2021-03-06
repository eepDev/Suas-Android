package zendesk.suas;


import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * Suas - This is the entry point.
 */
public class Suas {

    private static boolean isAndroid = false;

    static {
        try {
            Class.forName("android.os.Build");
            isAndroid = true;
        } catch (Exception ignored) {

        }
    }

    private Suas() {
        // intentionally empty
    }

    /**
     * Creates a {@link Store}.
     *
     * <p>
     *     A {@link Store} must at least have one {@link Reducer}.
     *     <br>
     *     It's not allowed to have two {@link Reducer} registered to the same key.
     * </p>
     *
     * @param reducers a collection of {@link Reducer}
     * @return a instance of {@link Builder} for further configuration
     */
    public static Builder createStore(@NonNull Collection<Reducer> reducers) {
        if(reducers == null || reducers.isEmpty()) {
            throw new IllegalArgumentException("Reducer must not be null or empty");
        }
        return new Builder(reducers);
    }

    /**
     * Creates a {@link Store}.
     *
     * <p>
     *     A {@link Store} must at least have one {@link Reducer}.
     *     <br>
     *     It's not allowed to have two {@link Reducer} registered to the same key.
     * </p>
     *
     * @param reducers a collection of {@link Reducer}
     * @return a instance of {@link Builder} for further configuration
     */
    public static Builder createStore(@NonNull Reducer... reducers) {
        if(reducers == null || reducers.length == 0) {
            throw new IllegalArgumentException("Reducer must not be null or empty");
        }
        return new Builder(Arrays.asList(reducers));
    }

    /**
     * Fluent API for creating a {@link Store}.
     */
    public static class Builder {

        private final Collection<Reducer> reducers;
        private State state;
        private Collection<Middleware> middleware = new ArrayList<>();
        private Filter<Object> notifier = Filters.DEFAULT;
        private Executor executor;

        Builder(@NonNull Collection<Reducer> reducers) {
            this.reducers = reducers;
        }

        /**
         * Configure the {@link Store} with a non empty {@link State}
         *
         * @param state an initial state
         * @return a instance of {@link Builder} for further configuration
         */
        public Builder withInitialState(@NonNull State state) {
            assertArgumentsNotNull(state, "Initial state must not be null");
            this.state = state;
            return this;
        }

        /**
         * Configure the {@link Store} with one or many {@link Middleware}
         *
         * @param middleware a list of {@link Middleware}
         * @return a instance of {@link Builder} for further configuration
         */
        public Builder withMiddleware(@NonNull Collection<Middleware> middleware) {
            assertArgumentsNotNull(middleware, "Middleware must not be null");
            this.middleware = middleware;
            return this;
        }

        /**
         * Configure the {@link Store} with one or many {@link Middleware}
         *
         * @param middleware a list of {@link Middleware}
         * @return a instance of {@link Builder} for further configuration
         */
        public Builder withMiddleware(@NonNull Middleware... middleware) {
            assertArgumentsNotNull(middleware, "Middleware must not be null");
            this.middleware = Arrays.asList(middleware);
            return this;
        }

        /**
         * Configure the {@link Store} with a default {@link Filter}.
         *
         * <p>
         *     Default: {@link Filters#DEFAULT}
         * </p>
         *
         * @param filter a custom default filter
         * @return a instance of {@link Builder} for further configuration
         */
        public Builder withDefaultFilter(Filter<Object> filter) {
            assertArgumentsNotNull(filter, "Notifier must not be null");
            this.notifier = filter;
            return this;
        }

        Builder withExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Creates an instance {@link Store} with the provided options.
         *
         * @return a new {@link Store}
         */
        public Store build() {
            final CombinedReducer combinedReducer = new CombinedReducer(reducers);
            final CombinedMiddleware combinedMiddleware = new CombinedMiddleware(middleware);
            final State initialState = State.mergeStates(combinedReducer.getEmptyState(), state);
            final Executor executor = getExecutor();

            return new SuasStore(initialState, combinedReducer, combinedMiddleware, notifier, executor);
        }

        private Executor getExecutor() {
            if(executor != null) {
                return executor;
            } else if(isAndroid) {
                return Executors.getAndroidExecutor();
            } else {
                return Executors.getDefaultExecutor();
            }
        }

        private void assertArgumentsNotNull(Object input, String msg) {
            if(input == null) {
                throw new IllegalArgumentException(msg);
            }
        }
    }
}
