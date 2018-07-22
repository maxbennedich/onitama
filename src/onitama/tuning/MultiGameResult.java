package onitama.tuning;

import onitama.ui.console.GameSimulator.GameResult;

public class MultiGameResult {
    public int[] wins = new int[2];
    public int draws = 0;
    public int plies;
    public int nodesEvaluated;

    public void add(GameResult gameResult, int ai0Player) {
        plies += gameResult.plies;
        nodesEvaluated += gameResult.nodesEvaluated;

        if (gameResult.playerWon == -1)
            ++draws;
        else
            ++wins[gameResult.playerWon ^ ai0Player];
    }

    public int gamesPlayed() {
        return wins[0] + wins[1] + draws;
    }

    public double points(int player) {
        return wins[player] + 0.5 * draws;
    }

    public double winRate(int player) {
        return points(player) / gamesPlayed();
    }

    public double eloDifference(int player) {
        return -400 * Math.log10(1 / winRate(player) - 1);
    }

    @Override public String toString() {
        return String.format("Elo = %.2f -- Win rate = %.2f %% -- Wins = %d / %d, Draws = %d",
                eloDifference(0), winRate(0)*100, wins[0], wins[1], draws);
    }
}