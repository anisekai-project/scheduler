package fr.anisekai.scheduler.tasking.data.io;

import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;

public record TestInput(String example) {

    public static final ObjectSerializer<TestInput> CODEC = new ObjectSerializer<>() {

        private static final String PREFIX = "input:";

        @Override
        public String serialize(TestInput container) {

            return String.format("%s%s", PREFIX, container.example());
        }

        @Override
        public TestInput deserialize(String raw) {

            if (!raw.startsWith(PREFIX)) throw new UnsupportedOperationException();
            return new TestInput(raw.substring(PREFIX.length()));
        }
    };

}
