package de.kyle.gefangenendilemmaclient;

import de.kyle.gefangenendilemma.api.Prisoner;
import de.kyle.gefangenendilemma.api.event.PostMessEvent;
import de.kyle.gefangenendilemma.api.result.PrisonerMessResult;
import de.kyle.gefangenendilemmaclient.strategy.OpponentStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Scofield implements Prisoner {
    private final Map<String, OpponentStrategy> prisonerStrategies = new HashMap<>();
    private final Random random = new Random();
    private String currentOpponent;
    private PrisonerMessResult lastPrisonerResult;
    private PrisonerMessResult lastOpponentResult;

    @Override
    public String getName() {
        return "Scofield";
    }

    @Override
    public PrisonerMessResult messAround(String opponent) {
        currentOpponent = opponent;

        if (!isOpponentKnown(opponent)) {
            lastPrisonerResult = PrisonerMessResult.COOPERATE;
            return PrisonerMessResult.COOPERATE;
        }

        OpponentStrategy opponentStrategy = getCurrentOpponentStrategy();

        PrisonerMessResult nextMove;
        switch (opponentStrategy) {
            case NAIVE -> {
                nextMove = (random.nextDouble() < 0.6) ? PrisonerMessResult.BETRAY : PrisonerMessResult.COOPERATE;
            }
            case TIT_FOR_TAT, UNCLASSIFIABLE -> nextMove = (lastOpponentResult == PrisonerMessResult.BETRAY) ? PrisonerMessResult.BETRAY : PrisonerMessResult.COOPERATE;
            default -> nextMove = PrisonerMessResult.COOPERATE;
        }

        lastPrisonerResult = nextMove;
        return nextMove;
    }

    @Override
    public void onPostMessEvent(PostMessEvent postMessEvent) {
        String opponent = postMessEvent.opponent();
        PrisonerMessResult opponentResult = postMessEvent.result();

        if (!isOpponentKnown(opponent)) {
            prisonerStrategies.put(opponent, OpponentStrategy.UNCLASSIFIABLE);
        }

        if (isOpponentNaive(opponentResult)) {
            prisonerStrategies.put(opponent, OpponentStrategy.NAIVE);
        } else if (isOpponentTitForTat(opponentResult)) {
            prisonerStrategies.put(opponent, OpponentStrategy.TIT_FOR_TAT);
        } else {
            prisonerStrategies.put(opponent, OpponentStrategy.UNCLASSIFIABLE);
        }

        lastOpponentResult = opponentResult;
    }

    private boolean isOpponentNaive(PrisonerMessResult opponentResult) {
        return lastPrisonerResult == PrisonerMessResult.BETRAY && opponentResult == PrisonerMessResult.COOPERATE;
    }

    private boolean isOpponentTitForTat(PrisonerMessResult opponentResult) {
        return (lastPrisonerResult == PrisonerMessResult.COOPERATE && opponentResult == PrisonerMessResult.COOPERATE)
                || (lastPrisonerResult == PrisonerMessResult.BETRAY && opponentResult == PrisonerMessResult.BETRAY);
    }

    private boolean isOpponentKnown(String opponent) {
        return prisonerStrategies.containsKey(opponent);
    }

    private OpponentStrategy getCurrentOpponentStrategy() {
        return prisonerStrategies.getOrDefault(currentOpponent, OpponentStrategy.UNCLASSIFIABLE);
    }
}
