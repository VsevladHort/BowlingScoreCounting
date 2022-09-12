package logic_test;

import bowling.entities.Frame;
import bowling.entities.Game;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestGame {

    @Test
    void testPerfectGame() {
        Game game = new Game();
        for (int i = 0; i < 14; i++) {
            game.score(10);
        }
        Assertions.assertEquals(300, game.getGameScore());
    }

    @Test
    void testIncompleteGame1() {
        Game game = new Game();
        game.score(10);
        game.score(10);
        game.score(10);
        game.score(10);
        game.score(10);
        Assertions.assertEquals(90, game.getGameScore());
        Assertions.assertFalse(game.isFinishedGame());
    }

    @Test
    void testIncompleteGame2() {
        Game game = new Game();
        game.score(10);
        game.score(10);
        game.score(10);
        game.score(10);
        game.score(10);
        game.score(5);
        game.score(5);
        game.score(1);
        Assertions.assertEquals(146, game.getGameScore());
        Assertions.assertFalse(game.isFinishedGame());
    }

    @Test
    void testOneCompleteNormalGame() {
        Game game = new Game();
        game.score(2);
        game.score(6);
        game.score(7);
        game.score(3);
        game.score(5);
        game.score(4);
        game.score(2);
        Assertions.assertFalse(game.score(10));
        game.score(6);
        Assertions.assertFalse(game.score(-6));
        Assertions.assertFalse(game.score(11));
        game.score(8);
        game.score(2);
        game.score(10);
        game.score(7);
        game.score(2);
        game.score(5);
        game.score(4);
        game.score(10);
        game.score(5);
        game.score(4);
        game.score(4);
        game.score(4);
        Assertions.assertEquals(125, game.getGameScore());
        Assertions.assertTrue(game.isFinishedGame());
    }

    @Test
    void testAlmostPerfectGameLastFrame9to1SpareAnd10AdditionalPoints() {
        Game game = new Game();
        for (int i = 0; i < 14; i++) {
            if (i == 9)
                game.score(9);
            else if (i == 10)
                game.score(1);
            else
                game.score(10);
        }
        Assertions.assertEquals(279, game.getGameScore());
    }

    @Test
    void testAlmostPerfectGameLastFrame9to1SpareAnd5AdditionalPoints() {
        Game game = new Game();
        for (int i = 0; i < 14; i++) {
            if (i == 9)
                game.score(9);
            else if (i == 10)
                game.score(1);
            else if (i == 11)
                game.score(5);
            else
                game.score(10);
        }
        for (Frame frame : game.getFrames()) {
            System.out.println("Frame: ");
            System.out.println(frame.getBallCounter());
            System.out.println(frame.getFinalScore());
            System.out.println(frame.getFirstHit());
            System.out.println(frame.getSecondHit());
            System.out.println("~~~END~~~");
        }
        Assertions.assertEquals(274, game.getGameScore());
    }

    @Test
    void testEveryThrowIs2Points() {
        Game game = new Game();
        for (int i = 0; i < 20; i++) {
            game.score(2);
        }
        Assertions.assertEquals(40, game.getGameScore());
    }

    @Test
    void testEveryThrowIs2PointsButLastFrameIsStrikeAndBonusThrowIsNotStrike() {
        Game game = new Game();
        for (int i = 0; i < 22; i++) {
            if (i == 18)
                game.score(10);
            else if (i == 19)
                game.score(5);
            else if (i == 20)
                game.score(5);
            else
                game.score(2);
        }
        Assertions.assertEquals(56, game.getGameScore());
    }

    @Test
    void testEveryThrowIs2PointsButLastFrameIsSpareAndBonusThrowIsNotStrike() {
        Game game = new Game();
        for (int i = 0; i < 22; i++) {
            if (i == 18)
                game.score(5);
            else if (i == 19)
                game.score(5);
            else if (i == 20)
                game.score(4);
            else
                game.score(2);
        }
        Assertions.assertEquals(50, game.getGameScore());
    }

    @Test
    void testAllSpares() {
        Game game = new Game();
        for (int i = 0; i < 21; i++) {
            game.score(5);
        }
        Assertions.assertEquals(150, game.getGameScore());
    }

    @Test
    void testIllegalScoreTooLowFirst() {
        Game game = new Game();
        Assertions.assertFalse(game.score(-1));
    }

    @Test
    void testIllegalScoreTooHighFirst() {
        Game game = new Game();
        Assertions.assertFalse(game.score(11));
    }

    @Test
    void testIllegalScoreTooHighSecond() {
        Game game = new Game();
        game.score(8);
        Assertions.assertFalse(game.score(3));
    }

    @Test
    void testIllegalScoreTooLowSecond() {
        Game game = new Game();
        game.score(8);
        Assertions.assertFalse(game.score(-1));
    }
}
