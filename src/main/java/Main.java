import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.engine.limit;
import org.jenetics.util.Factory;
import org.jfugue.player.Player;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final int ITERATION_COUNT = 1;
    private static final int POPULATION_SIZE = 10;
    private static final int TOURNAMENT_SIZE = 7;
    private static final double OFFSPRING_FRACTION = 0.8;
    private static double NUMBER_OF_PLAY_GEN = -1;
    private static final int NUMBER_OF_NOTES = 12;
    private static int LENGTH_OF_INPUT = 0;
    private static String INPUT_CONTENT = "";
    private static List<String> bestInGen = new LinkedList<>();
    private static int GEN_COUNT = 0;

    private static double getFitness(Genotype<IntegerGene> input) {

        double diff = 0;
        /*
         * Program je zapocet tako da koristi znamenke kao vrijednosti pa je doslo do problema kada
         * je trebalo napisati znamenku 10. Poslje 9 dolaze ':' i ';' zato jer su iduce po ASCII vrijednosti
         * koja se koristi za racunanje dobrote
         */
        StringBuilder currentValue = new StringBuilder();
        ((Genotype<IntegerGene>) input).getChromosome().stream().forEach(e -> {
            if (e.getAllele().doubleValue() < 10)
                currentValue.append((e.getAllele()));
            else if (e.getAllele() == 10)
                currentValue.append(':');
            else if (e.getAllele() == 11)
                currentValue.append(';');
        });
        String targetValue = noteToNumExtended(INPUT_CONTENT);


        for (int i = 0; i < targetValue.length(); i++) {
            diff += Math.abs(targetValue.charAt(i) - currentValue.charAt(i));
        }

        return diff;
    }

    public static void main(String[] args) {

        //INPUT_CONTENT = "A C A B D E F G A B";
        Scanner sc = new Scanner(System.in);
        System.out.print("Unesite note: ");
        INPUT_CONTENT = sc.nextLine();

        LENGTH_OF_INPUT = noteToNumExtended(INPUT_CONTENT).length();
        //sc.close();

        for (int i = 0; i < ITERATION_COUNT; i++) {

            long startTime = System.nanoTime();
            callNoOpt();
            long endTime = System.nanoTime();

            long duration = (endTime - startTime) / 1000000; // divide by 1000000 to get milliseconds.
            System.out.println(" Time: " + duration + " ms");
            System.out.println();
        }


        playGenerations();

    }

    private static void callNoOpt() {
        final Factory<Genotype<IntegerGene>> gtf = Genotype
                .of(IntegerChromosome.of(0, NUMBER_OF_NOTES - 1, LENGTH_OF_INPUT));
        // System.out.println(((Genotype<IntegerGene>) gtf).getChromosome());

//		Player player = new Player();
        final Engine<IntegerGene, Double> engine = Engine.builder(Main::getFitness, gtf)
                .populationSize(POPULATION_SIZE)
                .minimizing()
                //.selector(new TournamentSelector<>(TOURNAMENT_SIZE))
                .selector(new BoltzmannSelector<>(4))
                .offspringFraction(OFFSPRING_FRACTION)
                .alterers(new UniformCrossover<>(0.5), new GaussianMutator<>(0.25), new MeanAlterer<>(0.25))//, new GaussianMutator<>(0.25), new MeanAlterer<>(0.25)
                .build();

        final EvolutionResult<IntegerGene, Double> result = engine.stream().limit(limit.byFitnessThreshold(1.0))
                // .limit(limit.byFixedGeneration(10000))
                // .limit(limit.byExecutionTime(Duration.ofSeconds(30)))
                .limit(limit.byExecutionTime(Duration.ofMinutes(10)))
                // .peek( e -> player.play(numToNote(e.getBestPhenotype().toString())))
                // .peek( e ->
                // System.out.println(numToNote(e.getBestPhenotype().getGenotype().toString())))
                .peek(e -> bestInGen.add((numToNoteExtended(e.getBestPhenotype().getGenotype().toString()))))
                //.peek( e -> System.out.println(e.getBestPhenotype().toString()))
                .collect(EvolutionResult.toBestEvolutionResult());

        // System.out.println(result.getBestPhenotype().getFitness() == 0? "Yes" :
        // "No");
        System.out.println("Gen count: " + result.getTotalGenerations());
        GEN_COUNT += result.getTotalGenerations();
        //System.out.println("Time: " + result.getDurations().getEvaluationDuration());
        // System.out.println(result.getBestPhenotype().toString());
        //System.out.println(numToNote(result.getBestPhenotype().getGenotype().toString()));
    }

    private static String noteToNumExtended(String input) {
        // C C#Db D D#Eb E F F#Gb G G#Ab A A#Bb B
        StringBuilder output = new StringBuilder();
        int inputLen = input.length();
        input += " ";

        for (int i = 0; i < inputLen; i++) {
            switch (input.charAt(i)) {
                case 'C':
                    if (input.charAt(i + 1) != '#')
                        output.append("0");
                    else
                        output.append("1");
                    break;
                case 'D':
                    if (input.charAt(i + 1) == '#')
                        output.append("3");
                    else if (input.charAt(i + 1) == 'b')
                        output.append("1");
                    else
                        output.append("2");
                    break;
                case 'E':
                    if (input.charAt(i + 1) == 'b')
                        output.append("3");
                    else
                        output.append("4");
                    break;
                case 'F':
                    if (input.charAt(i + 1) == '#')
                        output.append("6");
                    else
                        output.append("5");
                    break;
                case 'G':
                    if (input.charAt(i + 1) == '#')
                        output.append("8");
                    else if (input.charAt(i + 1) == 'b')
                        output.append("6");
                    else
                        output.append("7");
                    break;
                case 'A':
                    if (input.charAt(i + 1) == '#')
                        output.append(":");
                    else if (input.charAt(i + 1) == 'b')
                        output.append("8");
                    else
                        output.append("9");
                    break;
                case 'B':
                    if (input.charAt(i + 1) == 'b')
                        output.append(":");
                    else
                        output.append(";");
                    break;

            }

        }


        return output.toString();

    }


    private static String numToNoteExtended(String input) {
        // CDEFGAB
        // System.out.println("INPUT: " + input);
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case '0':
                    output.append("C ");
                    break;
                case '1':
                    output.append("C# ");
                    break;
                case '2':
                    output.append("D ");
                    break;
                case '3':
                    output.append("D# ");
                    break;
                case '4':
                    output.append("E ");
                    break;
                case '5':
                    output.append("F ");
                    break;
                case '6':
                    output.append("F# ");
                    break;
                case '7':
                    output.append("G ");
                    break;
                case '8':
                    output.append("G# ");
                    break;
                case '9':
                    output.append("A ");
                    break;
                case ':':
                    output.append("A# ");
                    break;
                case ';':
                    output.append("B ");
                    break;

            }
        }
        return output.toString().trim();

    }

    @Deprecated
    private static String noteToNum(String input) {
        // CDEFGAB
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case 'C':
                    output.append("0");
                    break;
                case 'D':
                    output.append("1");
                    break;
                case 'E':
                    output.append("2");
                    break;
                case 'F':
                    output.append("3");
                    break;
                case 'G':
                    output.append("4");
                    break;
                case 'A':
                    output.append("5");
                    break;
                case 'B':
                    output.append("6");
                    break;

            }
        }

        return output.toString();

    }

    @Deprecated
    private static String numToNote(String input) {
        // CDEFGAB
        // System.out.println("INPUT: " + input);
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case '0':
                    output.append("C ");
                    break;
                case '1':
                    output.append("D ");
                    break;
                case '2':
                    output.append("E ");
                    break;
                case '3':
                    output.append("F ");
                    break;
                case '4':
                    output.append("G ");
                    break;
                case '5':
                    output.append("A ");
                    break;
                case '6':
                    output.append("B ");
                    break;

            }
        }
        return output.toString().trim();

    }

    private static void playGenerations() {


        Scanner sc = new Scanner(System.in);

        System.out.println("Rjesenje je pronadeno u " + bestInGen.size() + " generacija.");
        System.out.println("Koliko generacija zelite da svira: ");
        NUMBER_OF_PLAY_GEN = sc.nextInt();
        sc.close();

        Player player = new Player();
        //player.play("C C# D D# E F F# G G# A A# B ");
        //player.play("C Db D Eb E F Gb G Ab A Bb B ");

        if (NUMBER_OF_PLAY_GEN > bestInGen.size() || NUMBER_OF_PLAY_GEN == -1) {
            NUMBER_OF_PLAY_GEN = bestInGen.size() - 1;
        }

        if (NUMBER_OF_PLAY_GEN > 1) {
            double stepSize = bestInGen.size() / NUMBER_OF_PLAY_GEN;
            for (int i = 0; i < NUMBER_OF_PLAY_GEN-1; i++) {
                System.out.println(bestInGen.get((int) (i * stepSize)));
                player.play(bestInGen.get((int) (i * stepSize)));
            }
        }

        if (NUMBER_OF_PLAY_GEN > 0) {
            System.out.println(((LinkedList<String>) bestInGen).getLast());
            player.play(((LinkedList<String>) bestInGen).getLast());
        }

    }
}
