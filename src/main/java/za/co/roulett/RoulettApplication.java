package za.co.roulett;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import za.co.roulett.model.BetType;
import za.co.roulett.model.Player;
import za.co.roulett.model.ResultRoulett;
import za.co.roulett.service.RouletteService;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
public class RoulettApplication implements CommandLineRunner {

    @Autowired
    RouletteService rouletteService;
    static List<Player> players = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RoulettApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RoulettApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Player player = null;
       ResultRoulett resultRoulette = new ResultRoulett();
        Map<String, Player> betPlayers = new TreeMap<>();
        SplittableRandom random = new SplittableRandom();
        List<ResultRoulett> resultRoulettes = new CopyOnWriteArrayList<>();

        int playerNumber = 0, number = 0, chioce = 0, betType = 0, betValue = 0, control = 0;

        // SPin Value Captured
        int spin = random.nextInt(1, 36);

        players = rouletteService.laodFile("PLAYERS.txt");

        do {

            System.out.println("\n \tPlayers");
            while (players.size() != playerNumber) {
                player = RoulettApplication.players.get(playerNumber);

                System.out.println("\t"+playerNumber++ + ". " + player.getName());
            }
            playerNumber = 0;

            for (;;) {
                scanner.nextLine();
                try {
                    //scanner.nextLine();
                    System.out.print("\nSelect Player You Want To Bet: ");

                    chioce = scanner.nextInt();

                    break;
                } catch (Exception e) {
                    System.out.println();
                    System.err.println("Only Numbers Allowed");
                    LOGGER.error("Only Numbers Allowed");
                    // continue;
                }
            }

            if (chioce >= players.size()) {
                LOGGER.error("Inavid Player Name {try between 0 - " + (players.stream().count() -1 )  + "}");
                continue;
            }
            player = rouletteService.validatePlayer(chioce);

            for (;;) {
                try {
                    scanner.nextLine();
                    System.out.print("\nSelect bet Value/Amount : R");
                    betValue = scanner.nextInt();
                    break;
                } catch (Exception e) {
                    System.err.println("Numbers Please...{3.0, 5.7 ..ect}");
                }
            }
            System.out.println("\n");

            for (;;) {
                try {
                    scanner.nextLine();
                    System.out.print("\t1. " + BetType.EVEN + "\n \t2. " + BetType.ODD + "\n \t3. " + BetType.NUMBER
                            + "\nSelect Bet Type: ");

                    betType = scanner.nextInt();

                    if (betType >=1 && betType <=3) {

                        break;
                    }else {

                        System.err.println("Bet Should be greater than 0 and less than  4\n");
                    }

                } catch (Exception e) {
                    System.out.println();
                    System.err.println("Numbers Please....");
                }
            }

            if (betType == 3) {
                System.out.print("Place your bet on number(1-36): ");
                number = scanner.nextInt();
            }

            // Placing Bet
            resultRoulette = rouletteService.placeBet(spin, betValue, betType, number, player);

            // Checking Statistics per player
            rouletteService.checkPlayersStatistics(rouletteService.resultRoulettes, player);

            control++;
            player = null;
        } while (players.size() > control);

        betPlayers = rouletteService.betPlayers;

        // rouletteService.addingOldReecord(betPlayers);

        resultRoulettes = rouletteService.resultRoulettes;

        System.out.println("\nNumber: " + spin + " 				Output 1");
        line();
        printHeader1();
        control = 0;

        while (resultRoulettes.size() > control) {

            printRow1(resultRoulettes.get(control).getPlayer(), resultRoulettes.get(control).getBetType(),
                    resultRoulettes.get(control).getOutcome(), resultRoulettes.get(control).getWinnings());
            control++;
        }

        System.out.println("\n\n				  Output 2");
        line();
        printHeader2();
        control = 0;

        for (String key : betPlayers.keySet()) {
            printRow2(betPlayers.get(key).getName(), betPlayers.get(key).getTotalWin(),
                    betPlayers.get(key).getTotalBet());
        }
    }

    static void line() {
        System.out.printf("----------------------------------------------------------------------------------\n");
    }

    static void printHeader1() {
        System.out.printf("%-20s \t %-20s \t %-15s \t %-15s \n", "Player", "Bet ", "Outcome", "Win");
    }

    static void printRow1(String player, String bet, String outcome, double winnings) {
        System.out.printf("%-23s  %-23s  %-23s  %-23s \n", player, bet, outcome, "R"+winnings);
    }

    static void printHeader2() {
        System.out.printf("%-20s \t %-20s \t %-20s \n", "Player", "Total Win ", "Total Bet");
    }


    static void printRow2(String player, Double totalWIns, double totalBet) {
        System.out.printf("%-23s  %-23s  %-23s  \n", player, "R"+totalWIns, "R"+totalBet);
    }
}
