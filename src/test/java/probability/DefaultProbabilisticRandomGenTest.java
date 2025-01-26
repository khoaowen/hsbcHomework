package probability;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class DefaultProbabilisticRandomGenTest {

    @Test
    void givenUnorderedListWhenInitializeShouldOrderTheListByProbability() {
        // given
        var numAndProbability1 = new ProbabilisticRandomGen.NumAndProbability(1, 0.6f);
        var numAndProbability2 = new ProbabilisticRandomGen.NumAndProbability(2, 0.1f);
        var numAndProbability3 = new ProbabilisticRandomGen.NumAndProbability(3, 0.3f);
        List<ProbabilisticRandomGen.NumAndProbability> listOfNumAndProbability = Arrays.asList(
                numAndProbability1,
                numAndProbability2,
                numAndProbability3);
        var defaultProbabilisticRandomGen = new DefaultProbabilisticRandomGen(listOfNumAndProbability);

        // when
        List<ProbabilisticRandomGen.NumAndProbability> orderedList = defaultProbabilisticRandomGen.getNumAndProbabilities();

        // then
        assertThat(orderedList.get(0)).isEqualTo(numAndProbability2);
        assertThat(orderedList.get(1)).isEqualTo(numAndProbability3);
        assertThat(orderedList.get(2)).isEqualTo(numAndProbability1);

    }

    @Test
    void givenOneNumberWhenNextSampleShouldReturnIt() {
        // given
        var listOfNumAndProbability = List.of(new ProbabilisticRandomGen.NumAndProbability(1, 0.1f));
        var probabilisticRandomGen = new DefaultProbabilisticRandomGen(listOfNumAndProbability);

        // when
        var result = probabilisticRandomGen.nextFromSample();

        // then
        assertThat(result).isEqualTo(1);

    }

    @Test
    void givenListOfNumbersWhenLoopingNextSamplesShouldReturnTheCorrectProbabilities() {
        // given
        ProbabilisticRandomGen.NumAndProbability numAndProbability1 = new ProbabilisticRandomGen.NumAndProbability(1, 0.1f);
        ProbabilisticRandomGen.NumAndProbability numAndProbability2 = new ProbabilisticRandomGen.NumAndProbability(2, 0.3f);
        ProbabilisticRandomGen.NumAndProbability numAndProbability3 = new ProbabilisticRandomGen.NumAndProbability(3, 0.6f);
        var listOfNumAndProbability = List.of(
                numAndProbability1,
                numAndProbability2,
                numAndProbability3
        );
        var probabilisticRandomGen = new DefaultProbabilisticRandomGen(listOfNumAndProbability);
        int samples = 1_000_000;
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        // when
        for (int i = 0; i < samples; i++) {
            int result = probabilisticRandomGen.nextFromSample();
            frequencyMap.put(result, frequencyMap.getOrDefault(result, 0) + 1);
        }

        // then
        float tolerance = 0.01f;
        assertThat(frequencyMap.get(numAndProbability1.getNumber()) / (float) samples).isCloseTo(numAndProbability1.getProbabilityOfSample(), within(tolerance));
        assertThat(frequencyMap.get(numAndProbability2.getNumber()) / (float) samples).isEqualTo(numAndProbability2.getProbabilityOfSample(), within(tolerance));
        assertThat(frequencyMap.get(numAndProbability3.getNumber()) / (float) samples).isEqualTo(numAndProbability3.getProbabilityOfSample(), within(tolerance));

    }


}