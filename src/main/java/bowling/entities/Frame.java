package bowling.entities;

public class Frame {
    private int firstHit;
    private int secondHit;
    private int finalScore;
    private int ballCounter;
    private boolean finishedCountingScore;

    public int getBallCounter() {
        return ballCounter;
    }

    public void setBallCounter(int ballCounter) {
        this.ballCounter = ballCounter;
    }

    public void increaseFinalScore(int score) {
        setFinalScore(getFinalScore() + score);
    }

    public void incrementBallCounter() {
        setBallCounter(getBallCounter() + 1);
    }

    public void finishCountingScore() {
        setFinishedCountingScore(true);
    }

    public int getFirstHit() {
        return firstHit;
    }

    public void setFirstHit(int firstHit) {
        this.firstHit = firstHit;
    }

    public int getSecondHit() {
        return secondHit;
    }

    public void setSecondHit(int secondHit) {
        this.secondHit = secondHit;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public boolean isFinishedCountingScore() {
        return finishedCountingScore;
    }

    public void setFinishedCountingScore(boolean finishedCountingScore) {
        this.finishedCountingScore = finishedCountingScore;
    }

    public Frame() {
        finishedCountingScore = false;
        firstHit = 0;
        secondHit = 0;
        finalScore = 0;
    }
}
