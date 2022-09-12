package bowling.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Game {
    List<Frame> frames;
    private int currentFrame = 0;
    private boolean finishedGame = false;
    private int allowedBallsAfterTenthFrame;
    private int specialScoreAfterTenthFrame = -1;

    public Game() {
        frames = new ArrayList<>(10);
        for (int i = 0; i < 10; i++)
            frames.add(null);
    }

    public boolean score(int score) {
        if (!finishedGame) {
            if (currentFrame >= 9 && allowedBallsAfterTenthFrame > 0) {
                return handleAdditionalBallThrowsAfterGame(score);
            }
            if (frames.get(currentFrame) == null)
                return handleFirstBallThrowInFrame(score);
            else
                return handleSecondBallThrowInFrame(score);
        }
        return false;
    }

    public int getGameScore() {
        return frames.stream()
                .filter(it -> (!Objects.isNull(it)))
                .filter(Frame::isFinishedCountingScore)
                .mapToInt(Frame::getFinalScore).sum();
    }

    public List<Frame> getFrames() {
        return new ArrayList<>(frames);
    }

    public boolean isFinishedGame() {
        return finishedGame;
    }

    private boolean handleSecondBallThrowInFrame(int score) {
        if (isSecondBallThrowScoreIllegal(score, frames.get(currentFrame).getFirstHit()))
            return false;

        //go through frames, adding to score of unfinished strike/spare frames and finishing them
        sortOutStrikesAndSpares(score);

        frames.get(currentFrame).setSecondHit(score);

        //setting final score to the result of two ball throws from this frame

        frames.get(currentFrame).setFinalScore(
                frames.get(currentFrame).getFirstHit()
                        + frames.get(currentFrame).getSecondHit()
        );

        frames.get(currentFrame).incrementBallCounter();

        if (frames.get(currentFrame).getFinalScore() == 10) {
            if (currentFrame == 9) {
                //10th frame spare
                allowedBallsAfterTenthFrame = 1;
            }
            //spare
        } else {
            //finishing a normal frame, i.e. not strike or spare
            if (currentFrame >= 9)
                finishedGame = true;
            frames.get(currentFrame).setFinishedCountingScore(true);
        }
        currentFrame++;
        return true;
    }

    private boolean handleFirstBallThrowInFrame(int score) {
        if (isFirstBallThrowScoreIllegal(score))
            return false;

        //go through frames, adding to score of unfinished strike/spare frames and finishing them
        sortOutStrikesAndSpares(score);

        frames.set(currentFrame, new Frame());
        frames.get(currentFrame).setFirstHit(score);
        frames.get(currentFrame).incrementBallCounter();

        if (score == 10) {
            if (currentFrame == 9) {
                //10th frame strike
                allowedBallsAfterTenthFrame = 2;
            }
            //strike
            frames.get(currentFrame).setFinalScore(frames.get(currentFrame).getFirstHit());
            currentFrame++;
        }
        return true;
    }

    private boolean handleAdditionalBallThrowsAfterGame(int score) {
        if (specialScoreAfterTenthFrame == -1) {
            if (isFirstBallThrowScoreIllegal(score))
                return false;
            specialScoreAfterTenthFrame = score;
        } else if (specialScoreAfterTenthFrame < 10) {
            if (isSecondBallThrowScoreIllegal(score, specialScoreAfterTenthFrame))
                return false;
            specialScoreAfterTenthFrame += score;
        }
        allowedBallsAfterTenthFrame--;
        finishedGame = allowedBallsAfterTenthFrame <= 0;
        sortOutStrikesAndSpares(score);
        return true;
    }

    private void sortOutStrikesAndSpares(int score) {
        for (int i = 0; i < currentFrame; i++) {
            if (frames.get(i) != null && !frames.get(i).isFinishedCountingScore()) {
                calculateAdditionalScore(score, frames.get(i));
            }
        }
    }

    private void calculateAdditionalScore(int score, Frame frame) {
        frame.incrementBallCounter();
        frame.increaseFinalScore(score);
        if (frame.getBallCounter() >= 3)
            frame.finishCountingScore();
    }

    /**
     * @return true if illegal, false if legal
     */
    private boolean isFirstBallThrowScoreIllegal(int score) {
        return score < 0 || score > 10;
    }

    /**
     * @return true if illegal, false if legal
     */
    private boolean isSecondBallThrowScoreIllegal(int score, int firstBall) {
        if (isFirstBallThrowScoreIllegal(score))
            return true;
        int totalScore = score + firstBall;
        return totalScore > 10;
    }
}
