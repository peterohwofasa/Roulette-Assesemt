package za.co.roulett.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import za.co.roulett.model.Bet;
import za.co.roulett.model.Player;
import za.co.roulett.model.ResultRoulett;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
public class RouletteService {

	// Players User For Betting
	List<Player> players = new ArrayList<>();
	// Players That Have Been Use For Betting
	public Map<String, Player> betPlayers = new TreeMap<>();
	Bet bet = new Bet();
	public List<ResultRoulett> resultRoulettes = new CopyOnWriteArrayList<>();
	ResultRoulett resultRoulette = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(RouletteService.class);

	// Load File ANd Players
	public List<Player> laodFile(String file) {
        BufferedReader br =null;
		try {
			File fil = new File(file);
			br = new BufferedReader(new FileReader(fil));
			while (br.ready()) {

				LOGGER.info(br.readLine());
				Player player = new Player();
				player.setName(br.readLine());

				this.players.add(player);
				LOGGER.info("PLAYERS SUCCESSFULLY LOADED FROM RESOURCE FILE players.txt");
				br.close();
			}

		} catch (Exception e) {
			LOGGER.error("PLAYERS UNSUCCESSFULLY LOADED FROM RESOURCE FILE players.txt");
			e.printStackTrace();
		}finally {
        br = null;
    }
		return players;
	}

	// Get A Specific Player
	public Player validatePlayer(int betTye) {
		Player player = players.get(betTye);
		return player;

	}

	// Place A Bet
	public ResultRoulett placeBet(int spin, double betValue, int betType, int betNumber, Player player) {

		String betTyp = (betType == 1) ? "EVEN" : "ODD";

		// for Even
		if (betType == 1 && spin % 2 == 0) {
			bet.setBetType(bet.getBetType().EVEN);
			bet.setValue(betValue * 2);
			player.setBet(bet);
			resultRoulette = new ResultRoulett(player.getName(), bet.getBetType().name(), "Win's", bet.getValue(),
					betValue);
		} else if (betType == 2 && spin % 2 != 0) {
			bet.setBetType(bet.getBetType().ODD);
			bet.setValue(betValue * 2);
			player.setBet(bet);
			resultRoulette = new ResultRoulett(player.getName(), bet.getBetType().name(), "Win's", bet.getValue(),
					betValue);
		} else if (betNumber == 3) {
			resultRoulette = this.forNumber(betValue, betNumber, spin, player);
		} else {
			resultRoulette = new ResultRoulett(player.getName(), betTyp, "Loose", 0.0, betValue);
		}
		// System.out.println("place Bet =" + player);
		resultRoulettes.add(resultRoulette);
		return resultRoulette;
	}

	// Bet With Number's
	private ResultRoulett forNumber(double betValue, int betNumber, int spin, Player player) {

		if (betNumber == spin) {
			bet.setBetNumber(betNumber);
			bet.setValue(betValue * 36);
			player.setBet(bet);
			resultRoulette = new ResultRoulett(player.getName(), "Number", "Win's", bet.getValue(), betValue);
		} else {
			resultRoulette = new ResultRoulett(player.getName(), "Number", "Loose", 0.0, betValue);
		}

		// Checking Statistics per player
		checkPlayersStatistics(resultRoulettes, player);
		return resultRoulette;
	}

	// Checking Statistics per player
	public void checkPlayersStatistics(List<ResultRoulett> resultRoulettes, Player player) {

		int size = 0;
		double totalWIns = 0;
		double totalBet = 0;
		while (resultRoulettes.size() > size) {

			if (resultRoulettes.get(size).getPlayer().contentEquals(player.getName())) {
				totalWIns += resultRoulettes.get(size).getWinnings();
				totalBet += resultRoulettes.get(size).getAmountBet().doubleValue();
				player.setTotalWin(totalWIns);
				player.setTotalBet(totalBet);

			}
			size++;
		}
		betPlayers.put(player.getName(), player);
		// Adding Past Records
		addingOldReecord(this.betPlayers);
	}

	// Adding Past Records
	@SuppressWarnings("unlikely-arg-type")
	public void addingOldReecord(Map<String, Player> betPlayers) {
		File file = new File("file2.txt");

		try {
			BufferedReader read = new BufferedReader(new FileReader(file));
			for (String key : betPlayers.keySet()) {
				while (read.ready()) {
					String first[] = read.readLine().split(",");
					if (betPlayers.get(key).getName().equals(first[0])) {
						double totalBet = Double.valueOf(first[2]);
						double totalWins = Double.valueOf(first[1]);
						betPlayers.get(key).setTotalBet(betPlayers.get(key).getTotalBet() + totalBet);
						betPlayers.get(key).setTotalWin(betPlayers.get(key).getTotalWin() + totalWins);
					}
				}
			}
			this.betPlayers = betPlayers;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
