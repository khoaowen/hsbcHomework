package probability;

import java.util.Objects;

public interface ProbabilisticRandomGen {
    public int nextFromSample();

    static class NumAndProbability {
        private final int number;
        private final float probabilityOfSample;

        NumAndProbability(int number, float probabilityOfSample) {
            this.number = number;
            this.probabilityOfSample = probabilityOfSample;
        }

        public int getNumber() {
            return number;
        }

        public float getProbabilityOfSample() {
            return probabilityOfSample;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            NumAndProbability that = (NumAndProbability) o;
            return number == that.number && Float.compare(probabilityOfSample, that.probabilityOfSample) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(number, probabilityOfSample);
        }
    }
}
