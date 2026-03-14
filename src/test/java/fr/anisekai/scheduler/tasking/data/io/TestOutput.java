package fr.anisekai.scheduler.tasking.data.io;

import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;

public record TestOutput(String example) {

    public static final ObjectSerializer<TestOutput> CODEC = new ObjectSerializer<>() {

        private static final String PREFIX = "output:";

        @Override
        public String serialize(TestOutput container) {

            return String.format("%s%s", PREFIX, container.example());
        }

        @Override
        public TestOutput deserialize(String raw) {

            if (!raw.startsWith(PREFIX)) throw new UnsupportedOperationException();
            return new TestOutput(raw.substring(PREFIX.length()));
        }
    };

}
