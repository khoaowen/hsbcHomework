package probability;

import java.util.*;

public class DefaultProbabilisticRandomGen
        implements ProbabilisticRandomGen {


    private final List<NumAndProbability> numAndProbabilities;
    private final List<Float> cummulativeProbabilities = new ArrayList<>();
    private final Random random = new Random();

    public DefaultProbabilisticRandomGen(List<NumAndProbability> numAndProbabilities) {
        this.numAndProbabilities = numAndProbabilities.stream()
                .sorted(Comparator.comparingDouble(NumAndProbability::getProbabilityOfSample))
                .toList();
        float cumulative = 0;
        for (NumAndProbability numAndProbability : numAndProbabilities) {
            cumulative += numAndProbability.getProbabilityOfSample();
            cummulativeProbabilities.add(cumulative);
        }
        if (Math.abs(cumulative - 1.0) > 1) {
            throw new IllegalArgumentException("Probabilities must sum to 1.0");
        }
    }

    public List<NumAndProbability> getNumAndProbabilities() {
        return new ArrayList<>(numAndProbabilities);
    }

    @Override
    public int nextFromSample() {
        float rand = random.nextFloat();
        if (numAndProbabilities.size() == 1) {
            return numAndProbabilities.getFirst().getNumber();
        }
        int index = Collections.binarySearch(cummulativeProbabilities, rand);
        if (index < 0) {
            index = -index - 1;
        }
        return numAndProbabilities.get(index).getNumber();
    }

}
